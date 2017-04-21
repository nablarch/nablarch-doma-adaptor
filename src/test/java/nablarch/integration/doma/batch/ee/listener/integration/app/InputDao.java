package nablarch.integration.doma.batch.ee.listener.integration.app;

import java.util.stream.Stream;

import org.seasar.doma.Dao;
import org.seasar.doma.Select;
import org.seasar.doma.SelectType;

import nablarch.integration.doma.DomaTransactionNotSupportedConfig;

@Dao(config = DomaTransactionNotSupportedConfig.class)
public interface InputDao {

    @Select(strategy = SelectType.RETURN)
    Stream<Integer> find();
}
