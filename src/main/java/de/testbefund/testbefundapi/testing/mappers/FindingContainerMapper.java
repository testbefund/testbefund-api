package de.testbefund.testbefundapi.testing.mappers;

import de.testbefund.testbefundapi.testing.data.TestingContainer;
import de.testbefund.testbefundapi.testing.data.TestingSample;
import de.testbefund.testbefundapi.generated.api.model.TestbefundFinding;
import de.testbefund.testbefundapi.generated.api.model.TestbefundFindingContainer;
import de.testbefund.testbefundapi.generated.api.model.TestbefundFindingResult;
import de.testbefund.testbefundapi.generated.api.model.TestbefundFindingStatus;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;

import static de.testbefund.testbefundapi.testing.data.SampleStatus.*;

@Mapper(unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface FindingContainerMapper {

    FindingContainerMapper MAPPER = Mappers.getMapper(FindingContainerMapper.class);

    @Mapping(source = "testingSamples", target = "findings")
    @Mapping(source = "organization", target = "issuer")
    TestbefundFindingContainer mapOne(TestingContainer testingContainer);

    @Mapping(target = "result", qualifiedByName = "sampleToResultMapper", source = "testingSample")
    @Mapping(target = "status", qualifiedByName = "sampleToStatusMapper", source = "testingSample")
    TestbefundFinding mapOne(TestingSample testingSample);

    default OffsetDateTime mapOne(LocalDateTime localDateTime) {
        return OffsetDateTime.of(localDateTime, OffsetDateTime.now().getOffset());
    }

    @Named("sampleToStatusMapper")
    default TestbefundFindingStatus mapStatus(TestingSample testingSample) {
        if (testingSample.getCurrentStatus().isAffectedByGracePeriod() &&
            isWithinGracePeriod(testingSample)
        ) {
            return TestbefundFindingStatus.REVIEW_PENDING;
        }

        switch (testingSample.getCurrentStatus()) {
            case ISSUED:
                return TestbefundFindingStatus.IN_PROGRESS;
            case CONFIRM_POSITIVE:
            case CONFIRM_NEGATIVE:
                return TestbefundFindingStatus.DONE;
        }
        return TestbefundFindingStatus.IN_PROGRESS;
    }

    @Named("sampleToResultMapper")
    default TestbefundFindingResult mapResult(TestingSample testingSample) {
        if (!isWithinGracePeriod(testingSample)) {
            switch (testingSample.getCurrentStatus()) {
                case CONFIRM_POSITIVE:
                    return TestbefundFindingResult.POSITIVE;
                case CONFIRM_NEGATIVE:
                    return TestbefundFindingResult.NEGATIVE;
            }
        }

        return TestbefundFindingResult.UNKNOWN;
    }

    default boolean isWithinGracePeriod(TestingSample testingSample) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime lastChangedAt = testingSample.getLastChangeDateTime();
        long minutesBetween = ChronoUnit.MINUTES.between(lastChangedAt, now);

        return minutesBetween <= testingSample.getGracePeriodMinutes();
    }
}
