package de.testbefund.testbefundapi;

import com.aventrix.jnanoid.jnanoid.NanoIdUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.time.LocalDateTime;
import java.util.function.Supplier;

@SpringBootApplication
public class TestbefundApiApplication {

    @Value("${testbefund.test-id-strength}")
    private int idStrength;

    public static void main(String[] args) {
        SpringApplication.run(TestbefundApiApplication.class, args);
    }


    @Bean(name = "currentDateTimeSupplier")
    public Supplier<LocalDateTime> localDateTimeSupplier() {
        return LocalDateTime::now; // For testability, we expose the current date via the supplier interface.
    }

    @Bean(name = "idProvider")
    public Supplier<String> idProvider() {
        return () -> NanoIdUtils.randomNanoId(NanoIdUtils.DEFAULT_NUMBER_GENERATOR, NanoIdUtils.DEFAULT_ALPHABET, idStrength);
    }
}
