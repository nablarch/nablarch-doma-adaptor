package nablarch.integration.doma.batch.ee.listener.integration.app;

import org.seasar.doma.Entity;
import org.seasar.doma.Id;
import org.seasar.doma.Table;

@Entity(immutable = true)
@Table(name = "output")
public class OutputEntity {

    @Id
    public final Integer id;

    public OutputEntity(final Integer id) {
        this.id = id;
    }
}
