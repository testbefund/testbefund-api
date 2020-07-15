package de.testbefund.testbefundapi.testing.service;

import de.testbefund.testbefundapi.config.TestSecurityConfig;
import de.testbefund.testbefundapi.generated.api.model.TestbefundFindingResult;
import de.testbefund.testbefundapi.generated.api.model.TestbefundTestingDefinition;
import de.testbefund.testbefundapi.testing.data.SampleStatus;
import de.testbefund.testbefundapi.testing.data.TestingContainer;
import de.testbefund.testbefundapi.testing.data.TestingContainerRepository;
import de.testbefund.testbefundapi.testing.data.TestingSample;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@Import(TestSecurityConfig.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
class TestingServiceInterfaceTest {

    @Autowired
    private TestingService testingService;

    @Autowired
    private TestingContainerRepository testingContainerRepository;

    // transactional wraps the test in a transaction and performs a rollback once the test finishes
    @Test
    @Transactional
    public void shouldCreatePersistentTestCase() {
        TestingContainer newContainer =
            testingService.createTestingContainer(
                List.of(new TestbefundTestingDefinition().title("Test")),
                null);

        assertThat(newContainer.getId()).isNotNull();

        TestingContainer receivedContainer = testingContainerRepository.getOne(newContainer.getId());

        assertThat(receivedContainer).isEqualTo(newContainer);
    }

    @Test
    @Transactional
    public void shouldCreateTestCase_andReadItByReadId() {
        TestingContainer container =
            testingService.createTestingContainer(List.of(
                new TestbefundTestingDefinition().title("Test")), null);

        Optional<TestingContainer> maybeReceivedContainer = testingService.getContainerByReadId(container.getReadId());

        assertThat(maybeReceivedContainer).isPresent();
        assertThat(maybeReceivedContainer.get()).isEqualTo(container);
    }

    @Test
    @Transactional
    public void shouldCreateTestCase_andWriteItByWriteId() {
        TestingContainer container =
            testingService.createTestingContainer(List.of(
                new TestbefundTestingDefinition().title("Test")), null);
        TestingSample sample = container.getTestingSamples().iterator().next();
        testingService.updateTestingByWriteId(
            container.getWriteId(),
            sample.getId(),
            TestbefundFindingResult.POSITIVE);

        Optional<TestingContainer> maybeReceivedContainer =
            testingService.getContainerByReadId(container.getReadId());

        assertThat(maybeReceivedContainer).isPresent();
        assertThat(maybeReceivedContainer.get().getTestingSamples())
                .extracting(TestingSample::getTitle, TestingSample::getCurrentStatus)
                .containsExactly(Tuple.tuple("Test", SampleStatus.CONFIRM_POSITIVE));
    }
}
