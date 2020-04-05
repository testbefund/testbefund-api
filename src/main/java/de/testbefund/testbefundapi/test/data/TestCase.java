package de.testbefund.testbefundapi.test.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.testbefund.testbefundapi.client.data.Client;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Table(name = "single_test")
public class TestCase {

    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;

    @Column(name = "id_write")
    private String writeId;

    @Column(name = "title")
    private String title;

    @Enumerated()
    @Column(name = "result")
    private TestResult result;

    @Enumerated()
    @Column(name = "status")
    private TestStatus status;

    @JoinColumn(name = "client_id")
    @ManyToOne
    private Client client;

    @ManyToOne
    @JoinColumn(name = "test_container_id")
    @JsonIgnore // Backreference, don't serialize
    private TestContainer testContainer;
}
