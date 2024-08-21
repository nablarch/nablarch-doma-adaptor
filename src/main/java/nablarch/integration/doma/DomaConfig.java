package nablarch.integration.doma;

import javax.sql.DataSource;

import org.seasar.doma.Dao;
import org.seasar.doma.jdbc.Config;
import org.seasar.doma.jdbc.JdbcLogger;
import org.seasar.doma.jdbc.Naming;
import org.seasar.doma.jdbc.dialect.Dialect;
import org.seasar.doma.jdbc.tx.LocalTransaction;
import org.seasar.doma.jdbc.tx.LocalTransactionDataSource;
import org.seasar.doma.jdbc.tx.LocalTransactionManager;
import org.seasar.doma.jdbc.tx.TransactionManager;

import nablarch.core.util.annotation.Published;

/**
 * Domaを使用してデータベースアクセスを行うための設定を保持するクラス。
 *
 * @author Naoki Yamamoto
 */
public final class DomaConfig implements Config {

    /**
     * シングルトンインスタンス({@link Dao}のconfigで指定可能とするため、{@code INSTANCE}という名前で可視性はpublicの必要がある)。
     * 互換性のために存在するフィールドなので、これを直接利用することは推奨しない。
     */
    @Deprecated
    public static final DomaConfig INSTANCE = new DomaConfig();

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

    /** DomaProperties */
    private final DomaStatementProperties domaStatementProperties;

    /**
     * DBアクセスを行うための設定を持つインスタンスを生成する。
     */
    private DomaConfig() {
        final ConfigHolder holder = new ConfigHolder();

        //ローカルトランザクションを取得するときにロガーが必要なので先にフィールドを初期化している
        jdbcLogger = holder.getJdbcLogger();

        dialect = holder.getDialect();
        localTransactionDataSource = new LocalTransactionDataSource(holder.getDataSource());
        localTransaction = localTransactionDataSource.getLocalTransaction(getJdbcLogger());
        localTransactionManager = new LocalTransactionManager(localTransaction);

        domaStatementProperties = holder.getDomaStatementProperties();
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

    @Override
    public int getMaxRows() {
        return domaStatementProperties.getMaxRows();
    }

    @Override
    public int getFetchSize() {
        return domaStatementProperties.getFetchSize();
    }

    @Override
    public int getQueryTimeout() {
        return domaStatementProperties.getQueryTimeout();
    }

    @Override
    public int getBatchSize() {
        return domaStatementProperties.getBatchSize();
    }

    /**
     * シングルトンインスタンスを取得する。
     * @return シングルトンインスタンス
     */
    @Published
    public static DomaConfig singleton() {
        return INSTANCE;
    }

    /**
     * ローカルトランザクションを取得する。
     * @return ローカルトランザクション
     */
    public LocalTransaction getLocalTransaction() {
        return localTransaction;
    }
}
