package de.testbefund.testbefundapi.test.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.util.Collection;

@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class CreateTestContainerRequest {
    public Collection<TestToCreate> testRequests;
    /**
     * Optional ID associated with the container. Contains contact information
     */
    public String clientId;
}
