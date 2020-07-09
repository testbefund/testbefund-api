package de.testbefund.testbefundapi.test;

import de.testbefund.testbefundapi.generated.api.TestApi;
import de.testbefund.testbefundapi.generated.api.model.*;
import de.testbefund.testbefundapi.test.data.TestContainer;
import de.testbefund.testbefundapi.test.mappers.FindingContainerMapper;
import de.testbefund.testbefundapi.test.mappers.TestContainerMapper;
import de.testbefund.testbefundapi.test.service.TestService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

import static org.springframework.http.ResponseEntity.noContent;
import static org.springframework.http.ResponseEntity.notFound;

@RestController
@EnableWebSecurity
public class TestControllerV1 implements TestApi {

    private final TestService testService;

    public TestControllerV1(TestService testService) {
        this.testService = testService;
    }


    @Override
    public ResponseEntity<TestbefundTestContainer> createTestContainer(TestbefundTestContainerDefinition testRequests) {
        TestContainer testContainer = testService.createTestContainer(
                testRequests.getTestDefinitions(),
                testRequests.getIssuingOrganization()
        );
        var result = TestContainerMapper.MAPPER.mapContainer(testContainer);
        return ResponseEntity.ok(result);
    }

    @Override
    public ResponseEntity<TestbefundFindingContainer> getFindingContainer(String readId) {
        Optional<TestContainer> containerByReadId = testService.getContainerByReadId(readId);
        return containerByReadId
                .map(FindingContainerMapper.MAPPER::mapOne)
                .map(ResponseEntity::ok)
                .orElse(notFound().build());
    }

    @Override
    public ResponseEntity<TestbefundTestContainer> getTestContainer(String writeId) {
        Optional<TestContainer> containerByReadId = testService.getContainerByWriteId(writeId);
        return containerByReadId
                .map(TestContainerMapper.MAPPER::mapContainer)
                .map(ResponseEntity::ok)
                .orElse(notFound().build());

    }

    @GetMapping(value = "/auth")
    public ResponseEntity<Void> isAuthenticated() {
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<TestbefundTestContainer> updateContainerBatch(TestbefundUpdateFindingRequest request) {
        TestContainer testContainer = testService.updateTestContainer(request);
        return ResponseEntity.ok(TestContainerMapper.MAPPER.mapContainer(testContainer));
    }

    @Override
    public ResponseEntity<Void> updateTest(String writeId, String testId, TestbefundFindingResult testResult) {
        testService.updateTestByWriteId(writeId, testId, testResult);
        return noContent().build();
    }
}
