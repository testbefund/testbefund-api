package de.testbefund.testbefundapi.test.service;

import de.testbefund.testbefundapi.client.data.ClientRepository;
import de.testbefund.testbefundapi.generated.api.model.TestbefundFindingResult;
import de.testbefund.testbefundapi.generated.api.model.TestbefundTestDefinition;
import de.testbefund.testbefundapi.generated.api.model.TestbefundUpdateFindingRequest;
import de.testbefund.testbefundapi.generated.api.model.TestbefundUpdateSingleFinding;
import de.testbefund.testbefundapi.test.data.TestCase;
import de.testbefund.testbefundapi.test.data.TestContainer;
import de.testbefund.testbefundapi.test.data.TestContainerRepository;
import de.testbefund.testbefundapi.test.data.TestStageStatus;
import de.testbefund.testbefundapi.test.error.NoTestCaseFoundException;
import de.testbefund.testbefundapi.test.error.TestNameNotUniqueException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Service
public class TestService {

    private final TestContainerRepository testContainerRepository;

    private final ClientRepository clientRepository;

    private final Supplier<LocalDateTime> currentDateSupplier;

    private final Supplier<String> idProvider;

    @Value("${testbefund.grace-period-in-minutes}")
    int gracePeriod = 20;

    public TestService(TestContainerRepository testContainerRepository,
                       ClientRepository clientRepository,
                       @Qualifier("currentDateSupplier") Supplier<LocalDateTime> currentDateSupplier,
                       @Qualifier("idProvider") Supplier<String> idProvider) {
        this.testContainerRepository = testContainerRepository;
        this.clientRepository = clientRepository;
        this.currentDateSupplier = currentDateSupplier;
        this.idProvider = idProvider;
    }

    @Transactional
    public TestContainer createTestContainer(Collection<TestbefundTestDefinition> testTitles, String clientId) {
        validate(testTitles);
        TestContainer container = TestContainer.builder()
                .testCases(testCasesOf(testTitles))
                .date(LocalDateTime.now())
                .readId(idProvider.get())
                .writeId(idProvider.get())
                .build();
        Optional.ofNullable(clientId)
                .flatMap(clientRepository::findById)
                .ifPresent(container::setClient);
        return testContainerRepository.save(container);
    }

    public Optional<TestContainer> getContainerByReadId(String readId) {
        return testContainerRepository.findByReadId(readId);
    }

    public Optional<TestContainer> getContainerByWriteId(String writeId) {
        return testContainerRepository.findByWriteId(writeId);
    }

    @Transactional
    public void updateTestByWriteId(String writeId, String testId, TestbefundFindingResult result) {
        testContainerRepository.findByWriteId(writeId)
                .ifPresentOrElse(container -> updateTestContainer(container, testId, result), this::throwNoTestCaseFound);
    }

    @Transactional
    public TestContainer updateTestContainer(TestbefundUpdateFindingRequest testRequest) {
        return testContainerRepository.findByWriteId(testRequest.getWriteId())
                .map(container -> batchUpdateContainer(container, testRequest))
                .orElseThrow(NoTestCaseFoundException::new);
    }

    private TestContainer batchUpdateContainer(TestContainer testContainer, TestbefundUpdateFindingRequest request) {
        request.getFindings().forEach(singleFinding -> updateSingleTest(singleFinding, testContainer));
        return testContainer;
    }

    private void updateSingleTest(TestbefundUpdateSingleFinding singleTest, TestContainer testContainer) {
        TestCase caseToUpdate = testContainer.getTestCases().stream().filter(testCase -> testCase.getTitle().toLowerCase().equals(singleTest.getTitle().toLowerCase()))
                .findAny()
                .orElseThrow(() -> new RuntimeException("Updated failed; No test found for " + singleTest.getTitle()));
        updateTestCase(singleTest.getTestResult(), caseToUpdate);
    }

    private void updateTestContainer(TestContainer testContainer, String testId, TestbefundFindingResult result) {
        testContainer.getTestCases()
                .stream()
                .filter(testCase -> testCase.getId().equals(testId))
                .findAny()
                .ifPresentOrElse(testCase -> updateTestCase(result, testCase), this::throwNoTestCaseFound);
    }

    private void updateTestCase(TestbefundFindingResult testResult, TestCase testCase) {
        switch (testResult) {
            case UNKNOWN:
                testCase.setCurrentStatus(TestStageStatus.ISSUED);
                break;
            case POSITIVE:
                testCase.setCurrentStatus(TestStageStatus.CONFIRM_POSITIVE);
                break;
            case NEGATIVE:
                testCase.setCurrentStatus(TestStageStatus.CONFIRM_NEGATIVE);
                break;
        }
        testCase.setLastChangeDate(currentDateSupplier.get());
    }

    private void throwNoTestCaseFound() {
        throw new NoTestCaseFoundException();
    }

    private List<TestCase> testCasesOf(Collection<TestbefundTestDefinition> testsToCreate) {
        return testsToCreate.stream()
                .map(this::toTestCaseForTitle)
                .collect(Collectors.toList());
    }

    private TestCase toTestCaseForTitle(TestbefundTestDefinition testToCreate) {
        return TestCase.builder()
                .title(testToCreate.getTitle())
                .icdCode(testToCreate.getIcdCode())
                .lastChangeDate(currentDateSupplier.get())
                .gracePeriodMinutes(gracePeriod)
                .currentStatus(TestStageStatus.ISSUED)
                .build();
    }

    private void validate(Collection<TestbefundTestDefinition> tests) {
        long uniqueNameCount = tests.stream()
                .map(TestbefundTestDefinition::getTitle)
                .distinct()
                .count();
        if (tests.size() != uniqueNameCount) {
            throw new TestNameNotUniqueException();
        }
    }
}
