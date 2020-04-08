package de.testbefund.testbefundapi.test.data;

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
    @Column(name = "date")
    private LocalDateTime date;
    @OneToMany(cascade = ALL, orphanRemoval = true, mappedBy = "testContainer")
    private Collection<TestCase> testCases;
}
