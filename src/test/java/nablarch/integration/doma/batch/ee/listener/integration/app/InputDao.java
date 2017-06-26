package nablarch.integration.doma.batch.ee.listener.integration.app;

import java.util.stream.Stream;

import org.seasar.doma.Dao;
import org.seasar.doma.Select;
import org.seasar.doma.SelectType;
import org.seasar.doma.Suppress;
import org.seasar.doma.Update;
import org.seasar.doma.message.Message;

import nablarch.integration.doma.DomaTransactionNotSupportedConfig;

@Dao(config = DomaTransactionNotSupportedConfig.class)
public interface InputDao {

    @Select(strategy = SelectType.RETURN)
    @Suppress(messages = Message.DOMA4274)
    Stream<Integer> find();
    
    @Update(sqlFile = true)
    int update10Times();
}
