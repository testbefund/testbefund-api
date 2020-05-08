package de.testbefund.testbefundapi.test;

import de.testbefund.testbefundapi.test.data.TestContainer;
import de.testbefund.testbefundapi.test.dto.*;
import de.testbefund.testbefundapi.test.service.TestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.ResponseEntity.noContent;
import static org.springframework.http.ResponseEntity.notFound;

@RestController
@RequestMapping("/v1/test/")
@EnableWebSecurity
public class TestControllerV1 {

    private final TestService testService;

    public TestControllerV1(TestService testService) {
        this.testService = testService;
    }


    @GetMapping(value = "/auth")
    public ResponseEntity<Void> isAuthenticated() {
        return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "/container", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<TestContainer> createTestContainer(@RequestBody CreateTestContainerRequest request) {
        TestContainer testContainer = testService.createTestContainer(request.testRequests, request.clientId);
        return ResponseEntity.ok(testContainer);
    }

    @GetMapping(value = "/container/{read_id}", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<TestContainerReadT> getTestContainerByReadId(@PathVariable("read_id") String readId) {
        Optional<TestContainer> containerByReadId = testService.getContainerByReadId(readId);
        return containerByReadId
                .map(TestReadMapper.MAPPER::mapOne)
                .map(ResponseEntity::ok)
                .orElse(notFound().build());
    }

    @GetMapping(value = "/container/write/{write_id}")
    public ResponseEntity<TestContainerWriteT> getTestContainerByWriteId(@PathVariable("write_id") String writeId) {
        Optional<TestContainer> containerByReadId = testService.getContainerByWriteId(writeId);
        return containerByReadId
                .map(TestWriteMapper.MAPPER::mapOne)
                .map(ResponseEntity::ok)
                .orElse(notFound().build());
    }

    @PostMapping(value = "/container/{write_id}/testcase/{test_id}/{test_result}")
    public ResponseEntity updateTestByWriteId(@PathVariable("write_id") String writeId, @PathVariable("test_id") String testId, @PathVariable("test_result") TestResultT testResult) {
        testService.updateTestByWriteId(writeId, testId, testResult);
        return noContent().build();
    }

    @PostMapping(value = "/container/batch-update", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<TestContainerWriteT> updateContainerBatch(@RequestBody UpdateTestRequest request) {
        TestContainerWriteT result = TestWriteMapper.MAPPER.mapOne(testService.updateTestContainer(request));
        return ResponseEntity.ok(result);
    }
}
