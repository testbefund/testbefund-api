package de.testbefund.testbefundapi.testing;

import de.testbefund.testbefundapi.config.TestSecurityConfig;
import de.testbefund.testbefundapi.generated.api.model.*;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.net.URI;

import static org.assertj.core.api.Assertions.*;

@Import(TestSecurityConfig.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TestingControllerV1Test {

    private RestTemplate restTemplate = new RestTemplate();

    @Autowired
    private OrganizationRepository organizationRepository;

    @LocalServerPort
    private int port;

    private String baseUri() {
        return "http://localhost:" + port + "/v1/testing";
    }

    private String readBaseUri() {
        return "http://localhost:" + port + "/v1";
    }

    private ResponseEntity<TestbefundTestingContainer> createTestingContainer() {
        TestbefundTestingContainerDefinition body =
            new TestbefundTestingContainerDefinition()
                .addTestingDefinitionsItem(new TestbefundTestingDefinition()
                        .icdCode("ICD1234")
                        .title("Test"));
        RequestEntity<TestbefundTestingContainerDefinition> request =
            RequestEntity.post(URI.create(baseUri() + "/container"))
                .header("Authorization", "Basic dGVzdDp0ZXN0") // user=test, password=test
                .body(body);

        return restTemplate.exchange(request, TestbefundTestingContainer.class);
    }

    private TestbefundFindingContainer getFindingContainerByReadId(String readId) {
        TestbefundFindingContainer body =
            restTemplate
                .getForEntity(readBaseUri() + "/finding/" + readId, TestbefundFindingContainer.class)
                .getBody();

        assertThat(body).isNotNull();

        return body;
    }

    @Test
    void Post_Testing_ForUnknownId_ShouldCreateTestingContainer() {
        ResponseEntity<TestbefundTestingContainer> response = createTestingContainer();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
    }

    @Test
    void Get_Finding_ForKnownId_ShouldGetTestingContainerById() {
        TestbefundTestingContainer newTestingContainer = createTestingContainer().getBody();

        assertThat(newTestingContainer).isNotNull();

        TestbefundFindingContainer findingContainer = getFindingContainerByReadId(newTestingContainer.getReadId());

        assertThat(findingContainer).isNotNull();
        assertThat(findingContainer.getFindings().iterator().next().getIcdCode())
            .isEqualTo("ICD1234");
        assertThat(findingContainer.getFindings().iterator().next().getTitle())
            .isEqualTo("Test");
        assertThat(findingContainer.getFindings().iterator().next().getResult())
            .isEqualTo(TestbefundFindingResult.UNKNOWN);
    }

    @Test
    void Post_Testing_ForKnownId_shouldUpdateContainer() {
        TestbefundTestingContainer newTestingContainer = createTestingContainer().getBody();

        assertThat(newTestingContainer).isNotNull();

        TestbefundTesting testingSample = newTestingContainer.getTestingSample().iterator().next();
        String uri = String.format("/container/%s/testcase/%s/%s", newTestingContainer.getWriteId(), testingSample.getId(), TestbefundFindingResult.NEGATIVE);
        ResponseEntity<String> response = restTemplate.postForEntity(baseUri() + uri, null, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        TestbefundFindingContainer readContainer = getFindingContainerByReadId(newTestingContainer.getReadId());
        assertThat(readContainer.getFindings())
                .extracting(TestbefundFinding::getTitle, TestbefundFinding::getResult)
                .containsExactly(Tuple.tuple("Test", TestbefundFindingResult.NEGATIVE));
    }

    @Test
    void shouldReturnNotFound_whenTryingToUpdateNonExistingContainer() {
        TestbefundTestingContainer container = createTestingContainer().getBody();
        assertThat(container).isNotNull();
        String uri = String.format("/testcase/%s/%s", "FOOBAR", TestbefundFindingResult.NEGATIVE);
        assertThatThrownBy(() -> restTemplate.postForEntity(baseUri() + uri, null, String.class))
                .isInstanceOf(HttpClientErrorException.class)
                .hasMessageStartingWith("404");
    }

    @Test
    void shouldNotAllowedUnauthenticatedAccess_toCreateResource() {
        TestbefundTestContainerDefinition createTestContainerRequest = new TestbefundTestContainerDefinition()
                .addTestDefinitionsItem(new TestbefundTestDefinition().title("Title"));
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
        return organizationRepository.saveAndFlush(client);
    }

    private TestbefundTestContainer exchangeForTestContainer(TestbefundTestContainerDefinition createTestContainerRequest) {
        RequestEntity<TestbefundTestContainerDefinition> request = RequestEntity.post(URI.create(baseUri() + "/container"))
                .header("Authorization", "Basic dGVzdDp0ZXN0") // user=test, password=test
                .body(createTestContainerRequest);
        ResponseEntity<TestbefundTestContainer> response = restTemplate.exchange(request, TestbefundTestContainer.class);
        assertThat(response.getBody()).isNotNull();
        return response.getBody();
    }

    @Test
    void shouldGetContainer_byWriteId() {
        TestbefundTestContainer container = createTestingContainer().getBody();
        assertThat(container).isNotNull();
        String uri = String.format("/%s", container.getWriteId());
        ResponseEntity<TestbefundTestContainer> testContainer = restTemplate.getForEntity(baseUri() + uri, TestbefundTestContainer.class);
        assertThat(testContainer.getBody()).isNotNull();
        assertThat(testContainer.getBody().getWriteId()).isEqualTo(container.getWriteId());
        assertThat(testContainer.getBody().getTestCases())
                .extracting(TestbefundTest::getTitle, TestbefundTest::getIcdCode)
                .containsExactly(Tuple.tuple("Test", "ICD1234"));
    }

    @Test
    void shouldUpdateTest_byTitle() {
        TestbefundTestContainer container = createTestingContainer().getBody();
        TestbefundTest testCase = container.getTestCases().iterator().next();
        String url = String.format("%s/container/batch-update", baseUri());
        TestbefundUpdateSingleFinding singleTest = new TestbefundUpdateSingleFinding()
                .testResult(TestbefundFindingResult.POSITIVE)
                .title(testCase.getTitle());
        TestbefundUpdateFindingRequest request = new TestbefundUpdateFindingRequest()
                .addFindingsItem(singleTest)
                .writeId(container.getWriteId());
        ResponseEntity<TestbefundTestContainer> response = restTemplate.postForEntity(url, request, TestbefundTestContainer.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getTestCases())
                .extracting(TestbefundTest::getCurrentStatus, TestbefundTest::getTitle)
                .containsExactly(Tuple.tuple(TestbefundFindingResult.POSITIVE, singleTest.getTitle()));
    }

    @Test
    void shouldCreateWithClient() {
        Client issuer = organizationRepository.saveAndFlush(Client.builder().name("Testclient").build());
        TestbefundTestContainerDefinition createTestContainerRequest = new TestbefundTestContainerDefinition()
                .addTestDefinitionsItem(new TestbefundTestDefinition().title("Test").icdCode("ICD1234"))
                .issuingOrganization(issuer.getId());

        // Verify that the container was created with the client
        TestbefundTestContainer resultingContainer = exchangeForTestContainer(createTestContainerRequest);
        assertThat(resultingContainer.getIssuer().getName()).isEqualTo(issuer.getName());
        assertThat(resultingContainer.getIssuer().getId()).isEqualTo(issuer.getId());

        // Verify that we can actually read the data (TestContainerReadT contains the client)
        TestbefundFindingContainer finding = getFindingContainerByReadId(resultingContainer.getReadId());
        assertThat(finding.getIssuer()).isNotNull();
        assertThat(finding.getIssuer().getName()).isEqualTo(issuer.getName());
    }
}
