package de.testbefund.testbefundapi.test.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.testbefund.testbefundapi.client.data.Client;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.time.LocalDateTime;

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
    private TestResult currentResult;

    @Enumerated()
    @Column(name = "status")
    private TestStatus currentStatus;

    @Column(name = "last_change_date")
    private LocalDateTime lastChangeDate;

    @Column(name = "icd_code")
    private String icdCode;

    @JoinColumn(name = "client_id")
    @ManyToOne
    private Client client;

    @ManyToOne
    @JsonIgnore // Backreference, don't serialize
    private TestContainer testContainer;
}
