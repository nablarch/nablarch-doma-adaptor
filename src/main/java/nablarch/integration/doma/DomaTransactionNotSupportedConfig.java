package nablarch.integration.doma;

import javax.sql.DataSource;

import org.seasar.doma.SingletonConfig;
import org.seasar.doma.jdbc.Config;
import org.seasar.doma.jdbc.JdbcLogger;
import org.seasar.doma.jdbc.Naming;
import org.seasar.doma.jdbc.dialect.Dialect;

import nablarch.core.log.basic.LogLevel;
import nablarch.core.util.annotation.Published;

/**
 * Domaを使用してトランザクションを使用せずデータベースアクセスを行うための設定を保持するクラス。
 * <p>
 * トランザクションを使用しないため、全てのデータベースアクセス処理が自動コミットされる。
 * 本クラスを適用した{@link org.seasar.doma.Dao}クラスで行うデータベースへの変更は、十分注意すること。
 *
 * @author siosio
 */
@SingletonConfig
public final class DomaTransactionNotSupportedConfig implements Config {

    /** シングルトンインスタンス */
    private static final DomaTransactionNotSupportedConfig CONFIG = new DomaTransactionNotSupportedConfig();

    /** ダイアレクト */
    private final Dialect dialect;

    /** データソース*/
    private final DataSource dataSource;

    /** ロガー */
    private final JdbcLogger jdbcLogger;

    /**
     * DBアクセスを行うための設定を持つインスタンスを生成する。
     */
    private DomaTransactionNotSupportedConfig() {
        final ConfigHolder holder = new ConfigHolder();
        dialect = holder.getDialect();
        dataSource = holder.getDataSource();
        jdbcLogger = new NablarchJdbcLogger(LogLevel.TRACE);
    }

    @Override
    public Dialect getDialect() {
        return dialect;
    }

    @Override
    public DataSource getDataSource() {
        return dataSource;
    }

    @Override
    public JdbcLogger getJdbcLogger() {
        return jdbcLogger;
    }

    @Override
    public Naming getNaming() {
        return Naming.SNAKE_UPPER_CASE;
    }

    /**
     * シングルトンインスタンスを取得する。
     *
     * @return シングルトンインスタンス
     */
    @Published
    public static DomaTransactionNotSupportedConfig singleton() {
        return CONFIG;
    }
}
