package de.testbefund.testbefundapi.testing.data;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TestingContainerRepository extends JpaRepository<TestingContainer, String> {
    Optional<TestingContainer> findByReadId(String readId);
    Optional<TestingContainer> findByWriteId(String writeId);
}
