package de.testbefund.testbefundapi.test.dto;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;

public class TestCaseClientT {
    public String name;
    public String address;
    public String telefon;
    public String email;
    public String openingHours;
    public String homepage;
}
