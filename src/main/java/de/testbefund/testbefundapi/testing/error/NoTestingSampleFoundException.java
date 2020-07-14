package de.testbefund.testbefundapi.testing.error;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.NOT_FOUND, reason = "Test sample not found.")
public class NoTestingSampleFoundException extends RuntimeException {
}
