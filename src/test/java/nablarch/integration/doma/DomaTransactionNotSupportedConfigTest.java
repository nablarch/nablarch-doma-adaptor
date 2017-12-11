package nablarch.integration.doma;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSource;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.seasar.doma.jdbc.JdbcLogger;
import org.seasar.doma.jdbc.Naming;
import org.seasar.doma.jdbc.UtilLoggingJdbcLogger;
import org.seasar.doma.jdbc.dialect.Dialect;
import org.seasar.doma.jdbc.dialect.H2Dialect;

import mockit.Deencapsulation;
import nablarch.test.support.SystemRepositoryResource;

/**
 * {@link DomaTransactionNotSupportedConfig}のテスト。
 */
public class DomaTransactionNotSupportedConfigTest {

    @Rule
    public SystemRepositoryResource repositoryResource = new SystemRepositoryResource("config.xml");

    @Rule
    public ExpectedException expectedException = ExpectedException.none();


    /**
     * システムリポジトリに定義したダイアレクトが取得できること。
     *
     * @throws Exception
     */
    @Test
    public void getDialect() throws Exception {
        Dialect dialect = DomaTransactionNotSupportedConfig.singleton()
                                                           .getDialect();
        assertThat(dialect)
                  .isInstanceOf(H2Dialect.class);
    }

    /**
     * システムリポジトリに定義したデータソースが取得できること。
     *
     * @throws Exception
     */
    @Test
    public void getDataSource() throws Exception {
        DataSource dataSource = DomaTransactionNotSupportedConfig.singleton()
                                                                 .getDataSource();
        assertThat(dataSource)
                  .isInstanceOf(BasicDataSource.class);
    }

    /**
     * トランザクションはサポートしないので例外が送出されること。
     *
     * @throws Exception
     */
    @Test
    public void getTransactionManager() throws Exception {

        assertThatThrownBy(() -> DomaTransactionNotSupportedConfig.singleton()
                                                                             .getTransactionManager())
                  .isInstanceOf(UnsupportedOperationException.class)
        ;
    }

    /**
     * ネーミング規約が大文字スネークケースであること。
     *
     * @throws Exception
     */
    @Test
    public void getNaming() throws Exception {
        Naming naming = DomaTransactionNotSupportedConfig.singleton()
                                                         .getNaming();
        assertThat(naming)
                  .isEqualTo(Naming.SNAKE_UPPER_CASE);
    }

    /**
     * システムリポジトリからダイアレクトが取得できない場合に例外が送出されること。
     *
     * @throws Exception
     */
    @Test
    public void dialect_undefined() throws Exception {

        assertThatThrownBy(() -> {
            repositoryResource.addComponent("domaDialect", null);
            Deencapsulation.newInstance(DomaTransactionNotSupportedConfig.class);
        })
                  .isInstanceOf(IllegalArgumentException.class)
                  .hasMessage("specified domaDialect is not registered in SystemRepository.");
    }

    /**
     * システムリポジトリからデータソースが取得できない場合に例外が送出されること。
     *
     * @throws Exception
     */
    @Test
    public void dataSource_undefined() throws Exception {
        assertThatThrownBy(() -> {
            repositoryResource.addComponent("dataSource", null);
            Deencapsulation.newInstance(DomaTransactionNotSupportedConfig.class);
        })
                  .isInstanceOf(IllegalArgumentException.class)
                  .hasMessage("specified dataSource is not registered in SystemRepository.");
    }

    /**
     * ロガーが取得できること。
     */
    @Test
    public void getJdbcLogger() {
        JdbcLogger jdbcLogger = DomaTransactionNotSupportedConfig.singleton().getJdbcLogger();
        assertThat(jdbcLogger, instanceOf(UtilLoggingJdbcLogger.class));
    }

    /**
     * デフォルトのロガーが取得できること。
     */
    @Test
    public void getDefaultJdbcLogger() {
        repositoryResource.addComponent("domaJdbcLogger", null);
        JdbcLogger jdbcLogger = Deencapsulation.newInstance(DomaTransactionNotSupportedConfig.class).getJdbcLogger();
        assertThat(jdbcLogger, instanceOf(NablarchJdbcLogger.class));
    }

    /**
     * Statementに関する設定値が取得できること。
     */
    @Test
    public void getStatementProperties() {
        DomaTransactionNotSupportedConfig config = DomaTransactionNotSupportedConfig.singleton();
        assertThat(config.getMaxRows(), is(1000));
        assertThat(config.getFetchSize(), is(200));
        assertThat(config.getQueryTimeout(), is(30));
        assertThat(config.getBatchSize(), is(400));
    }

    /**
     * デフォルトのStatementに関する設定値が取得できること。
     */
    @Test
    public void getDefaultStatementProperties() {
        repositoryResource.addComponent("domaStatementProperties", null);
        DomaTransactionNotSupportedConfig config = Deencapsulation.newInstance(DomaTransactionNotSupportedConfig.class);
        assertThat(config.getMaxRows(), is(0));
        assertThat(config.getFetchSize(), is(0));
        assertThat(config.getQueryTimeout(), is(0));
        assertThat(config.getBatchSize(), is(0));
    }
}
