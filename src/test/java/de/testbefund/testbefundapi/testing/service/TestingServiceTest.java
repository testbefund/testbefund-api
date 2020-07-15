package de.testbefund.testbefundapi.testing.service;

import de.testbefund.testbefundapi.administration.data.Organization;
import de.testbefund.testbefundapi.administration.data.OrganizationRepository;
import de.testbefund.testbefundapi.generated.api.model.TestbefundFindingResult;
import de.testbefund.testbefundapi.generated.api.model.TestbefundTestingDefinition;
import de.testbefund.testbefundapi.generated.api.model.TestbefundUpdateFindingRequest;
import de.testbefund.testbefundapi.generated.api.model.TestbefundUpdateSingleFinding;
import de.testbefund.testbefundapi.testing.data.SampleStatus;
import de.testbefund.testbefundapi.testing.data.TestingContainer;
import de.testbefund.testbefundapi.testing.data.TestingContainerRepository;
import de.testbefund.testbefundapi.testing.data.TestingSample;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import static java.util.List.of;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.MockitoAnnotations.initMocks;

import static org.assertj.core.api.Assertions.*;

class TestingServiceTest {

    private TestingService testingService;

    @Mock
    private TestingContainerRepository testingContainerRepository;

    @Mock
    private OrganizationRepository organizationRepository;

    @Mock
    private Supplier<LocalDateTime> currentDateSupplier;

    @Mock
    private Supplier<String> idProvider;

    @BeforeEach
    void setup() {
        initMocks(this);
        testingService = new TestingService(testingContainerRepository, organizationRepository, currentDateSupplier, idProvider);
        Mockito.when(testingContainerRepository.save(any())).thenAnswer(returnsFirstArg());
    }


    @Test
    void shouldCreateTestContainer_withAllProvidedTests() {
        TestingContainer newContainer = testingService.createTestingContainer(of(
            definitionFor("TitleA", null),
            definitionFor("TitleB", null)
        ), null);

        assertThat(newContainer.getTestingSamples())
            .extracting(TestingSample::getTitle)
            .containsExactlyInAnyOrder("TitleA", "TitleB");
    }

    @Test
    void shouldInitializeTests_withCorrectData() {
        TestingContainer newContainer =
            testingService.createTestingContainer(
                of(definitionFor("TitleA", null)), null);

        assertThat(newContainer.getTestingSamples()).hasSize(1);

        TestingSample sample = newContainer.getTestingSamples().iterator().next();

        assertThat(sample.getId()).isNull(); // Filled in by auto generation
        assertThat(sample.getCurrentStatus()).isEqualTo(SampleStatus.ISSUED);
    }

    private TestbefundTestingDefinition definitionFor(String title, String icdCode) {
        TestbefundTestingDefinition definition = new TestbefundTestingDefinition();

        definition.setTitle(title);
        definition.setIcdCode(icdCode);

        return definition;
    }


    @Test
    void shouldCreateTest_withICDCode() {
        TestbefundTestingDefinition testToCreate = definitionFor("Title", "icd1234");
        TestingContainer testContainer = testingService.createTestingContainer(of(testToCreate), null);

        assertThat(testContainer.getTestingSamples())
            .extracting(TestingSample::getIcdCode)
            .containsExactly("icd1234");
    }

    @Test
    void shouldPersistTestContainer() {
        TestingContainer testContainer = testingService.createTestingContainer(
            of(definitionFor("TitleA", "icd1234")),
            null);
        Mockito.verify(testingContainerRepository, times(1)).save(testContainer);
    }

    @Test
    void shouldCreateTests_withCurrentTimeStamp() {
        LocalDateTime currentDate = LocalDateTime.now();
        Mockito.when(currentDateSupplier.get()).thenReturn(currentDate);
        TestingContainer newContainer = testingService.createTestingContainer(of(
            definitionFor("TitleA", null)), null);

        assertThat(newContainer.getTestingSamples())
            .extracting(TestingSample::getLastChangeDateTime)
            .containsExactly(currentDate);
    }

