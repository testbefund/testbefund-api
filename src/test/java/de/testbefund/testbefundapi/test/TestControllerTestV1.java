package de.testbefund.testbefundapi.test;

import de.testbefund.testbefundapi.client.data.Client;
import de.testbefund.testbefundapi.client.data.ClientRepository;
import de.testbefund.testbefundapi.config.TestSecurityConfig;
import de.testbefund.testbefundapi.test.data.TestCase;
import de.testbefund.testbefundapi.test.data.TestContainer;
import de.testbefund.testbefundapi.test.data.TestStageStatus;
import de.testbefund.testbefundapi.test.dto.*;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@Import(TestSecurityConfig.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TestControllerTestV1 {

    private RestTemplate restTemplate = new RestTemplate();

    @Autowired
    private ClientRepository clientRepository;

    @LocalServerPort
    private int port;

    private String baseUri() {
        return "http://localhost:" + port + "/v1/test";
    }

    private ResponseEntity<TestContainer> createSampleContainer() {
        CreateTestContainerRequest createTestContainerRequest = new CreateTestContainerRequest();
        createTestContainerRequest.testRequests = List.of(TestToCreate.builder().title("Test").icdCode("ICD1234").build());
        RequestEntity<CreateTestContainerRequest> request = RequestEntity.post(URI.create(baseUri() + "/container"))
                .header("Authorization", "Basic dGVzdDp0ZXN0") // user=test, password=test
                .body(createTestContainerRequest);
        return restTemplate.exchange(request, TestContainer.class);
    }

    private TestContainerReadT getContainerByReadId(String readId) {
        TestContainerReadT body = restTemplate.getForEntity(baseUri() + "/container/" + readId, TestContainerReadT.class).getBody();
        assertThat(body).isNotNull();
        return body;
    }

    @Test
    void shouldCreateTestContainer() {
        ResponseEntity<TestContainer> response = createSampleContainer();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
    }

    @Test
    void shouldGetTheTestContainer_byReadId() {
        TestContainer container = createSampleContainer().getBody();
        assertThat(container).isNotNull();
        TestContainerReadT readContainer = getContainerByReadId(container.getReadId());
        assertThat(readContainer).isNotNull();
        assertThat(readContainer.tests.iterator().next().icd_code).isEqualTo("ICD1234");
        assertThat(readContainer.tests.iterator().next().title).isEqualTo("Test");
        assertThat(readContainer.tests.iterator().next().infected).isEqualTo(TestResultT.UNKNOWN);
    }

    @Test
    void shouldUpdateContainer() {
        TestContainer container = createSampleContainer().getBody();
        assertThat(container).isNotNull();
        TestCase testCase = container.getTestCases().iterator().next();
        String uri = String.format("/container/%s/testcase/%s/%s", container.getWriteId(), testCase.getId(), TestResultT.NEGATIVE);
        ResponseEntity<String> response = restTemplate.postForEntity(baseUri() + uri, null, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        TestContainerReadT readContainer = getContainerByReadId(container.getReadId());
        assertThat(readContainer.tests)
                .extracting(test -> test.title, test -> test.infected)
                .containsExactly(Tuple.tuple("Test", TestResultT.NEGATIVE));
    }

    @Test
    void shouldReturnNotFound_whenTryingToUpdateNonExistingContainer() {
        TestContainer container = createSampleContainer().getBody();
        assertThat(container).isNotNull();
        String uri = String.format("/testcase/%s/%s", "FOOBAR", TestResultT.NEGATIVE);
        assertThatThrownBy(() -> restTemplate.postForEntity(baseUri() + uri, null, String.class))
                .isInstanceOf(HttpClientErrorException.class)
                .hasMessageStartingWith("404");
    }

    @Test
    void shouldNotAllowedUnauthenticatedAccess_toCreateResource() {
        CreateTestContainerRequest createTestContainerRequest = new CreateTestContainerRequest();
        createTestContainerRequest.testRequests = List.of(TestToCreate.builder().title("Title").build());
        HttpClientErrorException exception = catchThrowableOfType(() -> restTemplate.postForEntity(baseUri() + "/container", createTestContainerRequest, TestContainer.class), HttpClientErrorException.class);
        assertThat(exception).isNotNull();
        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    private Client createSampleClient() {
        Client client = Client.builder().name("Test")
                .address("Testweg 1")
                .email("test@test.test")
                .telefon("0123456789")
                .homepage("home.page")
                .openingHours("10-19h")
                .build();
        return clientRepository.saveAndFlush(client);
    }

    private TestContainer exchangeForTestContainer(CreateTestContainerRequest createTestContainerRequest) {
        RequestEntity<CreateTestContainerRequest> request = RequestEntity.post(URI.create(baseUri() + "/container"))
                .header("Authorization", "Basic dGVzdDp0ZXN0") // user=test, password=test
                .body(createTestContainerRequest);
        ResponseEntity<TestContainer> response =  restTemplate.exchange(request, TestContainer.class);
        assertThat(response.getBody()).isNotNull();
        return response.getBody();
    }

    @Test
    void shouldGetContainer_byWriteId() {
        TestContainer container = createSampleContainer().getBody();
        assertThat(container).isNotNull();
        String uri = String.format("/container/write/%s", container.getWriteId());
        ResponseEntity<TestContainerWriteT> testContainer = restTemplate.getForEntity(baseUri() + uri, TestContainerWriteT.class);
        assertThat(testContainer.getBody()).isNotNull();
        assertThat(testContainer.getBody().writeId).isEqualTo(container.getWriteId());
        assertThat(testContainer.getBody().testCases)
                .extracting(testCase -> testCase.title, testCase -> testCase.icdCode)
                .containsExactly(Tuple.tuple("Test", "ICD1234"));
    }

    @Test
    void shouldUpdateTest_byTitle() {
        TestContainer container = createSampleContainer().getBody();
        TestCase testCase = container.getTestCases().iterator().next();
        String url = String.format("%s/container/batch-update", baseUri());
        UpdateTestRequest.SingleTest singleTest = new UpdateTestRequest.SingleTest();
        singleTest.testResult = TestResultT.POSITIVE;
        singleTest.title = testCase.getTitle();
        UpdateTestRequest request = new UpdateTestRequest();
        request.tests = List.of(singleTest);
        request.writeId = container.getWriteId();
        ResponseEntity<TestContainerWriteT> response = restTemplate.postForEntity(url, request, TestContainerWriteT.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().testCases)
                .extracting(testCaseWriteT -> testCaseWriteT.currentStatus, testCaseWriteT -> testCaseWriteT.title)
                .containsExactly(Tuple.tuple(TestStageStatus.CONFIRM_POSITIVE, singleTest.title));
    }

    @Test
    void shouldCreateWithClient() {
        Client client = clientRepository.saveAndFlush(Client.builder().name("Testclient").build());
        CreateTestContainerRequest createTestContainerRequest = new CreateTestContainerRequest();
        TestToCreate testToCreate = TestToCreate.builder().title("Test").icdCode("ICD1234").build();
        createTestContainerRequest.testRequests = List.of(testToCreate);
        createTestContainerRequest.clientId = client.getId();

        // Verify that the container was created with the client
        TestContainer resultingContainer = exchangeForTestContainer(createTestContainerRequest);
        assertThat(resultingContainer.getClient().getName()).isEqualTo(client.getName());
        assertThat(resultingContainer.getClient().getId()).isEqualTo(client.getId());

        // Verify that we can actually read the data (TestContainerReadT contains the client)
        ResponseEntity<TestContainerReadT> readContainer = restTemplate.getForEntity(baseUri() + "/container/" + resultingContainer.getReadId(), TestContainerReadT.class);
        assertThat(readContainer.getBody()).isNotNull();
        assertThat(readContainer.getBody().client).isNotNull();
        assertThat(readContainer.getBody().client.name).isEqualTo(client.getName());
    }
}
