package nablarch.integration.doma;

import javax.sql.DataSource;

import org.seasar.doma.jdbc.JdbcLogger;
import org.seasar.doma.jdbc.dialect.Dialect;

import nablarch.core.log.basic.LogLevel;
import nablarch.core.repository.SystemRepository;

/**
 * Domaに必要な設定を保持するクラス。
 *
 * @author siosio
 */
public class ConfigHolder {

    /** {@link SystemRepository}に定義されているDomaのダイアレクト名 */
    private static final String DIALECT_NAME = "domaDialect";

    /** {@link SystemRepository}に定義されているデータソース名 */
    private static final String DATA_SOURCE_NAME = "dataSource";

    /** {@link SystemRepository}に定義されているDomaのJdbcLogger名 */
    private static final String JDBC_LOGGER_NAME = "domaJdbcLogger";

    /** {@link SystemRepository}に定義されているDomaのStatementに関する設定名 */
    private static final String STATEMENT_PROPERTIES_NAME = "domaStatementProperties";

    /**
     * Domaが使用する{@link Dialect}を返す。
     *
     * @return Dialect
     */
    public Dialect getDialect() {
        final Dialect dialect = SystemRepository.get(DIALECT_NAME);
        if (dialect == null) {
            throw new IllegalArgumentException("specified " + DIALECT_NAME + " is not registered in SystemRepository.");
        }
        return dialect;
    }

    /**
     * Domaが使用する{@link DataSource}を返す。
     *
     * @return DataSource
     */
    public DataSource getDataSource() {
        final DataSource dataSource = SystemRepository.get(DATA_SOURCE_NAME);
        if (dataSource == null) {
            throw new IllegalArgumentException(
                    "specified " + DATA_SOURCE_NAME + " is not registered in SystemRepository.");
        }
        return dataSource;
    }

    /**
     * Domaが使用する{@link JdbcLogger}を返す。
     * 
     * {@link SystemRepository}に定義されていない場合は{@link NablarchJdbcLogger}を返す。
     *
     * @return JdbcLogger
     */
    public JdbcLogger getJdbcLogger() {
        final JdbcLogger jdbcLogger = SystemRepository.get(JDBC_LOGGER_NAME);
        if (jdbcLogger == null) {
            return new NablarchJdbcLogger(LogLevel.TRACE);
        }
        return jdbcLogger;
    }

    /**
     * {@link DomaStatementProperties}を返す。
     * 
     * {@link SystemRepository}に定義されていない場合は単純にインスタンス化した{@link DomaStatementProperties}を返す。
     *
     * @return DomaStatementProperties
     */
    public DomaStatementProperties getDomaStatementProperties() {
        final DomaStatementProperties properties = SystemRepository.get(STATEMENT_PROPERTIES_NAME);
        if (properties == null) {
            return new DomaStatementProperties();
        }
        return properties;
    }
}
