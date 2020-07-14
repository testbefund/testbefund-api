package de.testbefund.testbefundapi.testing.mappers;

import de.testbefund.testbefundapi.generated.api.model.TestbefundFindingResult;
import de.testbefund.testbefundapi.generated.api.model.TestbefundTestingContainer;
import de.testbefund.testbefundapi.testing.data.TestingContainer;
import de.testbefund.testbefundapi.testing.data.SampleStatus;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface TestingContainerMapper {

    TestingContainerMapper MAPPER = Mappers.getMapper(TestingContainerMapper.class);

    @Mapping(target = "issuer", source = "organization")
    TestbefundTestingContainer mapContainer(TestingContainer testingContainer);

    default TestbefundFindingResult mapFinding(SampleStatus sampleStatus) {
        switch (sampleStatus) {
            case ISSUED:
                return TestbefundFindingResult.UNKNOWN;
            case CONFIRM_NEGATIVE:
                return TestbefundFindingResult.NEGATIVE;
            case CONFIRM_POSITIVE:
                return TestbefundFindingResult.POSITIVE;
        }

        throw new EnumConstantNotPresentException(SampleStatus.class, sampleStatus.name());
    }
}
