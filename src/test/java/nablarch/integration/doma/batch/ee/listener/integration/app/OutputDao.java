package nablarch.integration.doma.batch.ee.listener.integration.app;

import java.util.List;

import org.seasar.doma.BatchInsert;
import org.seasar.doma.Dao;
import org.seasar.doma.Select;
import org.seasar.doma.jdbc.BatchResult;
import org.seasar.doma.jdbc.SelectOptions;

import nablarch.integration.doma.DomaConfig;

@Dao(config = DomaConfig.class)
public interface OutputDao {

    @BatchInsert
    BatchResult<OutputEntity> batchInsert(List<OutputEntity> entityList);

    @Select
    Integer lockInput(Integer id, SelectOptions selectoptions);
}
