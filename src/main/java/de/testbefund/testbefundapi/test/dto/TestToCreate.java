package de.testbefund.testbefundapi.test.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TestToCreate {
    public String title;
    public String clientId;
}
