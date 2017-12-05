package nablarch.integration.doma;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;

import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSource;
import org.seasar.doma.jdbc.JdbcLogger;
import org.seasar.doma.jdbc.Naming;
import org.seasar.doma.jdbc.dialect.Dialect;
import org.seasar.doma.jdbc.dialect.H2Dialect;

import nablarch.test.support.SystemRepositoryResource;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import mockit.Deencapsulation;

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
        assertThat(jdbcLogger, instanceOf(NablarchJdbcLogger.class));
    }
}