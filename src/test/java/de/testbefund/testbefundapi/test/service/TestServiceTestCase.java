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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

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

    @Mock
    private Supplier<LocalDateTime> currentDateSupplier;

    @Mock
    private TestRepository testRepository;

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
        assertThat(testCase.getCurrentResult()).isEqualTo(TestResult.UNKNOWN);
        assertThat(testCase.getCurrentStatus()).isEqualTo(TestStatus.IN_PROGRESS);
    }

    @Test
    void shouldCreateTests_withClient() {
        Client client = Client.builder().id("ID1234").name("Client").build();
        Mockito.when(clientRepository.findById("ID1234")).thenReturn(Optional.of(client));
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

    @Test
    void shouldCreateTests_withCurrentTimeStamp() {
        LocalDateTime currentDate = LocalDateTime.now();
        Mockito.when(currentDateSupplier.get()).thenReturn(currentDate);
        TestContainer testContainer = testService.createTestContainer(List.of(TestToCreate.builder().title("TitleA").build()));
        assertThat(testContainer.getTestCases())
                .extracting(TestCase::getLastChangeDate)
                .containsExactly(currentDate);
    }

    private TestCase withPersistentTestCase() {
        TestCase testCase = TestCase.builder()
                .title("SARS-CoV2")
                .client(new Client())
                .icdCode("1234")
                .lastChangeDate(LocalDateTime.now())
                .writeId("ABCD-1234")
                .currentResult(TestResult.UNKNOWN)
                .currentStatus(TestStatus.IN_PROGRESS)
                .build();
        Mockito.when(testRepository.findByWriteId(testCase.getWriteId())).thenReturn(Optional.of(testCase));
        return testCase;
    }

    @Test
    public void whenUpdatingTest_shouldUpdateTimeStamp() {
        LocalDateTime currentDate = LocalDateTime.now();
        Mockito.when(currentDateSupplier.get()).thenReturn(currentDate);
        TestCase testCase = withPersistentTestCase();
        testService.updateTestByWriteId(testCase.getWriteId(), TestResult.POSITIVE);
        assertThat(testCase.getCurrentResult()).isEqualTo(TestResult.POSITIVE);
        assertThat(testCase.getLastChangeDate()).isEqualTo(currentDate);
    }
}
