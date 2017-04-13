package nablarch.integration.doma.batch.ee.listener;

import mockit.Expectations;
import mockit.Mocked;
import mockit.Verifications;
import nablarch.fw.batch.ee.listener.NablarchListenerContext;
import nablarch.integration.doma.DomaConfig;
import nablarch.test.support.SystemRepositoryResource;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.seasar.doma.jdbc.tx.LocalTransaction;

/**
 * {@link DomaTransactionStepListener}のテストクラス
 */
public class DomaTransactionStepListenerTest {

    /** テスト対象となるリスナークラス */
    private final DomaTransactionStepListener domaTransactionStepListener = new DomaTransactionStepListener();

    @Mocked
    private NablarchListenerContext mockContext;

    @Rule
    public SystemRepositoryResource repositoryResource = new SystemRepositoryResource("config.xml");


    /** Domaの設定ファイル */
    private DomaConfig domaConfig;

    @Mocked
    private LocalTransaction mockLocalTransaction;

    @Before
    public void setUp() {
        domaConfig = DomaConfig.singleton();
    }

    /**
     * {@link DomaTransactionStepListener#beforeStep(NablarchListenerContext)}で
     * Domaのトランザクションが正常に開始されていること。
     */
    @Test
    public void testBeforeStep() {
        new Expectations() {{
            domaConfig.getLocalTransaction();
            result = mockLocalTransaction;
        }};

        domaTransactionStepListener.beforeStep(mockContext);

        new Verifications() {{
            mockLocalTransaction.begin();
            times = 1;
        }};
    }

    /**
     * {@link DomaTransactionStepListener#afterStep(NablarchListenerContext)}で
     * Domaのトランザクションがcommitされること。
     */
    @Test
    public void testAfterStepNormal() {
        new Expectations() {{
            domaConfig.getLocalTransaction();
            result = mockLocalTransaction;

            mockContext.isStepProcessSucceeded();
            result = true;
        }};

        domaTransactionStepListener.afterStep(mockContext);

        new Verifications() {{
            mockLocalTransaction.commit();
            times = 1;
        }};
    }

    /**
     * {@link DomaTransactionStepListener#afterStep(NablarchListenerContext)}で
     * Domaのトランザクションがrollbackされること。
     */
    @Test
    public void testAfterStepFailed() {
        new Expectations() {{
            domaConfig.getLocalTransaction();
            result = mockLocalTransaction;

            mockContext.isStepProcessSucceeded();
            result = false;
        }};

        domaTransactionStepListener.afterStep(mockContext);

        new Verifications() {{
            mockLocalTransaction.rollback();
            times = 1;
        }};
    }
}
