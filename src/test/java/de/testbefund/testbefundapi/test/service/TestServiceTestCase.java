package de.testbefund.testbefundapi.test.service;

import de.testbefund.testbefundapi.client.data.Client;
import de.testbefund.testbefundapi.client.data.ClientRepository;
import de.testbefund.testbefundapi.test.data.*;
import de.testbefund.testbefundapi.test.dto.TestResultT;
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

import static de.testbefund.testbefundapi.test.dto.TestToCreate.builder;
import static java.util.List.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.MockitoAnnotations.initMocks;

class TestServiceTestCase {

    private TestService testService;

    @Mock
    private TestContainerRepository testContainerRepository;

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private Supplier<LocalDateTime> currentDateSupplier;

    @Mock
    private TestRepository testRepository;

    @Mock
    private Supplier<String> idProvider;

    @BeforeEach
    void setUp() {
        initMocks(this);
        testService = new TestService(testContainerRepository, testRepository, clientRepository, currentDateSupplier, idProvider);
        Mockito.when(testContainerRepository.save(any())).thenAnswer(returnsFirstArg());
    }

    @Test
    void shouldCreateTestContainer_withAllProvidedTests() {
        TestContainer testContainer = testService.createTestContainer(of(
                builder().title("TitleA").build(),
                builder().title("TitleB").build()
        ));
        assertThat(testContainer.getTestCases())
                .extracting(TestCase::getTitle)
                .containsExactlyInAnyOrder("TitleA", "TitleB");
    }

    @Test
    void shouldInitializeTests_withCorrectData() {
        TestContainer testContainer = testService.createTestContainer(of(builder().title("TitleA").build()));
        assertThat(testContainer.getTestCases()).hasSize(1);
        TestCase testCase = testContainer.getTestCases().iterator().next();
        assertThat(testCase.getId()).isNull(); // Filled in by auto generation
        assertThat(testCase.getCurrentStatus()).isEqualTo(TestStageStatus.ISSUED);
    }

    @Test
    void shouldCreateTests_withClient() {
        Client client = Client.builder().id("ID1234").name("Client").build();
        Mockito.when(clientRepository.findById("ID1234")).thenReturn(Optional.of(client));
        TestToCreate testToCreate = builder().title("Title").clientId("ID1234").build();
        TestContainer testContainer = testService.createTestContainer(of(testToCreate));
        assertThat(testContainer.getTestCases())
                .extracting(TestCase::getClient)
                .containsExactly(client);
    }

    @Test
    void shouldCreateTest_withICDCode() {
        TestToCreate testToCreate = builder().title("Title").icdCode("icd1234").build();
        TestContainer testContainer = testService.createTestContainer(of(testToCreate));
        assertThat(testContainer.getTestCases())
                .extracting(TestCase::getIcdCode)
                .containsExactly("icd1234");
    }

    @Test
    void shouldPersistTestContainer() {
        TestContainer testContainer = testService.createTestContainer(of(builder().title("TitleA").build()));
        Mockito.verify(testContainerRepository, times(1)).save(testContainer);
    }

    @Test
    void shouldCreateTests_withCurrentTimeStamp() {
        LocalDateTime currentDate = LocalDateTime.now();
        Mockito.when(currentDateSupplier.get()).thenReturn(currentDate);
        TestContainer testContainer = testService.createTestContainer(of(builder().title("TitleA").build()));
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
                .currentStatus(TestStageStatus.ISSUED)
                .build();
        Mockito.when(testRepository.findByWriteId(testCase.getWriteId())).thenReturn(Optional.of(testCase));
        return testCase;
    }

    @Test
    void whenUpdatingTest_shouldUpdateTimeStamp() {
        LocalDateTime currentDate = LocalDateTime.now();
        Mockito.when(currentDateSupplier.get()).thenReturn(currentDate);
        TestCase testCase = withPersistentTestCase();
        testService.updateTestByWriteId(testCase.getWriteId(), TestResultT.NEGATIVE);
        assertThat(testCase.getLastChangeDate()).isEqualTo(currentDate);
    }

    @Test
    void whenUpdatingTest_shouldSetCorrectStatusForNegative() {
        TestCase testCase = withPersistentTestCase();
        testService.updateTestByWriteId(testCase.getWriteId(), TestResultT.NEGATIVE);
        assertThat(testCase.getCurrentStatus()).isEqualTo(TestStageStatus.CONFIRM_NEGATIVE);
    }

    @Test
    void whenUpdatingTest_shouldSetCorrectStatusForPositive() {
        TestCase testCase = withPersistentTestCase();
        testService.updateTestByWriteId(testCase.getWriteId(), TestResultT.POSITIVE);
        assertThat(testCase.getCurrentStatus()).isEqualTo(TestStageStatus.CONFIRM_POSITIVE);
    }

    @Test
    void shouldUseIdProvider() {
        Mockito.when(idProvider.get()).thenReturn("ABCDE");
        TestContainer testContainer = testService.createTestContainer(of(builder().title("TitleA").build()));
        assertThat(testContainer.getReadId()).isEqualTo("ABCDE");
        assertThat(testContainer.getTestCases())
                .extracting(TestCase::getWriteId)
                .containsExactly("ABCDE");
    }
}
