package nablarch.integration.doma;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import javax.sql.DataSource;

import mockit.Deencapsulation;
import nablarch.test.support.SystemRepositoryResource;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.seasar.doma.jdbc.Naming;
import org.seasar.doma.jdbc.dialect.Dialect;
import org.seasar.doma.jdbc.dialect.H2Dialect;
import org.seasar.doma.jdbc.tx.LocalTransactionDataSource;
import org.seasar.doma.jdbc.tx.LocalTransactionManager;
import org.seasar.doma.jdbc.tx.TransactionManager;

/**
 * {@link DomaConfig}のテストクラス。
 */
public class DomaConfigTest {

    @Rule
    public SystemRepositoryResource repositoryResource = new SystemRepositoryResource("config.xml");

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

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
        assertThat(Deencapsulation.getField(dataSource, "dataSource"), instanceOf(JdbcDataSource.class));
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
        repositoryResource.addComponent("dialect", null);

        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage(is("specified dialect is not registered in SystemRepository."));

        Deencapsulation.newInstance(DomaConfig.class);
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

        Deencapsulation.newInstance(DomaConfig.class);
    }

}