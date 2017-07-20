package nablarch.integration.doma;

import org.seasar.doma.internal.jdbc.util.JdbcUtil;

import nablarch.core.db.connection.BasicDbConnection;
import nablarch.core.db.connection.ConnectionFactorySupport;
import nablarch.core.db.connection.TransactionManagerConnection;

/**
 * Domaで生成したデータベース接続をNablarch用の{@link TransactionManagerConnection}に変換するクラス。
 *
 * @author siosio
 */
public class ConnectionFactoryFromDomaConnection extends ConnectionFactorySupport {

    @Override
    public TransactionManagerConnection getConnection(final String connectionName) {
        final BasicDbConnection connection = new BasicDbConnection(
                JdbcUtil.getConnection(DomaConfig.singleton()
                                                 .getDataSource()));
        initConnection(connection, connectionName);
        return connection;
    }
}
