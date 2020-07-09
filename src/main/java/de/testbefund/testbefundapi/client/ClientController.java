package de.testbefund.testbefundapi.client;

import de.testbefund.testbefundapi.client.data.Client;
import de.testbefund.testbefundapi.client.data.ClientRepository;
import de.testbefund.testbefundapi.generated.api.OrganizationApi;
import de.testbefund.testbefundapi.generated.api.model.TestbefundIssuingOrganization;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import javax.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class ClientController implements OrganizationApi {

    private final ClientRepository clientRepository;

    public ClientController(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    @Override
    @Transactional
    public ResponseEntity<TestbefundIssuingOrganization> createOrganization(TestbefundIssuingOrganization testbefundIssuingOrganization) {
        Client client = IssuingOrganizationMapper.MAPPER.restoreOne(testbefundIssuingOrganization);
        client.setId(null);
        Client saved = clientRepository.save(client);
        return ResponseEntity.ok(IssuingOrganizationMapper.MAPPER.mapOne(saved));
    }

    @Override
    public ResponseEntity<Void> deleteOrganization(String id) {
        clientRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<List<TestbefundIssuingOrganization>> getAllOrganizations() {
        List<TestbefundIssuingOrganization> result = clientRepository.findAll()
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
                        clientRepository.getOne(id)
                )
        );
    }
}
