package de.testbefund.testbefundapi.test.data;

import de.testbefund.testbefundapi.client.data.Client;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Collection;

import static javax.persistence.CascadeType.ALL;


@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "test_container")
public class TestContainer {
    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;
    @Column(name = "read_id", unique = true, updatable = false)
    private String readId;
    @Column(name = "write_id", unique = true, updatable = false)
    private String writeId;
    @Column(name = "date")
    private LocalDateTime date;
    @JoinColumn(name = "client_id")
    @ManyToOne()
    private Client client;
    @OneToMany(cascade = ALL, orphanRemoval = true)
    @JoinColumn(name = "test_container_id")
    private Collection<TestCase> testCases;
}
