package de.testbefund.testbefundapi.test.service;

import de.testbefund.testbefundapi.test.data.*;
import de.testbefund.testbefundapi.test.dto.TestResultT;
import de.testbefund.testbefundapi.test.dto.TestToCreate;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

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
        TestContainer savedContainer = testService.createTestContainer(List.of(TestToCreate.builder().title("Test").build()), null);
        assertThat(savedContainer.getId()).isNotNull();
        TestContainer persistentContainer = testContainerRepository.getOne(savedContainer.getId());
        assertThat(persistentContainer).isEqualTo(savedContainer);
    }

    @Test
    @Transactional
    public void shouldCreateTestCase_andReadItByReadId() {
        TestContainer savedContainer = testService.createTestContainer(List.of(TestToCreate.builder().title("Test").build()), null);
        Optional<TestContainer> maybePersistentContainer = testService.getContainerByReadId(savedContainer.getReadId());
        assertThat(maybePersistentContainer).isPresent();
        assertThat(maybePersistentContainer.get()).isEqualTo(savedContainer);
    }

    @Test
    @Transactional
    public void shouldCreateTestCase_andWriteItByWriteId() {
        TestContainer savedContainer = testService.createTestContainer(List.of(TestToCreate.builder().title("Test").build()), null);
        TestCase testCase = savedContainer.getTestCases().iterator().next();
        testService.updateTestByWriteId(savedContainer.getWriteId(), testCase.getId(), TestResultT.POSITIVE);
        Optional<TestContainer> persistentContainer = testService.getContainerByReadId(savedContainer.getReadId());
        assertThat(persistentContainer).isPresent();
        assertThat(persistentContainer.get().getTestCases())
                .extracting(TestCase::getTitle, TestCase::getCurrentStatus)
                .containsExactly(Tuple.tuple("Test", TestStageStatus.CONFIRM_POSITIVE));
    }
}
