package nablarch.integration.doma;

import javax.sql.DataSource;

import org.seasar.doma.SingletonConfig;
import org.seasar.doma.jdbc.Config;
import org.seasar.doma.jdbc.JdbcLogger;
import org.seasar.doma.jdbc.Naming;
import org.seasar.doma.jdbc.dialect.Dialect;
import org.seasar.doma.jdbc.tx.LocalTransaction;
import org.seasar.doma.jdbc.tx.LocalTransactionDataSource;
import org.seasar.doma.jdbc.tx.LocalTransactionManager;
import org.seasar.doma.jdbc.tx.TransactionManager;

import nablarch.core.log.basic.LogLevel;
import nablarch.core.util.annotation.Published;

/**
 * Domaを使用してデータベースアクセスを行うための設定を保持するクラス。
 *
 * @author Naoki Yamamoto
 */
@SingletonConfig
public final class DomaConfig implements Config {

    /** シングルトンインスタンス */
    private static final DomaConfig CONFIG = new DomaConfig();

    /** ダイアレクト */
    private final Dialect dialect;

    /** ローカルトランザクションデータソース */
    private final LocalTransactionDataSource localTransactionDataSource;

    /** ローカルトランザクションマネージャ */
    private final LocalTransactionManager localTransactionManager;

    /** ローカルトランザクション */
    private final LocalTransaction localTransaction;

    /** ロガー */
    private final JdbcLogger jdbcLogger;

    /**
     * DBアクセスを行うための設定を持つインスタンスを生成する。
     */
    private DomaConfig() {
        //ローカルトランザクションを取得するときにロガーが必要なので先にフィールドを初期化している
        jdbcLogger = new NablarchJdbcLogger(LogLevel.TRACE);

        final ConfigHolder holder = new ConfigHolder();
        dialect = holder.getDialect();
        localTransactionDataSource = new LocalTransactionDataSource(holder.getDataSource());
        localTransaction = localTransactionDataSource.getLocalTransaction(getJdbcLogger());
        localTransactionManager = new LocalTransactionManager(localTransaction);
    }

    @Override
    public JdbcLogger getJdbcLogger() {
        return jdbcLogger;
    }

    @Override
    public Dialect getDialect() {
        return dialect;
    }

    @Override
    public DataSource getDataSource() {
        return localTransactionDataSource;
    }

    @Override
    @Published
    public TransactionManager getTransactionManager() {
        return localTransactionManager;
    }

    @Override
    public Naming getNaming() {
        return Naming.SNAKE_UPPER_CASE;
    }

    /**
     * シングルトンインスタンスを取得する。
     * @return シングルトンインスタンス
     */
    @Published
    public static DomaConfig singleton() {
        return CONFIG;
    }

    /**
     * ローカルトランザクションを取得する。
     * @return ローカルトランザクション
     */
    public LocalTransaction getLocalTransaction() {
        return localTransaction;
    }
}
