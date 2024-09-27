package nablarch.integration.doma;

import org.seasar.doma.Dao;
import org.seasar.doma.Insert;

@Dao
public interface TestTableDao {
    @Insert
    int insert(TestTableForDoma entity);
}
