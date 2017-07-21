package nablarch.integration.doma;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

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

    @Entity
    @Table(name = "TEST_TABLE")
    public static class TestTable {

        public TestTable() {
        }

        public TestTable(String name) {
            this.name = name;
        }

        @Id
        @Column(name = "NAME")
        public String name;

        public String getName() {
            return name;
        }
    }

    @org.seasar.doma.Entity
    @org.seasar.doma.Table(name = "TEST_TABLE")
    public static class TestTableForDoma {

        public TestTableForDoma() {
        }

        public TestTableForDoma(String name) {
            this.name = name;
        }

        @org.seasar.doma.Id
        @org.seasar.doma.Column(name = "NAME")
        public String name;
    }
}