package nablarch.integration.doma;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 *
 */
@Entity
@Table(name = "TEST_TABLE")
public class TestTable {

    public TestTable() {
    }

    public TestTable(String name) {
        this.name = name;
    }

    @Id
    @Column(name = "NAME")
    public String name;

    public String getName() {
        return name;
    }
}
