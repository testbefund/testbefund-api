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
public interface TestWriteMapper {
    TestWriteMapper MAPPER = Mappers.getMapper(TestWriteMapper.class);
    TestContainerWriteT mapOne(TestContainer testContainer);
}
