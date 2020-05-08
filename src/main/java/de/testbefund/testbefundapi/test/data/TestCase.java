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

    @Column(name = "title")
    private String title;

    @Column(name = "grace_period_minutes", nullable = false, columnDefinition = "integer not null default 20")
    @Builder.Default
    private int gracePeriodMinutes = 20;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private TestStageStatus currentStatus;

    @Column(name = "last_change_date")
    private LocalDateTime lastChangeDate;

    @Column(name = "icd_code")
    private String icdCode;

    @ManyToOne
    @JsonIgnore // Backreference, don't serialize
    private TestContainer testContainer;
}
