package de.testbefund.testbefundapi.testing.service;

import de.testbefund.testbefundapi.administration.data.OrganizationRepository;
import de.testbefund.testbefundapi.generated.api.model.TestbefundFindingResult;
import de.testbefund.testbefundapi.generated.api.model.TestbefundTestingDefinition;
import de.testbefund.testbefundapi.generated.api.model.TestbefundUpdateFindingRequest;
import de.testbefund.testbefundapi.generated.api.model.TestbefundUpdateSingleFinding;
import de.testbefund.testbefundapi.testing.data.TestingSample;
import de.testbefund.testbefundapi.testing.data.TestingContainer;
import de.testbefund.testbefundapi.testing.data.TestingContainerRepository;
import de.testbefund.testbefundapi.testing.error.NoTestingSampleFoundException;
import de.testbefund.testbefundapi.testing.error.TestingSampleNameNotUniqueException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Service
public class TestingService {
    private final TestingContainerRepository testingContainerRepository;
    private final OrganizationRepository organizationRepository;
    private final Supplier<LocalDateTime> currentDateTimeSupplier;
    private final Supplier<String> idProvider;

    @Value("${testbefund.grace-period-in-minutes}")
    int gracePeriod = 20;

    public TestingService(TestingContainerRepository testingContainerRepository,
                       OrganizationRepository organizationRepository,
                       @Qualifier("currentDateTimeSupplier") Supplier<LocalDateTime> currentDateTimeSupplier,
                       @Qualifier("idProvider") Supplier<String> idProvider) {
        this.testingContainerRepository = testingContainerRepository;
        this.organizationRepository = organizationRepository;
        this.currentDateTimeSupplier = currentDateTimeSupplier;
        this.idProvider = idProvider;
    }

    @Transactional
    public TestingContainer createTestingContainer(Collection<TestbefundTestingDefinition> testingDefinitions, String clientId) {
        validate(testingDefinitions);
        TestingContainer container = TestingContainer.builder()
                .testingSamples(testingSamplesOf(testingDefinitions))
                .dateTime(LocalDateTime.now())
                .readId(idProvider.get())
                .writeId(idProvider.get())
                .build();
        Optional.ofNullable(clientId)
                .flatMap(organizationRepository::findById)
                .ifPresent(container::setClient);
        return testingContainerRepository.save(container);
    }

    public Optional<TestingContainer> getContainerByReadId(String readId) {
        return testingContainerRepository.findByReadId(readId);
    }

    public Optional<TestingContainer> getContainerByWriteId(String writeId) {
        return testingContainerRepository.findByWriteId(writeId);
    }

    @Transactional
    public void updateTestingByWriteId(String writeId, String testingId, TestbefundFindingResult result) {
        testingContainerRepository
            .findByWriteId(writeId)
            .ifPresentOrElse(container ->
                updateTestingContainer(container, testingId, result),
                this::throwNoTestingSampleFound
            );
    }

    @Transactional
    public TestingContainer updateFindingRequest(TestbefundUpdateFindingRequest findingRequest) {
        return testingContainerRepository
                .findByWriteId(findingRequest.getWriteId())
                .map(container -> batchUpdateContainer(container, findingRequest))
                .orElseThrow(NoTestingSampleFoundException);
    }

    private TestingContainer batchUpdateContainer(TestingContainer testingContainer, TestbefundUpdateFindingRequest request) {
        request
            .getFindings()
            .forEach(singleFinding ->
                updateSingleTest(singleFinding, testingContainer)
            );

        return testingContainer;
    }

    private void updateSingleTest(TestbefundUpdateSingleFinding singleFinding, TestingContainer testingContainer) {
        TestingSample sampleToUpdate =
            testingContainer.getTestingSamples()
                .stream()
                .filter(testingSample ->
                    testingSample
                        .getTitle()
                        .toLowerCase()
                        .equals(singleFinding.getTitle().toLowerCase())
                )
                .findAny()
                .orElseThrow(() ->
                    new RuntimeException("Updated failed; No test found for " + singleFinding.getTitle())
                );

        updateTestCase(singleTest.getTestingResult(), sampleToUpdate);
    }

    private void updateTestingContainer(TestingContainer testingContainer, String testingId, TestbefundFindingResult result) {
        testingContainer.getTestingSample()
                .stream()
                .filter(testCase -> testingSample.getId().equals(testingId))
                .findAny()
                .ifPresentOrElse(testingSample -> updateTestingSample(result, testingSample), this::throwNoTestingSampleFound);
    }

    private void updateTestCase(TestbefundFindingResult testResult, TestingSample testingSample) {
        switch (testResult) {
            case UNKNOWN:
                testingSample.setCurrentStatus(SampleStatus.ISSUED);
                break;
            case POSITIVE:
                testingSample.setCurrentStatus(SampleStatus.CONFIRM_POSITIVE);
                break;
            case NEGATIVE:
                testingSample.setCurrentStatus(SampleStatus.CONFIRM_NEGATIVE);
                break;
        }
        testingSample.setLastChangeDate(currentDateTimeSupplier.get());
    }

    private void throwNoTestingSampleFound() {
        throw new NoTestingSampleFoundException();
    }

    private List<TestingSample> testingSamplesOf(Collection<TestbefundTestingDefinition> testingDefinitions) {
        return testingDefinitions.stream()
                .map(this::toTestingSampleForTitle)
                .collect(Collectors.toList());
    }

    private TestingSample toTestingSampleForTitle(TestbefundTestingDefinition testingDefinition) {
        return TestingSample.builder()
                .title(testingDefinition.getTitle())
                .icdCode(testingDefinition.getIcdCode())
                .lastChangeDate(currentDateTimeSupplier.get())
                .gracePeriodMinutes(gracePeriod)
                .currentStatus(SampleStatus.ISSUED)
                .build();
    }

    private void validate(Collection<TestbefundTestingDefinition> tests) {
        long uniqueNameCount = tests.stream()
                .map(TestbefundTestingDefinition::getTitle)
                .distinct()
                .count();
        if (tests.size() != uniqueNameCount) throw new TestingSampleNameNotUniqueException();
    }
}
