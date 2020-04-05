package de.testbefund.testbefundapi.test.error;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.NOT_FOUND, reason = "No Test Case Found")
public class NoTestCaseFoundException extends RuntimeException {
}
