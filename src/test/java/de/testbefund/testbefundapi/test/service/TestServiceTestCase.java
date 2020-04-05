package de.testbefund.testbefundapi.test.service;

import de.testbefund.testbefundapi.test.data.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.MockitoAnnotations.initMocks;

public class TestServiceTestCase {

    @InjectMocks
    private TestService testService;

    @Mock
    private TestContainerRepository testContainerRepository;

    @BeforeEach
    void setUp() {
        initMocks(this);
        Mockito.when(testContainerRepository.save(any())).thenAnswer(returnsFirstArg());
    }

    @Test
    void shouldCreateTestContainer_withAllProvidedTests() {
        TestContainer testContainer = testService.createTestContainer(List.of("TitleA", "TitleB"));
        assertThat(testContainer.getTestCases())
                .extracting(TestCase::getTitle)
                .containsExactlyInAnyOrder("TitleA", "TitleB");
    }

    @Test
    void shouldInitializeTests_withCorrectData() {
        TestContainer testContainer = testService.createTestContainer(List.of("Title"));
        assertThat(testContainer.getTestCases()).hasSize(1);
        TestCase testCase = testContainer.getTestCases().iterator().next();
        assertThat(testCase.getId()).isNull(); // Filled in by auto generation
        assertThat(testCase.getWriteId()).isNotNull();
        assertThat(testCase.getResult()).isEqualTo(TestResult.UNKNOWN);
        assertThat(testCase.getStatus()).isEqualTo(TestStatus.IN_PROGRESS);
    }

    @Test
    void shouldPersistTestContainer() {
        TestContainer testContainer = testService.createTestContainer(List.of("Title"));
        Mockito.verify(testContainerRepository, times(1)).save(testContainer);
    }
}
