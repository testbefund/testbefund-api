package de.testbefund.testbefundapi.testing.error;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.BAD_REQUEST, reason = "Testing sample names need to be unique among all test cases.")
public class TestingSampleNameNotUniqueException extends RuntimeException {
}
