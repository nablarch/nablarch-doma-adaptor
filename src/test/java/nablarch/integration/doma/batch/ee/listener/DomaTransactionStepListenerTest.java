package nablarch.integration.doma.batch.ee.listener;

import org.seasar.doma.jdbc.tx.LocalTransaction;

import nablarch.fw.batch.ee.listener.NablarchListenerContext;
import nablarch.integration.doma.DomaConfig;
import nablarch.test.support.SystemRepositoryResource;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import mockit.Expectations;
import mockit.Mocked;
import mockit.Verifications;

/**
 * {@link DomaTransactionStepListener}のテストクラス
 */
public class DomaTransactionStepListenerTest {

    /** テスト対象となるリスナークラス */
    private DomaTransactionStepListener sut = new DomaTransactionStepListener();

    @Mocked
    private NablarchListenerContext mockContext;

    @Rule
    public SystemRepositoryResource repositoryResource = new SystemRepositoryResource("config.xml");

    /** Domaの設定ファイル */
    private DomaConfig domaConfig;

    @Before
    public void setUp() {
        domaConfig = DomaConfig.singleton();
    }

    /**
     * {@link DomaTransactionStepListener#beforeStep(NablarchListenerContext)}で
     * Domaのトランザクションが正常に開始されていること。
     */
    @Test
    public void testBeforeStep(@Mocked LocalTransaction mockLocalTransaction) {
        new Expectations() {{
            domaConfig.getLocalTransaction();
            result = mockLocalTransaction;
        }};

        sut.beforeStep(mockContext);

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
    public void testAfterStepNormal(@Mocked LocalTransaction mockLocalTransaction) {
        new Expectations() {{
            domaConfig.getLocalTransaction();
            result = mockLocalTransaction;

            mockContext.isStepProcessSucceeded();
            result = true;
        }};

        sut.afterStep(mockContext);

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
    public void testAfterStepFailed(@Mocked LocalTransaction mockLocalTransaction) {
        new Expectations() {{
            domaConfig.getLocalTransaction();
            result = mockLocalTransaction;

            mockContext.isStepProcessSucceeded();
            result = false;
        }};

        sut.afterStep(mockContext);

        new Verifications() {{
            mockLocalTransaction.rollback();
            times = 1;
        }};
    }
}
