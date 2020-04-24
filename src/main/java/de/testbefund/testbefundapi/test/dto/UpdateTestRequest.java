package de.testbefund.testbefundapi.test.dto;

import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.List;

@ApiModel("UpdateTestRequest")
public class UpdateTestRequest {
    public List<SingleTest> tests = Collections.emptyList();
    public String writeId;

    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SingleTest {
        public String title;
        public TestResultT testResult;
    }
}
