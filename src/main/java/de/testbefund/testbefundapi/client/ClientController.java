package de.testbefund.testbefundapi.client;

import de.testbefund.testbefundapi.client.data.Client;
import de.testbefund.testbefundapi.client.data.ClientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping("/client")
public class ClientController {

    private final ClientRepository clientRepository;

    public ClientController(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    @PostMapping(consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @ResponseBody
    public Client createClient(@RequestBody Client client) {
        client.setId(null);
        return clientRepository.save(client);
    }

    @GetMapping(produces = APPLICATION_JSON_VALUE)
    @ResponseBody
    public Collection<Client> getAllClients() {
        return clientRepository.findAll();
    }

    @GetMapping(path = "/{id}", produces = APPLICATION_JSON_VALUE)
    public Client getClientById(@PathVariable("id") String id) {
        return clientRepository.getOne(id);
    }

    @DeleteMapping(path = "/{id}")
    public void deleteClientById(@PathVariable("id") String id) {
        clientRepository.deleteById(id);
    }
}
