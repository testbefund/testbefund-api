package de.testbefund.testbefundapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.time.LocalDateTime;
import java.util.function.Supplier;

@SpringBootApplication
public class TestbefundApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(TestbefundApiApplication.class, args);
    }


    @Bean(name = "currentDateSupplier")
    public Supplier<LocalDateTime> localDateSupplier() {
        return LocalDateTime::now; // For testability, we expose the current date via the supplier interface.
    }
}