    private TestingSample withPersistentTestingSample(String writeId) {
        TestingSample sample = TestingSample.builder()
            .title("SARS-CoV2")
            .id("1234")
            .icdCode("1234")
            .lastChangeDateTime(LocalDateTime.now())
            .currentStatus(SampleStatus.ISSUED)
            .build();
        TestingContainer container = TestingContainer.builder()
            .testingSamples(List.of(sample))
            .writeId(writeId)
            .build();
        Mockito
            .when(testingContainerRepository.findByWriteId(container.getWriteId()))
            .thenReturn(Optional.of(container));

        return sample;
    }

    @Test
    void whenUpdatingTest_shouldUpdateTimeStamp() {
        LocalDateTime now = LocalDateTime.now();
        Mockito.when(currentDateSupplier.get()).thenReturn(now);
        TestingSample sample = withPersistentTestingSample("ABCD");

        testingService.updateTestingByWriteId("ABCD", sample.getId(), TestbefundFindingResult.NEGATIVE);

        assertThat(sample.getLastChangeDateTime()).isEqualTo(now);
    }

    @Test
    void whenUpdatingTest_shouldSetCorrectStatusForNegative() {
        TestingSample sample = withPersistentTestingSample("ABCDE");

        testingService.updateTestingByWriteId("ABCDE", sample.getId(), TestbefundFindingResult.NEGATIVE);

        assertThat(sample.getCurrentStatus()).isEqualTo(SampleStatus.CONFIRM_NEGATIVE);
    }

    @Test
    void whenUpdatingTest_shouldSetCorrectStatusForPositive() {
        TestingSample sample = withPersistentTestingSample("ABCDE");

        testingService.updateTestingByWriteId("ABCDE", sample.getId(), TestbefundFindingResult.POSITIVE);

        assertThat(sample.getCurrentStatus()).isEqualTo(SampleStatus.CONFIRM_POSITIVE);
    }

    @Test
    void shouldUseIdProvider() {
        Mockito.when(idProvider.get()).thenReturn("ABCDE");

        TestingContainer container =
            testingService.createTestingContainer(of(
                definitionFor("TitleA", null)), null);

        assertThat(container.getReadId()).isEqualTo("ABCDE");
    }

    @Test
    void shouldAttachWriteId_toContainer() {
        Mockito.when(idProvider.get()).thenReturn("ABCDE");

        TestingContainer container =
            testingService.createTestingContainer(of(
                definitionFor("TitleA", null)), null);

        assertThat(container.getWriteId()).isEqualTo("ABCDE");
    }

    @Test
    void shouldBatchUpdate() {
        // Title is SARS-CoV2
        TestingSample sample = withPersistentTestingSample("ABCDE");
        TestbefundUpdateSingleFinding finding = new TestbefundUpdateSingleFinding()
                .title(sample.getTitle())
                .testingResult(TestbefundFindingResult.NEGATIVE);
        TestbefundUpdateFindingRequest request = new TestbefundUpdateFindingRequest();

        request.setWriteId("ABCDE");
        request.setFindings(of(finding));
        TestingContainer result = testingService.updateFindingRequest(request);

        assertThat(result.getTestingSamples())
                .extracting(TestingSample::getCurrentStatus)
                .containsExactly(SampleStatus.CONFIRM_NEGATIVE);
    }

    @Test
    void shouldCreateContainerWithClient() {
        Organization organization =
            Organization.builder().id("12345-abcdef").name("Testorganization").build();
        Mockito
            .when(organizationRepository.findById(organization.getId()))
            .thenReturn(Optional.of(organization));

        TestingContainer container = testingService.createTestingContainer(of(
            definitionFor("TitleA", null)), organization.getId());

        assertThat(container.getOrganization()).isEqualTo(organization);
    }
}
