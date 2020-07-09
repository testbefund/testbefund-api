package de.testbefund.testbefundapi.test.mappers;

import de.testbefund.testbefundapi.generated.api.model.TestbefundFindingResult;
import de.testbefund.testbefundapi.generated.api.model.TestbefundTestContainer;
import de.testbefund.testbefundapi.test.data.TestContainer;
import de.testbefund.testbefundapi.test.data.TestStageStatus;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface TestContainerMapper {

    TestContainerMapper MAPPER = Mappers.getMapper(TestContainerMapper.class);

    @Mapping(target = "issuer", source = "client")
    TestbefundTestContainer mapContainer(TestContainer testContainer);

    default TestbefundFindingResult mapFinding(TestStageStatus testStageStatus) {
        switch (testStageStatus) {
            case ISSUED:
                return TestbefundFindingResult.UNKNOWN;
            case CONFIRM_NEGATIVE:
                return TestbefundFindingResult.NEGATIVE;
            case CONFIRM_POSITIVE:
                return TestbefundFindingResult.POSITIVE;
        }
        throw new EnumConstantNotPresentException(TestStageStatus.class, testStageStatus.name());
    }
}
