package de.testbefund.testbefundapi.administration;

import de.testbefund.testbefundapi.administration.data.Organization;
import de.testbefund.testbefundapi.administration.data.OrganizationRepository;
import de.testbefund.testbefundapi.generated.api.AdministrationApi;
import de.testbefund.testbefundapi.generated.api.model.TestbefundIssuingOrganization;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import javax.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class OrganizationController implements AdministrationApi {

    private final OrganizationRepository organizationRepository;

    public OrganizationController(OrganizationRepository organizationRepository) {
        this.organizationRepository = organizationRepository;
    }

    @Override
    @Transactional
    public ResponseEntity<TestbefundIssuingOrganization> createOrganization(TestbefundIssuingOrganization testbefundIssuingOrganization) {
        Organization organization = IssuingOrganizationMapper.MAPPER.restoreOne(testbefundIssuingOrganization);

        organization.setId(null);
        Organization savedOrganization = organizationRepository.save(organization);

        return ResponseEntity.ok(IssuingOrganizationMapper.MAPPER.mapOne(savedOrganization));
    }

    @Override
    public ResponseEntity<Void> deleteOrganization(String id) {
        organizationRepository.deleteById(id);

        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<List<TestbefundIssuingOrganization>> getAllOrganizations() {
        List<TestbefundIssuingOrganization> result = organizationRepository.findAll()
            .stream()
            .map(IssuingOrganizationMapper.MAPPER::mapOne)
            .collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    @Override
    @Transactional
    public ResponseEntity<TestbefundIssuingOrganization> getOrganizationById(String id) {
        return ResponseEntity.ok(
                IssuingOrganizationMapper.MAPPER.mapOne(
                        organizationRepository.getOne(id)
                )
        );
    }
}
