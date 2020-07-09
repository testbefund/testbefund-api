package de.testbefund.testbefundapi.client;

import de.testbefund.testbefundapi.client.data.Client;
import de.testbefund.testbefundapi.generated.api.model.TestbefundIssuingOrganization;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface IssuingOrganizationMapper {

    IssuingOrganizationMapper MAPPER = Mappers.getMapper(IssuingOrganizationMapper.class);

    TestbefundIssuingOrganization mapOne(Client client);
    Client restoreOne(TestbefundIssuingOrganization client);

}
