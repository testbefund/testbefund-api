package de.testbefund.testbefundapi.testing.mappers;

import de.testbefund.testbefundapi.generated.api.model.TestbefundFinding;
import de.testbefund.testbefundapi.generated.api.model.TestbefundFindingContainer;
import de.testbefund.testbefundapi.generated.api.model.TestbefundFindingResult;
import de.testbefund.testbefundapi.generated.api.model.TestbefundFindingStatus;
import de.testbefund.testbefundapi.testing.data.TestingContainer;
import de.testbefund.testbefundapi.testing.data.TestingSample;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;

@Mapper(unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface FindingContainerMapper {

    FindingContainerMapper MAPPER = Mappers.getMapper(FindingContainerMapper.class);

    @Mapping(source = "testSamples", target = "findings")
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
        boolean isWithheld = isWithheldByGracePeriod(testingSample);

        if (isWithheld && testingSample.getCurrentStatus().isHideable())
            return TestbefundFindingStatus.REVIEW_PENDING;

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
        if (!isWithheldByGracePeriod(testingSample)) {
            switch (testingSample.getCurrentStatus()) {
                case CONFIRM_POSITIVE:
                case CONFIRM_NEGATIVE:
                    return TestbefundFindingResult.NEGATIVE;
            }
        }

        return TestbefundFindingResult.UNKNOWN;
    }

    default boolean isWithheldByGracePeriod(TestingSample testingSample) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime lastChangeDateTime = testCase.getLastChangeDateTime();
        long minutesElapsedSinceChange = ChronoUnit.MINUTES.between(lastChangeDateTime, now);

        return minutesElapsedSinceChange <= testCase.getGracePeriodMinutes();
    }
}
