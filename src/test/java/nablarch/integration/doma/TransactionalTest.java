package nablarch.integration.doma;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

import nablarch.fw.ExecutionContext;
import nablarch.fw.Handler;
import nablarch.test.support.SystemRepositoryResource;
import nablarch.test.support.db.helper.DatabaseTestRunner;
import nablarch.test.support.db.helper.VariousDbTestHelper;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * {@link Transactional}のテストクラス。
 */
@RunWith(DatabaseTestRunner.class)
public class TransactionalTest {

    @Rule
    public SystemRepositoryResource repositoryResource = new SystemRepositoryResource("config.xml");

    @BeforeClass
    public static void setUpClass() throws Exception {
        VariousDbTestHelper.createTable(TestTable.class);
    }

    @Before
    public void setUp() throws Exception {
        VariousDbTestHelper.delete(TestTable.class);
    }

    /**
     * {@link Transactional}が設定されたハンドラ内でトランザクション制御が行われていること
     * @throws Exception
     */
    @Test
    public void handle_commit() throws Exception {
        new ExecutionContext()
                .addHandler(new Handler<String, Object>() {
                    @Override
                    @Transactional
                    public Object handle(String data, ExecutionContext context) {
                        DomaDaoRepository.get(TestTableDao.class).insert(new TestTableForDoma("test"));

                        assertThat("トランザクションがコミットされていないため、レコードが取得できないこと",
                                VariousDbTestHelper.findAll(TestTable.class).size(), is(0));
                        return null;
                    }})
                .handleNext("test");

        assertThat("トランザクションがコミットされたため、レコードを1件取得できること",
                VariousDbTestHelper.findAll(TestTable.class).size(), is(1));
    }

    /**
     * {@link Transactional}が設定されたハンドラ内で例外が発生した場合に
     * トランザクションがロールバックされること。
     * @throws Exception
     */
    @Test
    public void handle_rollback() throws Exception {
        new ExecutionContext()
                .addHandler((o, context) -> {
                    try {
                        context.handleNext(o);
                    } catch (RuntimeException e) {
                        // NOP
                    }
                    return null;
                })
                .addHandler(new Handler<String, Object>() {
                    @Override
                    @Transactional
                    public Object handle(String data, ExecutionContext context) {
                        DomaDaoRepository.get(TestTableDao.class).insert(new TestTableForDoma("test"));

                        throw new RuntimeException("test");
                    }})
                .handleNext("test");

        assertThat("トランザクションがロールバックされているため、レコードが取得できないこと",
                VariousDbTestHelper.findAll(TestTable.class).size(), is(0));
    }

}