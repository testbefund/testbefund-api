package de.testbefund.testbefundapi.test.data;

import de.testbefund.testbefundapi.test.data.TestContainer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TestContainerRepository extends JpaRepository<TestContainer, String> {

    Optional<TestContainer> findByReadId(String readId);
}
