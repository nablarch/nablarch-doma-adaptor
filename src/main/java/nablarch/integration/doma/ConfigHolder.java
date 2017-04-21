package nablarch.integration.doma;

import javax.sql.DataSource;

import org.seasar.doma.jdbc.dialect.Dialect;

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
}
