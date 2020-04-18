package de.testbefund.testbefundapi.test.dto;

import de.testbefund.testbefundapi.test.data.TestCase;
import de.testbefund.testbefundapi.test.data.TestContainer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Maps the Read-Only part of the test cases to DTOs
 */
@Mapper
public interface TestReadMapper {

    TestReadMapper MAPPER = Mappers.getMapper(TestReadMapper.class);

    @Mapping(source = "readId", target = "uuid_read")
    @Mapping(source = "testCases", target = "tests")
    TestContainerReadT mapOne(TestContainer testContainer);

    @Mapping(target = "infected", qualifiedByName = "caseToResultMapper", source = "testCase")
    @Mapping(target = "status", qualifiedByName = "caseToStatusMapper",  source = "testCase")
    @Mapping(source = "icdCode", target = "icd_code")
    TestCaseReadT mapOne(TestCase testCase);

    @Named("caseToStatusMapper")
    default TestStatusT mapStatus(TestCase testCase) {
        switch (testCase.getCurrentStatus()) {
            case ISSUED:
                return TestStatusT.IN_PROGRESS;
            case CONFIRM_POSITIVE:
                return TestStatusT.DONE;
            case CONFIRM_NEGATIVE:
                return TestStatusT.DONE;
        }
        return TestStatusT.IN_PROGRESS;
    }

    @Named("caseToResultMapper")
    default TestResultT mapResult(TestCase testCase) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime lastChangeDate = testCase.getLastChangeDate();
        long minutesBetween = ChronoUnit.MINUTES.between(lastChangeDate, now);
        boolean hideResult = minutesBetween <= testCase.getGracePeriodMinutes();
        if (hideResult) {
            return TestResultT.UNKNOWN;
        }
        switch (testCase.getCurrentStatus()) {
            case ISSUED:
                return TestResultT.UNKNOWN;
            case CONFIRM_POSITIVE:
                return TestResultT.POSITIVE;
            case CONFIRM_NEGATIVE:
                return TestResultT.NEGATIVE;
        }
        return TestResultT.UNKNOWN;
    }
}
