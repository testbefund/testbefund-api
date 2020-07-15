package de.testbefund.testbefundapi.administration;

import de.testbefund.testbefundapi.administration.data.Organization;
import de.testbefund.testbefundapi.generated.api.model.TestbefundIssuingOrganization;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface IssuingOrganizationMapper {
    IssuingOrganizationMapper MAPPER = Mappers.getMapper(IssuingOrganizationMapper.class);

    TestbefundIssuingOrganization mapOne(Organization organization);
    Organization restoreOne(TestbefundIssuingOrganization organization);
}
