package nablarch.integration.doma;

import javax.sql.DataSource;

import nablarch.core.repository.SystemRepository;
import nablarch.core.util.annotation.Published;
import org.seasar.doma.SingletonConfig;
import org.seasar.doma.jdbc.Config;
import org.seasar.doma.jdbc.Naming;
import org.seasar.doma.jdbc.dialect.Dialect;
import org.seasar.doma.jdbc.tx.LocalTransactionDataSource;
import org.seasar.doma.jdbc.tx.LocalTransactionManager;
import org.seasar.doma.jdbc.tx.TransactionManager;

/**
 * Domaを使用してデータベースアクセスを行うための設定を保持するクラス。
 *
 * @author Naoki Yamamoto
 */
@SingletonConfig
public final class DomaConfig implements Config {

    /** シングルトンインスタンス */
    private static final DomaConfig CONFIG = new DomaConfig();

    /** {@link SystemRepository}に定義されているダイアレクト名 */
    private static final String DIALECT_NAME = "dialect";

    /** {@link SystemRepository}に定義されているデータソース名 */
    private static final String DATA_SOURCE_NAME = "dataSource";

    /** ダイアレクト */
    private final Dialect dialect;

    /** ローカルトランザクションデータソース */
    private final LocalTransactionDataSource localTransactionDataSource;

    /** ローカルトランザクションマネージャ */
    private final LocalTransactionManager localTransactionManager;

    /**
     * DBアクセスを行うための設定を持つインスタンスを生成する。
     */
    private DomaConfig() {
        dialect = SystemRepository.get(DIALECT_NAME);
        if (dialect == null) {
            throw new IllegalArgumentException("specified "+ DIALECT_NAME +" is not registered in SystemRepository.");
        }

        final DataSource dataSource = SystemRepository.get(DATA_SOURCE_NAME);
        if (dataSource == null) {
            throw new IllegalArgumentException("specified " + DATA_SOURCE_NAME + " is not registered in SystemRepository.");
        }
        localTransactionDataSource = new LocalTransactionDataSource(dataSource);

        localTransactionManager = new LocalTransactionManager(
                localTransactionDataSource.getLocalTransaction(getJdbcLogger()));
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


}
