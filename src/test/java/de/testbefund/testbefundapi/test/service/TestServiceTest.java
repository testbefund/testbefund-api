package de.testbefund.testbefundapi.test.service;

import de.testbefund.testbefundapi.client.data.Client;
import de.testbefund.testbefundapi.client.data.ClientRepository;
import de.testbefund.testbefundapi.generated.api.model.TestbefundFindingResult;
import de.testbefund.testbefundapi.generated.api.model.TestbefundTestDefinition;
import de.testbefund.testbefundapi.generated.api.model.TestbefundUpdateFindingRequest;
import de.testbefund.testbefundapi.generated.api.model.TestbefundUpdateSingleFinding;
import de.testbefund.testbefundapi.test.data.TestCase;
import de.testbefund.testbefundapi.test.data.TestContainer;
import de.testbefund.testbefundapi.test.data.TestContainerRepository;
import de.testbefund.testbefundapi.test.data.TestStageStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import static java.util.List.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.MockitoAnnotations.initMocks;

class TestServiceTest {

    private TestService testService;

    @Mock
    private TestContainerRepository testContainerRepository;

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private Supplier<LocalDateTime> currentDateSupplier;

    @Mock
    private Supplier<String> idProvider;

    @BeforeEach
    void setUp() {
        initMocks(this);
        testService = new TestService(testContainerRepository, clientRepository, currentDateSupplier, idProvider);
        Mockito.when(testContainerRepository.save(any())).thenAnswer(returnsFirstArg());
    }


    @Test
    void shouldCreateTestContainer_withAllProvidedTests() {
        TestContainer testContainer = testService.createTestContainer(of(
                definitionFor("TitleA", null),
                definitionFor("TitleB", null)
        ), null);
        assertThat(testContainer.getTestCases())
                .extracting(TestCase::getTitle)
                .containsExactlyInAnyOrder("TitleA", "TitleB");
    }

    @Test
    void shouldInitializeTests_withCorrectData() {
        TestContainer testContainer = testService.createTestContainer(of(definitionFor("TitleA", null)), null);
        assertThat(testContainer.getTestCases()).hasSize(1);
        TestCase testCase = testContainer.getTestCases().iterator().next();
        assertThat(testCase.getId()).isNull(); // Filled in by auto generation
        assertThat(testCase.getCurrentStatus()).isEqualTo(TestStageStatus.ISSUED);
    }

    private TestbefundTestDefinition definitionFor(String title, String icdCode) {
        TestbefundTestDefinition testbefundTestDefinition = new TestbefundTestDefinition();
        testbefundTestDefinition.setTitle(title);
        testbefundTestDefinition.setIcdCode(icdCode);
        return testbefundTestDefinition;
    }


    @Test
    void shouldCreateTest_withICDCode() {
        TestbefundTestDefinition testToCreate = definitionFor("Title", "icd1234");
        TestContainer testContainer = testService.createTestContainer(of(testToCreate), null);
        assertThat(testContainer.getTestCases())
                .extracting(TestCase::getIcdCode)
                .containsExactly("icd1234");
    }

    @Test
    void shouldPersistTestContainer() {
        TestContainer testContainer = testService.createTestContainer(
                of(definitionFor("TitleA", "icd1234")),
                null);
        Mockito.verify(testContainerRepository, times(1)).save(testContainer);
    }

    @Test
    void shouldCreateTests_withCurrentTimeStamp() {
        LocalDateTime currentDate = LocalDateTime.now();
        Mockito.when(currentDateSupplier.get()).thenReturn(currentDate);
        TestContainer testContainer = testService.createTestContainer(of(definitionFor("TitleA", null)), null);
        assertThat(testContainer.getTestCases())
                .extracting(TestCase::getLastChangeDate)
                .containsExactly(currentDate);
    }

    private TestCase withPersistentTestCase(String writeId) {
        TestCase testCase = TestCase.builder()
                .title("SARS-CoV2")
                .id("1234")
                .icdCode("1234")
                .lastChangeDate(LocalDateTime.now())
                .currentStatus(TestStageStatus.ISSUED)
                .build();
        TestContainer testContainer = TestContainer.builder()
                .testCases(List.of(testCase))
                .writeId(writeId)
                .build();
        Mockito.when(testContainerRepository.findByWriteId(testContainer.getWriteId())).thenReturn(Optional.of(testContainer));
        return testCase;
    }

    @Test
    void whenUpdatingTest_shouldUpdateTimeStamp() {
        LocalDateTime currentDate = LocalDateTime.now();
        Mockito.when(currentDateSupplier.get()).thenReturn(currentDate);
        TestCase testCase = withPersistentTestCase("ABCD");
        testService.updateTestByWriteId("ABCD", testCase.getId(), TestbefundFindingResult.NEGATIVE);
        assertThat(testCase.getLastChangeDate()).isEqualTo(currentDate);
    }

    @Test
    void whenUpdatingTest_shouldSetCorrectStatusForNegative() {
        TestCase testCase = withPersistentTestCase("ABCDE");
        testService.updateTestByWriteId("ABCDE", testCase.getId(), TestbefundFindingResult.NEGATIVE);
        assertThat(testCase.getCurrentStatus()).isEqualTo(TestStageStatus.CONFIRM_NEGATIVE);
    }

    @Test
    void whenUpdatingTest_shouldSetCorrectStatusForPositive() {
        TestCase testCase = withPersistentTestCase("ABCDE");
        testService.updateTestByWriteId("ABCDE", testCase.getId(), TestbefundFindingResult.POSITIVE);
        assertThat(testCase.getCurrentStatus()).isEqualTo(TestStageStatus.CONFIRM_POSITIVE);
    }

    @Test
    void shouldUseIdProvider() {
        Mockito.when(idProvider.get()).thenReturn("ABCDE");
        TestContainer testContainer = testService.createTestContainer(of(definitionFor("TitleA", null)), null);
        assertThat(testContainer.getReadId()).isEqualTo("ABCDE");
    }

    @Test
    void shouldAttachWriteId_toContainer() {
        Mockito.when(idProvider.get()).thenReturn("ABCDE");
        TestContainer testContainer = testService.createTestContainer(of(definitionFor("TitleA", null)), null);
        assertThat(testContainer.getWriteId()).isEqualTo("ABCDE");
    }

    @Test
    void shouldBatchUpdate() {
        // Title is SARS-CoV2
        TestCase testCase = withPersistentTestCase("ABCDE");
        TestbefundUpdateSingleFinding finding = new TestbefundUpdateSingleFinding()
                .title(testCase.getTitle())
                .testResult(TestbefundFindingResult.NEGATIVE);
        TestbefundUpdateFindingRequest request = new TestbefundUpdateFindingRequest();
        request.setWriteId("ABCDE");
        request.setFindings(of(finding));
        TestContainer result = testService.updateTestContainer(request);
        assertThat(result.getTestCases())
                .extracting(TestCase::getCurrentStatus)
                .containsExactly(TestStageStatus.CONFIRM_NEGATIVE);
    }

    @Test
    void shouldCreateContainerWithClient() {
        Client client = Client.builder().id("12345-abcdef").name("Testorganization").build();
        Mockito.when(clientRepository.findById(client.getId())).thenReturn(Optional.of(client));
        TestContainer testContainer = testService.createTestContainer(of(definitionFor("TitleA", null)), client.getId());
        assertThat(testContainer.getClient()).isEqualTo(client);
    }
}
