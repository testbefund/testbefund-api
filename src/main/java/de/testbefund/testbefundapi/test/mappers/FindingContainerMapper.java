package de.testbefund.testbefundapi.test.mappers;

import de.testbefund.testbefundapi.generated.api.model.TestbefundFinding;
import de.testbefund.testbefundapi.generated.api.model.TestbefundFindingContainer;
import de.testbefund.testbefundapi.generated.api.model.TestbefundFindingResult;
import de.testbefund.testbefundapi.generated.api.model.TestbefundFindingStatus;
import de.testbefund.testbefundapi.test.data.TestCase;
import de.testbefund.testbefundapi.test.data.TestContainer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;

@Mapper(unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface FindingContainerMapper {

    FindingContainerMapper MAPPER = Mappers.getMapper(FindingContainerMapper.class);

    @Mapping(source = "testCases", target = "findings")
    @Mapping(source = "client", target = "issuer")
    TestbefundFindingContainer mapOne(TestContainer testContainer);

    @Mapping(target = "result", qualifiedByName = "caseToResultMapper", source = "testCase")
    @Mapping(target = "status", qualifiedByName = "caseToStatusMapper", source = "testCase")
    TestbefundFinding mapOne(TestCase testCase);

    default OffsetDateTime mapOne(LocalDateTime localDateTime) {
        return OffsetDateTime.of(localDateTime, OffsetDateTime.now().getOffset());
    }


    @Named("caseToStatusMapper")
    default TestbefundFindingStatus mapStatus(TestCase testCase) {
        boolean hideResult = hideResult(testCase);
        if (hideResult && testCase.getCurrentStatus().isHideable()) {
            return TestbefundFindingStatus.REVIEW_PENDING;
        }
        switch (testCase.getCurrentStatus()) {
            case ISSUED:
                return TestbefundFindingStatus.IN_PROGRESS;
            case CONFIRM_POSITIVE:
                return TestbefundFindingStatus.DONE;
            case CONFIRM_NEGATIVE:
                return TestbefundFindingStatus.DONE;
        }
        return TestbefundFindingStatus.IN_PROGRESS;
    }

    @Named("caseToResultMapper")
    default TestbefundFindingResult mapResult(TestCase testCase) {
        boolean hideResult = hideResult(testCase);
        if (hideResult) {
            return TestbefundFindingResult.UNKNOWN;
        }
        switch (testCase.getCurrentStatus()) {
            case ISSUED:
                return TestbefundFindingResult.UNKNOWN;
            case CONFIRM_POSITIVE:
                return TestbefundFindingResult.POSITIVE;
            case CONFIRM_NEGATIVE:
                return TestbefundFindingResult.NEGATIVE;
        }
        return TestbefundFindingResult.UNKNOWN;
    }

    default boolean hideResult(TestCase testCase) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime lastChangeDate = testCase.getLastChangeDate();
        long minutesBetween = ChronoUnit.MINUTES.between(lastChangeDate, now);
        return minutesBetween <= testCase.getGracePeriodMinutes();
    }
}
