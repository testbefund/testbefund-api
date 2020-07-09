package de.testbefund.testbefundapi.test.dto;

import de.testbefund.testbefundapi.generated.api.model.TestbefundFinding;
import de.testbefund.testbefundapi.generated.api.model.TestbefundFindingResult;
import de.testbefund.testbefundapi.test.data.TestCase;
import de.testbefund.testbefundapi.test.data.TestStageStatus;
import de.testbefund.testbefundapi.test.mappers.FindingContainerMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;

import java.time.LocalDateTime;
import java.time.temporal.TemporalAmount;
import java.util.stream.Stream;

import static de.testbefund.testbefundapi.test.data.TestStageStatus.CONFIRM_NEGATIVE;
import static java.time.temporal.ChronoUnit.MINUTES;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class TestReadMapperTest {


    static Stream<Arguments> statusTestArguments() {
        return Stream.of(
                Arguments.of(CONFIRM_NEGATIVE, TestbefundFindingResult.NEGATIVE),
                Arguments.of(TestStageStatus.CONFIRM_POSITIVE, TestbefundFindingResult.POSITIVE),
                Arguments.of(TestStageStatus.ISSUED, TestbefundFindingResult.UNKNOWN)
        );
    }

    @ParameterizedTest
    @MethodSource("statusTestArguments")
    void shouldMapTestStatus(TestStageStatus status, TestbefundFindingResult expectedResult) {
        TestCase testCase = TestCase.builder()
                .currentStatus(status)
                .title("testCase")
                .gracePeriodMinutes(20)
                .lastChangeDate(LocalDateTime.now().minus(22, MINUTES))
                .build();
        TestbefundFinding result = FindingContainerMapper.MAPPER.mapOne(testCase);
        assertThat(result.getResult()).isEqualTo(expectedResult);
    }

    @Test
    void shouldMapToUnknown_testIsYoungerThanGracePeriod() {
        TestCase testCase = TestCase.builder()
                .currentStatus(CONFIRM_NEGATIVE)
                .title("testCase")
                .gracePeriodMinutes(20)
                .lastChangeDate(LocalDateTime.now().minus(15, MINUTES))
                .build();
        TestbefundFinding result = FindingContainerMapper.MAPPER.mapOne(testCase);
        assertThat(result.getResult()).isEqualTo(TestbefundFindingResult.UNKNOWN);
    }


}
