package de.testbefund.testbefundapi.testing.dto;

import de.testbefund.testbefundapi.generated.api.model.TestbefundFinding;
import de.testbefund.testbefundapi.generated.api.model.TestbefundFindingResult;
import de.testbefund.testbefundapi.testing.data.SampleStatus;
import de.testbefund.testbefundapi.testing.data.TestingSample;
import de.testbefund.testbefundapi.testing.mappers.FindingContainerMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.LocalDateTime;
import java.util.stream.Stream;

import static java.time.temporal.ChronoUnit.MINUTES;
import static org.assertj.core.api.Assertions.assertThat;

class TestingMapperReadTest {
    static Stream<Arguments> statusTestArguments() {
        return Stream.of(
                Arguments.of(SampleStatus.CONFIRM_NEGATIVE, TestbefundFindingResult.NEGATIVE),
                Arguments.of(SampleStatus.CONFIRM_POSITIVE, TestbefundFindingResult.POSITIVE),
                Arguments.of(SampleStatus.ISSUED, TestbefundFindingResult.UNKNOWN)
        );
    }

    @ParameterizedTest
    @MethodSource("statusTestArguments")
    void MapOne_ShouldMap_SampleOutOfGracePeriod_To_Finding(SampleStatus status, TestbefundFindingResult expectedFindingResult) {
        TestingSample sample = TestingSample.builder()
            .currentStatus(status)
            .title("sample")
            .gracePeriodMinutes(20)
            .lastChangeDateTime(LocalDateTime.now().minus(21, MINUTES))
            .build();

        TestbefundFinding finding = FindingContainerMapper.MAPPER.mapOne(sample);

        assertThat(finding.getResult()).isEqualTo(expectedFindingResult);
    }

    @Test
    void MapOne_ShouldMap_SampleWithinGracePeriod_To_Unknown() {
        TestingSample testingSample = TestingSample.builder()
            .currentStatus(SampleStatus.CONFIRM_NEGATIVE)
            .title("testingSample")
            .gracePeriodMinutes(20)
            .lastChangeDateTime(LocalDateTime.now().minus(20, MINUTES))
            .build();

        TestbefundFinding finding = FindingContainerMapper.MAPPER.mapOne(testingSample);

        assertThat(finding.getResult()).isEqualTo(TestbefundFindingResult.UNKNOWN);
    }


}
