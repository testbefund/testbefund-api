package de.testbefund.testbefundapi.test.data;

import de.testbefund.testbefundapi.test.data.TestCase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TestRepository extends JpaRepository<TestCase, String> {
    Optional<TestCase> findByWriteId(String writeId);
}
