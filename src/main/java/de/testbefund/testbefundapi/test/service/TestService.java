package de.testbefund.testbefundapi.test.service;

import de.testbefund.testbefundapi.client.data.Client;
import de.testbefund.testbefundapi.client.data.ClientRepository;
import de.testbefund.testbefundapi.test.data.TestCase;
import de.testbefund.testbefundapi.test.data.TestContainer;
import de.testbefund.testbefundapi.test.data.TestContainerRepository;
import de.testbefund.testbefundapi.test.data.TestStageStatus;
import de.testbefund.testbefundapi.test.dto.TestResultT;
import de.testbefund.testbefundapi.test.dto.TestToCreate;
import de.testbefund.testbefundapi.test.dto.UpdateTestRequest;
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
    public TestContainer createTestContainer(Collection<TestToCreate> testTitles) {
        validate(testTitles);
        TestContainer container = TestContainer.builder()
                .testCases(testCasesOf(testTitles))
                .date(LocalDateTime.now())
                .readId(idProvider.get())
                .writeId(idProvider.get())
                .build();
        return testContainerRepository.save(container);
    }

    public Optional<TestContainer> getContainerByReadId(String readId) {
        return testContainerRepository.findByReadId(readId);
    }

    public Optional<TestContainer> getContainerByWriteId(String writeId) {
        return testContainerRepository.findByWriteId(writeId);
    }

    @Transactional
    public void updateTestByWriteId(String writeId, String testId, TestResultT result) {
        testContainerRepository.findByWriteId(writeId)
                .ifPresentOrElse(container -> updateTestContainer(container, testId, result), this::throwNoTestCaseFound);
    }

    @Transactional
    public TestContainer updateTestContainer(UpdateTestRequest testRequest) {
        return testContainerRepository.findByWriteId(testRequest.writeId)
                .map(container -> batchUpdateContainer(container, testRequest))
                .orElseThrow(NoTestCaseFoundException::new);
    }

    private TestContainer batchUpdateContainer(TestContainer testContainer, UpdateTestRequest request) {
        request.tests.forEach(singleTest -> updateSingleTest(singleTest, testContainer));
        return testContainer;
    }

    private void updateSingleTest(UpdateTestRequest.SingleTest singleTest, TestContainer testContainer) {
        TestCase caseToUpdate = testContainer.getTestCases().stream().filter(testCase -> testCase.getTitle().toLowerCase().equals(singleTest.title.toLowerCase()))
                .findAny()
                .orElseThrow(() -> new RuntimeException("Updated failed; No test found for " + singleTest.title));
        updateTestCase(singleTest.testResult, caseToUpdate);
    }

    private void updateTestContainer(TestContainer testContainer, String testId, TestResultT result) {
        testContainer.getTestCases()
                .stream()
                .filter(testCase -> testCase.getId().equals(testId))
                .findAny()
                .ifPresentOrElse(testCase -> updateTestCase(result, testCase), this::throwNoTestCaseFound);
    }

    private void updateTestCase(TestResultT testResult, TestCase testCase) {
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

    private List<TestCase> testCasesOf(Collection<TestToCreate> testsToCreate) {
        return testsToCreate.stream()
                .map(this::toTestCaseForTitle)
                .collect(Collectors.toList());
    }

    private TestCase toTestCaseForTitle(TestToCreate testToCreate) {
        Client client = Optional.ofNullable(testToCreate.clientId)
                .flatMap(clientRepository::findById)
                .orElse(null);
        return TestCase.builder()
                .title(testToCreate.title)
                .client(client)
                .icdCode(testToCreate.icdCode)
                .lastChangeDate(currentDateSupplier.get())
                .gracePeriodMinutes(gracePeriod)
                .currentStatus(TestStageStatus.ISSUED)
                .build();
    }

    private void validate(Collection<TestToCreate> tests) {
        long uniqueNameCount = tests.stream()
                .map(testToCreate -> testToCreate.title)
                .distinct()
                .count();
        if (tests.size() != uniqueNameCount) {
            throw new TestNameNotUniqueException();
        }
    }
}
