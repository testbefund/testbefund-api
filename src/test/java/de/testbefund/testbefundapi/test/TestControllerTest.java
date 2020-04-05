package de.testbefund.testbefundapi.test;

import de.testbefund.testbefundapi.test.data.TestCase;
import de.testbefund.testbefundapi.test.data.TestContainer;
import de.testbefund.testbefundapi.test.data.TestResult;
import de.testbefund.testbefundapi.test.dto.CreateTestContainerRequest;
import de.testbefund.testbefundapi.test.dto.TestContainerReadT;
import de.testbefund.testbefundapi.test.dto.TestToCreate;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TestControllerTest {

    private RestTemplate restTemplate = new RestTemplate();

    @LocalServerPort
    private int port;

    private String baseUri() {
        return "http://localhost:" + port + "/test";
    }

    private ResponseEntity<TestContainer> createSampleContainer() {
        CreateTestContainerRequest createTestContainerRequest = new CreateTestContainerRequest();
        createTestContainerRequest.testRequests = List.of(TestToCreate.builder().title("Test").build());
        RequestEntity<CreateTestContainerRequest> request = RequestEntity.post(URI.create(baseUri() + "/container"))
                .header("Authorization", "Basic dGVzdDp0ZXN0") // user=test, password=test
                .body(createTestContainerRequest);
        return restTemplate.exchange(request, TestContainer.class);
    }

    private TestContainerReadT getContainerByReadId(String readId) {
        return restTemplate.getForEntity(baseUri() + "/container/" + readId, TestContainerReadT.class).getBody();
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
    }

    @Test
    void shouldUpdateContainer() {
        TestContainer container = createSampleContainer().getBody();
        assertThat(container).isNotNull();
        String uri = String.format("/testcase/%s/%s", container.getTestCases().iterator().next().getWriteId(), TestResult.NEGATIVE);
        ResponseEntity<String> response = restTemplate.postForEntity(baseUri() + uri, null, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        TestContainerReadT readContainer = getContainerByReadId(container.getReadId());
        assertThat(readContainer.tests)
                .extracting(test -> test.title, test -> test.infected)
                .containsExactly(Tuple.tuple("Test", TestResult.NEGATIVE));
    }

    @Test
    void shouldReturnNotFound_whenTryingToUpdateNonExistingContainer() {
        TestContainer container = createSampleContainer().getBody();
        assertThat(container).isNotNull();
        String uri = String.format("/testcase/%s/%s", "FOOBAR", TestResult.NEGATIVE);
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
}
