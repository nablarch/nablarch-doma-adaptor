package nablarch.integration.doma.listener;

import org.seasar.doma.Dao;
import org.seasar.doma.Insert;

import nablarch.integration.doma.DomaConfig;

@Dao(config = DomaConfig.class)
public interface TestDomaEntityDao {
    @Insert
    int insert(TestDomaEntity entity);
}
