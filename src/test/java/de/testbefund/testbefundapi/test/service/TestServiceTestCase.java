package de.testbefund.testbefundapi.test.service;

import de.testbefund.testbefundapi.client.data.Client;
import de.testbefund.testbefundapi.client.data.ClientRepository;
import de.testbefund.testbefundapi.test.data.*;
import de.testbefund.testbefundapi.test.dto.TestToCreate;
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

    @Mock
    private ClientRepository clientRepository;

    @BeforeEach
    void setUp() {
        initMocks(this);
        Mockito.when(testContainerRepository.save(any())).thenAnswer(returnsFirstArg());
    }

    @Test
    void shouldCreateTestContainer_withAllProvidedTests() {
        TestContainer testContainer = testService.createTestContainer(List.of(
                TestToCreate.builder().title("TitleA").build(),
                TestToCreate.builder().title("TitleB").build()
        ));
        assertThat(testContainer.getTestCases())
                .extracting(TestCase::getTitle)
                .containsExactlyInAnyOrder("TitleA", "TitleB");
    }

    @Test
    void shouldInitializeTests_withCorrectData() {
        TestContainer testContainer = testService.createTestContainer(List.of(TestToCreate.builder().title("TitleA").build()));
        assertThat(testContainer.getTestCases()).hasSize(1);
        TestCase testCase = testContainer.getTestCases().iterator().next();
        assertThat(testCase.getId()).isNull(); // Filled in by auto generation
        assertThat(testCase.getWriteId()).isNotNull();
        assertThat(testCase.getResult()).isEqualTo(TestResult.UNKNOWN);
        assertThat(testCase.getStatus()).isEqualTo(TestStatus.IN_PROGRESS);
    }

    @Test
    void shouldCreateTests_withClient() {
        Client client = Client.builder().id("ID1234").name("Client").build();
        Mockito.when(clientRepository.getOne("ID1234")).thenReturn(client);
        TestToCreate testToCreate = TestToCreate.builder().title("Title").clientId("ID1234").build();
        TestContainer testContainer = testService.createTestContainer(List.of(testToCreate));
        assertThat(testContainer.getTestCases())
                .extracting(TestCase::getClient)
                .containsExactly(client);
    }

    @Test
    public void shouldCreateTest_withICDCode() {
        TestToCreate testToCreate = TestToCreate.builder().title("Title").icdCode("icd1234").build();
        TestContainer testContainer = testService.createTestContainer(List.of(testToCreate));
        assertThat(testContainer.getTestCases())
                .extracting(TestCase::getIcdCode)
                .containsExactly("icd1234");
    }

    @Test
    void shouldPersistTestContainer() {
        TestContainer testContainer = testService.createTestContainer(List.of(TestToCreate.builder().title("TitleA").build()));
        Mockito.verify(testContainerRepository, times(1)).save(testContainer);
    }
}
