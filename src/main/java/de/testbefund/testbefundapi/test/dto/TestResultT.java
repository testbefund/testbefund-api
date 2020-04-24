package de.testbefund.testbefundapi.test.dto;

import io.swagger.annotations.ApiModel;

@ApiModel("TestResult")
public enum  TestResultT {
    UNKNOWN,
    POSITIVE,
    NEGATIVE
}
