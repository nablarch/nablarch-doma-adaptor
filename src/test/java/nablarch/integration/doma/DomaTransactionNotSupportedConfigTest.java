package nablarch.integration.doma;

import nablarch.test.support.SystemRepositoryResource;
import nablarch.test.support.reflection.ReflectionUtil;
import org.apache.commons.dbcp.BasicDataSource;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.seasar.doma.jdbc.JdbcLogger;
import org.seasar.doma.jdbc.Naming;
import org.seasar.doma.jdbc.UtilLoggingJdbcLogger;
import org.seasar.doma.jdbc.dialect.Dialect;
import org.seasar.doma.jdbc.dialect.H2Dialect;

import javax.sql.DataSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

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
     * <p>Configオブジェクトの初期化タイミングがクラスロード時であり、
     * 本テストクラスの前処理で設定したシステムリポジトリの状態で初期化される保証が無いため、
     * 明示的に生成した結果から検証する。（以降のテストも同様）
     *
     * @throws Exception
     */
    @Test
    public void getDialect() throws Exception {
        DomaTransactionNotSupportedConfig config = ReflectionUtil.newInstance(DomaTransactionNotSupportedConfig.class);
        Dialect dialect = config.getDialect();
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
        DomaTransactionNotSupportedConfig config = ReflectionUtil.newInstance(DomaTransactionNotSupportedConfig.class);
        DataSource dataSource = config.getDataSource();
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
        DomaTransactionNotSupportedConfig config = ReflectionUtil.newInstance(DomaTransactionNotSupportedConfig.class);
        assertThatThrownBy(config::getTransactionManager)
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
        DomaTransactionNotSupportedConfig config = ReflectionUtil.newInstance(DomaTransactionNotSupportedConfig.class);
        Naming naming = config.getNaming();
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
            ReflectionUtil.newInstance(DomaTransactionNotSupportedConfig.class);
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
            ReflectionUtil.newInstance(DomaTransactionNotSupportedConfig.class);
        })
                  .isInstanceOf(IllegalArgumentException.class)
                  .hasMessage("specified dataSource is not registered in SystemRepository.");
    }

    /**
     * ロガーが取得できること。
     */
    @Test
    public void getJdbcLogger() {
        DomaTransactionNotSupportedConfig config = ReflectionUtil.newInstance(DomaTransactionNotSupportedConfig.class);
        JdbcLogger jdbcLogger = config.getJdbcLogger();
        assertThat(jdbcLogger, instanceOf(UtilLoggingJdbcLogger.class));
    }

    /**
     * デフォルトのロガーが取得できること。
     */
    @Test
    public void getDefaultJdbcLogger() {
        repositoryResource.addComponent("domaJdbcLogger", null);
        JdbcLogger jdbcLogger = ReflectionUtil.newInstance(DomaTransactionNotSupportedConfig.class).getJdbcLogger();
        assertThat(jdbcLogger, instanceOf(NablarchJdbcLogger.class));
    }

    /**
     * Statementに関する設定値が取得できること。
     */
    @Test
    public void getStatementProperties() {
        DomaTransactionNotSupportedConfig config = ReflectionUtil.newInstance(DomaTransactionNotSupportedConfig.class);
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
        DomaTransactionNotSupportedConfig config = ReflectionUtil.newInstance(DomaTransactionNotSupportedConfig.class);
        assertThat(config.getMaxRows(), is(0));
        assertThat(config.getFetchSize(), is(0));
        assertThat(config.getQueryTimeout(), is(0));
        assertThat(config.getBatchSize(), is(0));
    }

    /**
     * 初期化されたConfigオブジェクトを取得できること。
     *
     * <p>Configオブジェクトの初期化タイミングがクラスロード時であり、
     * 本テストクラスの前処理で設定したシステムリポジトリの状態で初期化される保証が無いため、
     * 初期化によりオブジェクトが設定されていることのみ検証する。
     */
    @Test
    public void getSingletonObject() {
        assertNotNull(DomaTransactionNotSupportedConfig.singleton());
    }
}
