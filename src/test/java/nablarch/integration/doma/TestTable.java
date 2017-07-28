package nablarch.integration.doma;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

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
