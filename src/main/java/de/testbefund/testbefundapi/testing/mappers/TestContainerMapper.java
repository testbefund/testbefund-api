package de.testbefund.testbefundapi.testing.mappers;

import de.testbefund.testbefundapi.generated.api.model.TestbefundFindingResult;
import de.testbefund.testbefundapi.generated.api.model.TestbefundTestContainer;
import de.testbefund.testbefundapi.test.data.TestContainer;
import de.testbefund.testbefundapi.test.data.TestStageStatus;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface TestingContainerMapper {

    TestingContainerMapper MAPPER = Mappers.getMapper(TestingContainerMapper.class);

    @Mapping(target = "issuer", source = "client")
    TestbefundTestingContainer mapContainer(TestingContainer testingContainer);

    default TestbefundFindingResult mapFinding(TestingStageStatus testingStageStatus) {
        switch (testingStageStatus) {
            case ISSUED:
                return TestbefundFindingResult.UNKNOWN;
            case CONFIRM_NEGATIVE:
                return TestbefundFindingResult.NEGATIVE;
            case CONFIRM_POSITIVE:
                return TestbefundFindingResult.POSITIVE;
        }

        throw new EnumConstantNotPresentException(TestingStageStatus.class, testingStageStatus.name());
    }
}
