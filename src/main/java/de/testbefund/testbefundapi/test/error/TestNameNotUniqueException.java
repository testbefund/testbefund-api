package de.testbefund.testbefundapi.test.error;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.BAD_REQUEST, reason = "Test case names need to be unique among all test cases.")
public class TestNameNotUniqueException extends RuntimeException {
}
