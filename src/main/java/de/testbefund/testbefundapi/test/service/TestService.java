package de.testbefund.testbefundapi.test.service;

import de.testbefund.testbefundapi.test.data.*;
import de.testbefund.testbefundapi.test.error.NoTestCaseFoundException;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class TestService {

    private final TestContainerRepository testContainerRepository;

    private final TestRepository testRepository;

    public TestService(TestContainerRepository testContainerRepository, TestRepository testRepository) {
        this.testContainerRepository = testContainerRepository;
        this.testRepository = testRepository;
    }

    @Transactional
    public TestContainer createTestContainer(Collection<String> testTitles) {
        TestContainer container = TestContainer.builder()
                .testCases(testCasesOf(testTitles))
                .date(LocalDateTime.now())
                .readId(UUID.randomUUID().toString())
                .build();
        return testContainerRepository.save(container);
    }

    public Optional<TestContainer> getContainerByReadId(String readId) {
        return testContainerRepository.findByReadId(readId);
    }

    @Transactional
    public void updateTestByWriteId(String writeId, TestResult testResult) {
        testRepository.findByWriteId(writeId)
                .ifPresentOrElse(testCase -> testCase.setResult(testResult), this::throwNoTestCaseFound);

    }


    private void throwNoTestCaseFound() {
        throw new NoTestCaseFoundException();
    }

    private List<TestCase> testCasesOf(Collection<String> titles) {
        return titles.stream()
                .map(this::toTestCaseForTitle)
                .collect(Collectors.toList());
    }

    private TestCase toTestCaseForTitle(String title) {
        return TestCase.builder().title(title)
                .writeId(UUID.randomUUID().toString())
                .result(TestResult.UNKNOWN)
                .status(TestStatus.IN_PROGRESS)
                .build();
    }
}
