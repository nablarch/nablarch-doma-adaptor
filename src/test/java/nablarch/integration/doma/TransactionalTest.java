package nablarch.integration.doma;

import mockit.Mocked;
import mockit.Verifications;
import nablarch.fw.ExecutionContext;
import nablarch.fw.Handler;
import nablarch.test.support.SystemRepositoryResource;
import nablarch.test.support.db.helper.DatabaseTestRunner;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.seasar.doma.jdbc.tx.LocalTransaction;
import org.seasar.doma.jdbc.tx.TransactionIsolationLevel;

/**
 * {@link Transactional}のテストクラス。
 */
@RunWith(DatabaseTestRunner.class)
public class TransactionalTest {

    @Rule
    public SystemRepositoryResource repositoryResource = new SystemRepositoryResource("config.xml");

    @Before
    public void setUp() throws Exception {

    }

    @Mocked
    private LocalTransaction localTransaction;

    /**
     * {@link Transactional}が設定されたハンドラ内でトランザクション制御が行われていること
     * @throws Exception
     */
    @Test
    public void handle_commit() throws Exception {
        new ExecutionContext()
                .addHandler((o, context) -> {
                    // トランザクションが開始されていないこと
                    new Verifications() {{
                       localTransaction.begin(TransactionIsolationLevel.DEFAULT);
                       times = 0;
                    }};

                    // 後続ハンドラを実行
                    Object result = context.handleNext(o);

                    // トランザクションがコミットされていること
                    new Verifications() {{
                        localTransaction.begin(TransactionIsolationLevel.DEFAULT);
                        times = 1;
                        localTransaction.commit();
                        times = 1;
                    }};


                    return result;
                })
                .addHandler(new Handler<String, Object>() {
                    @Override
                    @Transactional
                    public Object handle(String data, ExecutionContext context) {
                        // トランザクションが開始されていること
                        new Verifications() {{
                            localTransaction.begin(TransactionIsolationLevel.DEFAULT);
                            times = 1;
                            localTransaction.commit();
                            times = 0;
                        }};
                        return null;
                    }})
                .handleNext("test");
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
                    // トランザクションが開始されていないこと
                    new Verifications() {{
                        localTransaction.begin(TransactionIsolationLevel.DEFAULT);
                        times = 0;
                    }};

                    // 後続ハンドラを実行
                    try {
                        Object result = context.handleNext(o);
                    } catch (RuntimeException e) {
                        // トランザクションがロールバックされていること
                        new Verifications() {{
                            localTransaction.begin(TransactionIsolationLevel.DEFAULT);
                            times = 1;
                            localTransaction.rollback();
                            times = 1;
                            localTransaction.commit();
                            times = 0;
                        }};
                    }
                    return null;
                })
                .addHandler(new Handler<String, Object>() {
                    @Override
                    @Transactional
                    public Object handle(String data, ExecutionContext context) {
                        throw new RuntimeException("test");
                    }})
                .handleNext("test");
    }

    @Test
    public void handle_isolationLavel() throws Exception {
        new ExecutionContext()
                .addHandler(new Handler<String, Object>() {
                    @Override
                    @Transactional(transactionIsolationLevel = TransactionIsolationLevel.READ_COMMITTED)
                    public Object handle(String data, ExecutionContext context) {
                        // 分離レベルがREAD_COMMITTEDでトランザクションが開始されていること
                        new Verifications() {{
                            localTransaction.begin(TransactionIsolationLevel.DEFAULT);
                            times = 0;
                            localTransaction.begin(TransactionIsolationLevel.READ_COMMITTED);
                            times = 1;
                        }};
                        return null;
                    }})
                .handleNext("test");
    }
}