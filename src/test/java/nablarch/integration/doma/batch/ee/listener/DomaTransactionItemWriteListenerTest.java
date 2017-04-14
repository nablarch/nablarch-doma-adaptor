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

import java.util.Collections;
import java.util.List;

/**
 * {@link DomaTransactionItemWriteListener}のテストクラス
 */
public class DomaTransactionItemWriteListenerTest {

    /** テスト対象となるリスナークラス */
    private DomaTransactionItemWriteListener sut = new DomaTransactionItemWriteListener();

    @Mocked
    private NablarchListenerContext mockContext;

    @Rule
    public SystemRepositoryResource repositoryResource = new SystemRepositoryResource("config.xml");

    /**  Domaの設定ファイル */
    private DomaConfig domaConfig;

    @Mocked
    private LocalTransaction mockLocalTransaction;

    @Before
    public void setUp() {
        domaConfig = DomaConfig.singleton();
    }

    /**
     * {@link DomaTransactionItemWriteListener#afterWrite(NablarchListenerContext, List)}で
     * Domaのトランザクションが正常にコミットされ、その後開始されていること。
     */
    @Test
    public void testAfterWriteNormal() {
        new Expectations() {{
            domaConfig.getLocalTransaction();
            result = mockLocalTransaction;

            mockContext.isProcessSucceeded();
            result = true;
        }};

        sut.afterWrite(mockContext, Collections.emptyList());

        new Verifications() {{
            mockLocalTransaction.commit();
            times = 1;

            mockLocalTransaction.begin();
            times = 1;
        }};
    }

    /**
     * {@link DomaTransactionItemWriteListener#afterWrite(NablarchListenerContext, List)}で
     * Domaのトランザクションがロールバックされていること。
     */
    @Test
    public void testAfterWriteFailed() {
        new Expectations() {{
            domaConfig.getLocalTransaction();
            result = mockLocalTransaction;

            mockContext.isProcessSucceeded();
            result = false;
        }};

        sut.afterWrite(mockContext, Collections.emptyList());

        new Verifications() {{
            mockLocalTransaction.rollback();
            times = 1;
        }};
    }

    /**
     * {@link DomaTransactionItemWriteListener#onWriteError(NablarchListenerContext, List, Exception)}で
     * Domaのトランザクションがロールバックされていること。
     */
    @Test
    @SuppressWarnings("unchecked")
    public void testOnWriteError() {
        new Expectations() {{
            domaConfig.getLocalTransaction();
            result = mockLocalTransaction;
        }};

        sut.onWriteError(mockContext, Collections.EMPTY_LIST, new Exception());

        new Verifications() {{
            mockLocalTransaction.rollback();
            times = 1;
        }};
    }

}
