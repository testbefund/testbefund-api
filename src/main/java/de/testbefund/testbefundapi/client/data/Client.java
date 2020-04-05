package de.testbefund.testbefundapi.client.data;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;


@Data
@Entity
@NoArgsConstructor
@Table(name = "client")
public class Client {
    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;
    @Column(name = "name")
    private String name;
    @Column(name = "address")
    private String address;
    @Column(name = "phone")
    private String telefon;
    @Column(name = "email")
    private String email;
    @Column(name = "opening_hours")
    private String openingHours;
    @Column(name = "homepage")
    private String homepage;
}
