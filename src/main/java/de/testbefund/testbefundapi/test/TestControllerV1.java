package de.testbefund.testbefundapi.test;

import de.testbefund.testbefundapi.test.data.TestContainer;
import de.testbefund.testbefundapi.test.dto.CreateTestContainerRequest;
import de.testbefund.testbefundapi.test.dto.TestContainerReadT;
import de.testbefund.testbefundapi.test.dto.TestReadMapper;
import de.testbefund.testbefundapi.test.dto.TestResultT;
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
@CrossOrigin(origins = "*") // For now we'll allow "*"
@RequestMapping("/v1/test/")
@EnableWebSecurity
public class TestControllerV1 {

    private final TestService testService;

    public TestControllerV1(TestService testService) {
        this.testService = testService;
    }

    @PostMapping(value = "/container", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<TestContainer> createTestContainer(@RequestBody CreateTestContainerRequest request) {
        TestContainer testContainer = testService.createTestContainer(request.testRequests);
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

    @PostMapping(value = "/testcase/{write_id}/{test_result}")
    public ResponseEntity updateTestByWriteId(@PathVariable("write_id") String writeId, @PathVariable("test_result") TestResultT testResult) {
        testService.updateTestByWriteId(writeId, testResult);
        return noContent().build();
    }
}
