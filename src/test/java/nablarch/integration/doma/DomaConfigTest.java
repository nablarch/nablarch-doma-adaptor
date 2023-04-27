package nablarch.integration.doma;

import nablarch.test.support.SystemRepositoryResource;
import nablarch.test.support.reflection.ReflectionUtil;
import org.apache.commons.dbcp.BasicDataSource;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.seasar.doma.jdbc.JdbcLogger;
import org.seasar.doma.jdbc.Naming;
import org.seasar.doma.jdbc.UtilLoggingJdbcLogger;
import org.seasar.doma.jdbc.dialect.Dialect;
import org.seasar.doma.jdbc.dialect.H2Dialect;
import org.seasar.doma.jdbc.tx.LocalTransactionDataSource;
import org.seasar.doma.jdbc.tx.LocalTransactionManager;
import org.seasar.doma.jdbc.tx.TransactionManager;

import javax.sql.DataSource;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * {@link DomaConfig}のテストクラス。
 */
public class DomaConfigTest {

    @Rule
    public SystemRepositoryResource repositoryResource = new SystemRepositoryResource("config.xml");

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setUp() {
        // クラスの初期化が完了した後に実行されないと失敗するテストケースがあるので、冒頭で確実に初期化させる
        DomaConfig.singleton();
    }

    /**
     * システムリポジトリに定義したダイアレクトが取得できること。
     * @throws Exception
     */
    @Test
    public void getDialect() throws Exception {
        Dialect dialect = DomaConfig.singleton().getDialect();
        assertThat(dialect, instanceOf(H2Dialect.class));
    }

    /**
     * システムリポジトリに定義したデータソースが取得できること。
     * @throws Exception
     */
    @Test
    public void getDataSource() throws Exception {
        DataSource dataSource = DomaConfig.singleton().getDataSource();
        assertThat(dataSource, instanceOf(LocalTransactionDataSource.class));
        assertThat(ReflectionUtil.getFieldValue(dataSource, "dataSource"), instanceOf(BasicDataSource.class));
    }

    /**
     * トランザクションマネージャが取得できること。
     * @throws Exception
     */
    @Test
    public void getTransactionManager() throws Exception {
        TransactionManager transactionManager = DomaConfig.singleton().getTransactionManager();
        assertThat(transactionManager, instanceOf(LocalTransactionManager.class));

    }

    /**
     * ネーミング規約が大文字スネークケースであること。
     * @throws Exception
     */
    @Test
    public void getNaming() throws Exception {
        Naming naming = DomaConfig.singleton().getNaming();
        assertThat(naming.apply(null, "testName"), is("TEST_NAME"));
    }

    /**
     * システムリポジトリからダイアレクトが取得できない場合に例外が送出されること。
     * @throws Exception
     */
    @Test
    public void dialect_undefined() throws Exception {
        repositoryResource.addComponent("domaDialect", null);

        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage(is("specified domaDialect is not registered in SystemRepository."));

        ReflectionUtil.newInstance(DomaConfig.class);
    }

    /**
     * システムリポジトリからデータソースが取得できない場合に例外が送出されること。
     * @throws Exception
     */
    @Test
    public void dataSource_undefined() throws Exception {
        repositoryResource.addComponent("dataSource", null);

        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage(is("specified dataSource is not registered in SystemRepository."));

        ReflectionUtil.newInstance(DomaConfig.class);
    }

    /**
     * ロガーが取得できること。
     */
    @Test
    public void getJdbcLogger() {
        JdbcLogger jdbcLogger = DomaConfig.singleton().getJdbcLogger();
        assertThat(jdbcLogger, instanceOf(UtilLoggingJdbcLogger.class));
    }

    /**
     * デフォルトのロガーが取得できること。
     */
    @Test
    public void getDefaultJdbcLogger() {
        repositoryResource.addComponent("domaJdbcLogger", null);
        JdbcLogger jdbcLogger = ReflectionUtil.newInstance(DomaConfig.class).getJdbcLogger();
        assertThat(jdbcLogger, instanceOf(NablarchJdbcLogger.class));
    }

    /**
     * Statementに関する設定値が取得できること。
     */
    @Test
    public void getStatementProperties() {
        DomaConfig config = DomaConfig.singleton();
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
        DomaConfig config = ReflectionUtil.newInstance(DomaConfig.class);
        assertThat(config.getMaxRows(), is(0));
        assertThat(config.getFetchSize(), is(0));
        assertThat(config.getQueryTimeout(), is(0));
        assertThat(config.getBatchSize(), is(0));
    }
}