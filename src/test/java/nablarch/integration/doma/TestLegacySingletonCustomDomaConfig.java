package nablarch.integration.doma;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;
import javax.sql.DataSource;

import org.seasar.doma.SingletonConfig;
import org.seasar.doma.jdbc.Config;
import org.seasar.doma.jdbc.dialect.Dialect;
import org.seasar.doma.jdbc.dialect.H2Dialect;

/**
 * プロジェクトが独自に{@link Config}を作成した場合のテスト用{@link Config}
 */
@SingletonConfig
public class TestLegacySingletonCustomDomaConfig implements Config {
    private TestLegacySingletonCustomDomaConfig() {
        // @SingletonConfigアノテーションはprivateなコンストラクタを定義していることを要求する
    }

    @Override
    public DataSource getDataSource() {
        // dummy
        return new DataSource() {
            @Override
            public Connection getConnection() throws SQLException {
                return null;
            }

            @Override
            public Connection getConnection(String s, String s1) throws SQLException {
                return null;
            }

            @Override
            public PrintWriter getLogWriter() throws SQLException {
                return null;
            }

            @Override
            public void setLogWriter(PrintWriter printWriter) throws SQLException {

            }

            @Override
            public void setLoginTimeout(int i) throws SQLException {

            }

            @Override
            public int getLoginTimeout() throws SQLException {
                return 0;
            }

            @Override
            public <T> T unwrap(Class<T> aClass) throws SQLException {
                return null;
            }

            @Override
            public boolean isWrapperFor(Class<?> aClass) throws SQLException {
                return false;
            }

            @Override
            public Logger getParentLogger() throws SQLFeatureNotSupportedException {
                return null;
            }
        };
    }

    @Override
    public Dialect getDialect() {
        return new H2Dialect();
    }

    public static TestLegacySingletonCustomDomaConfig singleton() {
        // @SingletonConfigアノテーションはstaticなsingletonメソッドを実装していることを要求する
        return new TestLegacySingletonCustomDomaConfig();
    }
}
