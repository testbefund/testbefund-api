package de.testbefund.testbefundapi.testing;

import de.testbefund.testbefundapi.generated.api.AdministrationApi;
import de.testbefund.testbefundapi.generated.api.model.*;
import de.testbefund.testbefundapi.testing.data.TestingContainer;
import de.testbefund.testbefundapi.testing.mappers.FindingContainerMapper;
import de.testbefund.testbefundapi.testing.mappers.TestingContainerMapper;
import de.testbefund.testbefundapi.testing.service.TestingService;
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
public class TestingControllerV1 implements AdministrationApi {

    private final TestingService testingService;

    public TestingControllerV1(TestingService testingService) {
        this.testingService = testingService;
    }


    @Override
    public ResponseEntity<TestbefundTestingContainer> createTestingContainer(
        TestbefundTestingContainerDefinition testingRequests
    ) {
        TestingContainer testingContainer = testService.createTestingContainer(
                testingRequests.getTestingDefinitions(),
                testingRequests.getIssuingOrganization()
        );
        var result = TestingContainerMapper.MAPPER.mapContainer(testingContainer);
        return ResponseEntity.ok(result);
    }

    @Override
    public ResponseEntity<TestbefundFindingContainer> getFindingContainer(String readId) {
        Optional<TestingContainer> containerByReadId = testingService.getContainerByReadId(readId);
        return containerByReadId
                .map(FindingContainerMapper.MAPPER::mapOne)
                .map(ResponseEntity::ok)
                .orElse(notFound().build());
    }

    @Override
    public ResponseEntity<TestbefundTestingContainer> getTestingContainer(String writeId) {
        Optional<TestingContainer> containerByReadId = testingService.getContainerByWriteId(writeId);
        return containerByReadId
                .map(TestingContainerMapper.MAPPER::mapContainer)
                .map(ResponseEntity::ok)
                .orElse(notFound().build());

    }

    @GetMapping(value = "/auth")
    public ResponseEntity<Void> isAuthenticated() {
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<TestbefundTestingContainer> updateContainerBatch(TestbefundUpdateFindingRequest request) {
        TestingContainer testingContainer = testService.updateTestingContainer(request);
        return ResponseEntity.ok(TestingContainerMapper.MAPPER.mapContainer(testingContainer));
    }

    @Override
    public ResponseEntity<Void> updateTest(String writeId, String testId, TestbefundFindingResult testingResult) {
        testingService.updateTestingByWriteId(writeId, testId, testingResult);
        return noContent().build();
    }
}
