package nablarch.integration.doma.batch.ee.listener;

import nablarch.fw.batch.ee.listener.NablarchListenerContext;
import nablarch.integration.doma.DomaConfig;
import nablarch.test.support.SystemRepositoryResource;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.seasar.doma.jdbc.tx.LocalTransaction;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link DomaTransactionStepListener}のテストクラス
 */
public class DomaTransactionStepListenerTest {

    /** テスト対象となるリスナークラス */
    private DomaTransactionStepListener sut = new DomaTransactionStepListener();

    private final NablarchListenerContext mockContext = mock(NablarchListenerContext.class);

    @Rule
    public SystemRepositoryResource repositoryResource = new SystemRepositoryResource("config.xml");

    /** Domaの設定ファイル */
    private DomaConfig domaConfig;

    @Before
    public void setUp() {
        // SystemRepositoryResource でシステムリポジトリが初期化されたあとで
        // DomaConfig のクラスがロードされて初期化されないとエラーになる
        domaConfig = mock(DomaConfig.class);
    }
    
    /**
     * {@link DomaTransactionStepListener#beforeStep(NablarchListenerContext)}で
     * Domaのトランザクションが正常に開始されていること。
     */
    @Test
    public void testBeforeStep() {
        LocalTransaction mockLocalTransaction = mock(LocalTransaction.class);

        try (final MockedStatic<DomaConfig> mocked = mockStatic(DomaConfig.class)) {
            mocked.when(DomaConfig::singleton).thenReturn(domaConfig);
            when(domaConfig.getLocalTransaction()).thenReturn(mockLocalTransaction);

            sut.beforeStep(mockContext);

            verify(mockLocalTransaction).begin();
        }
    }

    /**
     * {@link DomaTransactionStepListener#afterStep(NablarchListenerContext)}で
     * Domaのトランザクションがcommitされること。
     */
    @Test
    public void testAfterStepNormal() {
        LocalTransaction mockLocalTransaction = mock(LocalTransaction.class);

        try (final MockedStatic<DomaConfig> mocked = mockStatic(DomaConfig.class)) {
            mocked.when(DomaConfig::singleton).thenReturn(domaConfig);
            when(domaConfig.getLocalTransaction()).thenReturn(mockLocalTransaction);
            
            when(mockContext.isStepProcessSucceeded()).thenReturn(true);

            sut.afterStep(mockContext);

            verify(mockLocalTransaction).commit();
        }
    }

    /**
     * {@link DomaTransactionStepListener#afterStep(NablarchListenerContext)}で
     * Domaのトランザクションがrollbackされること。
     */
    @Test
    public void testAfterStepFailed() {
        LocalTransaction mockLocalTransaction = mock(LocalTransaction.class);

        try (final MockedStatic<DomaConfig> mocked = mockStatic(DomaConfig.class)) {
            mocked.when(DomaConfig::singleton).thenReturn(domaConfig);
            when(domaConfig.getLocalTransaction()).thenReturn(mockLocalTransaction);
            
            when(mockContext.isStepProcessSucceeded()).thenReturn(false);

            sut.afterStep(mockContext);

            verify(mockLocalTransaction).rollback();
        }
    }
}
