package nablarch.integration.doma;

import nablarch.common.dao.UniversalDao;
import nablarch.core.db.connection.BasicDbConnection;
import nablarch.core.db.connection.DbConnectionContext;
import nablarch.fw.ExecutionContext;
import nablarch.fw.Handler;
import nablarch.test.support.SystemRepositoryResource;
import nablarch.test.support.db.helper.DatabaseTestRunner;
import nablarch.test.support.db.helper.VariousDbTestHelper;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.sql.SQLException;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * {@link DomaConnection}のテストクラス。
 *
 */
@RunWith(DatabaseTestRunner.class)
public class DomaConnectionTest {

    @Rule
    public SystemRepositoryResource repositoryResource = new SystemRepositoryResource("DomaConnectionConfig.xml");

    @Before
    public void setUp() throws Exception {
        VariousDbTestHelper.createTable(TransactionalTest.TestTable.class);
        VariousDbTestHelper.insert(new TransactionalTest.TestTable("name"));
    }

    @Test
    public void DomaのコネクションがDbConnectionContextに設定されていること() throws Exception {

        new ExecutionContext().addHandler(new Handler<String, Object>() {
            @Override
            @DomaConnection
            public Object handle(String s, ExecutionContext context) {
                final BasicDbConnection connection = (BasicDbConnection) DbConnectionContext.getConnection();
                final DomaConfig domaConfig = DomaConfig.singleton();
                try {
                    assertThat(connection.getConnection(), is(domaConfig.getDataSource().getConnection()));
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
                return null;
            }
        })
        .handleNext("test");
    }

    @Test
    public void Domaのコネクションを利用してNablarchのデータベースアクセスが使用出来ること() throws Exception {
        new ExecutionContext().addHandler(new Handler<String, Object>() {
            @Override
            @DomaConnection
            public Object handle(String s, ExecutionContext context) {
                UniversalDao.insert(new TransactionalTest.TestTable("test"));
                return null;
            }
        })
        .handleNext("test");
        // レコードが登録され、個数が2であること
        assertThat(VariousDbTestHelper.findAll(TransactionalTest.TestTable.class).size(), is(2));
    }

    @Test
    public void ハンドラ内で例外が発生した場合ロールバックされること() throws Exception {
        new ExecutionContext()
                .addHandler(new Handler<Object, Object>() {
                    @Override
                    public Object handle(Object o, ExecutionContext context) {
                        try {
                            context.handleNext(o);
                        } catch (RuntimeException e) {
                            // ignore
                        }
                        return null;
                    }
                })
                .addHandler(new Handler<String, Object>() {
                    @Override
                    @DomaConnection
                    public Object handle(String s, ExecutionContext context) {
                        UniversalDao.insert(new TransactionalTest.TestTable("test"));
                        throw new RuntimeException("Test exception");
                    }
                })
                .handleNext("test");
        // レコードが登録されず、初期状態のままであること
        assertThat(VariousDbTestHelper.findAll(TransactionalTest.TestTable.class).size(), is(1));
    }
}
