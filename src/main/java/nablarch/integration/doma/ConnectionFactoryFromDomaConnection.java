package nablarch.integration.doma;

import java.sql.Connection;

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
        final BasicDbConnection connection = new DomaBasedDbConnection(
                JdbcUtil.getConnection(DomaConfig.singleton().getDataSource()));
        initConnection(connection, connectionName);
        return connection;
    }

    /**
     * Domaが保持しているコネクションを使ってデータベースアクセスを実現するクラス。
     */
    private static class DomaBasedDbConnection extends BasicDbConnection {
        /**
         * 指定されたデータ接続を保持するオブジェクトを生成する。
         *
         * @param con データベース接続オブジェクト
         */
        public DomaBasedDbConnection(final Connection con) {
            super(con);
        }

        /**
         * 破棄処理を行う。
         * 
         * この実装では、保持しているステートメントの解放処理のみを行う。
         * データベース接続の解放などは、Doma側で行う必要がある。
         */
        @Override
        public void terminate() {
            closeStatements();
        }
    }
}
