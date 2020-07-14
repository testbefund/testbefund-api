package de.testbefund.testbefundapi.test.service;

import de.testbefund.testbefundapi.config.TestingSecurityConfig;
import de.testbefund.testbefundapi.generated.api.model.TestbefundFindingResult;
import de.testbefund.testbefundapi.generated.api.model.TestbefundTestDefinition;
import de.testbefund.testbefundapi.test.data.TestCase;
import de.testbefund.testbefundapi.test.data.TestContainer;
import de.testbefund.testbefundapi.test.data.TestContainerRepository;
import de.testbefund.testbefundapi.test.data.TestStageStatus;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@Import(TestingSecurityConfig.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
class TestServiceITest {

    @Autowired
    private TestService testService;

    @Autowired
    private TestContainerRepository testContainerRepository;

    // transactional wraps the test in a transaction and performs a rollback once the test finishes
    @Test
    @Transactional
    public void shouldCreatePersistentTestCase() {
        TestContainer savedContainer = testService.createTestContainer(List.of(new TestbefundTestDefinition().title("Test")), null);
        assertThat(savedContainer.getId()).isNotNull();
        TestContainer persistentContainer = testContainerRepository.getOne(savedContainer.getId());
        assertThat(persistentContainer).isEqualTo(savedContainer);
    }

    @Test
    @Transactional
    public void shouldCreateTestCase_andReadItByReadId() {
        TestContainer savedContainer = testService.createTestContainer(List.of(new TestbefundTestDefinition().title("Test")), null);
        Optional<TestContainer> maybePersistentContainer = testService.getContainerByReadId(savedContainer.getReadId());
        assertThat(maybePersistentContainer).isPresent();
        assertThat(maybePersistentContainer.get()).isEqualTo(savedContainer);
    }

    @Test
    @Transactional
    public void shouldCreateTestCase_andWriteItByWriteId() {
        TestContainer savedContainer = testService.createTestContainer(List.of(new TestbefundTestDefinition().title("Test")), null);
        TestCase testCase = savedContainer.getTestCases().iterator().next();
        testService.updateTestByWriteId(savedContainer.getWriteId(), testCase.getId(), TestbefundFindingResult.POSITIVE);
        Optional<TestContainer> persistentContainer = testService.getContainerByReadId(savedContainer.getReadId());
        assertThat(persistentContainer).isPresent();
        assertThat(persistentContainer.get().getTestCases())
                .extracting(TestCase::getTitle, TestCase::getCurrentStatus)
                .containsExactly(Tuple.tuple("Test", TestStageStatus.CONFIRM_POSITIVE));
    }
}
