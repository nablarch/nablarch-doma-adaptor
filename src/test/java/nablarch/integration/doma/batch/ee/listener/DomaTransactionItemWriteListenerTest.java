package nablarch.integration.doma.batch.ee.listener;

import nablarch.fw.batch.ee.listener.NablarchListenerContext;
import nablarch.integration.doma.DomaConfig;
import nablarch.test.support.SystemRepositoryResource;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.seasar.doma.jdbc.tx.LocalTransaction;

import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link DomaTransactionItemWriteListener}のテストクラス
 */
public class DomaTransactionItemWriteListenerTest {

    /** テスト対象となるリスナークラス */
    private DomaTransactionItemWriteListener sut = new DomaTransactionItemWriteListener();

    private final NablarchListenerContext mockContext = mock(NablarchListenerContext.class);

    @Rule
    public SystemRepositoryResource repositoryResource = new SystemRepositoryResource("config.xml");

    /**  Domaの設定ファイル */
    private DomaConfig domaConfig;

    @Before
    public void setUp() {
        // SystemRepositoryResource でシステムリポジトリが初期化されたあとで
        // DomaConfig のクラスがロードされて初期化されないとエラーになる
        domaConfig = mock(DomaConfig.class);
    }

    /**
     * {@link DomaTransactionItemWriteListener#afterWrite(NablarchListenerContext, List)}で
     * Domaのトランザクションが正常にコミットされ、その後開始されていること。
     */
    @Test
    public void testAfterWriteNormal() {
        LocalTransaction mockLocalTransaction = mock(LocalTransaction.class);
        
        try (final MockedStatic<DomaConfig> mocked = mockStatic(DomaConfig.class)) {
            mocked.when(DomaConfig::singleton).thenReturn(domaConfig);
            when(domaConfig.getLocalTransaction()).thenReturn(mockLocalTransaction);
            when(mockContext.isProcessSucceeded()).thenReturn(true);

            sut.afterWrite(mockContext, Collections.emptyList());
            
            verify(mockLocalTransaction).commit();
            verify(mockLocalTransaction).begin();
        }
    }

    /**
     * {@link DomaTransactionItemWriteListener#afterWrite(NablarchListenerContext, List)}で
     * Domaのトランザクションがロールバックされていること。
     */
    @Test
    public void testAfterWriteFailed() {
        final LocalTransaction mockLocalTransaction = mock(LocalTransaction.class);
        try (final MockedStatic<DomaConfig> mocked = mockStatic(DomaConfig.class)) {
            mocked.when(DomaConfig::singleton).thenReturn(domaConfig);
            when(domaConfig.getLocalTransaction()).thenReturn(mockLocalTransaction);
            when(mockContext.isProcessSucceeded()).thenReturn(false);

            sut.afterWrite(mockContext, Collections.emptyList());
            
            verify(mockLocalTransaction).rollback();
        }
    }

    /**
     * {@link DomaTransactionItemWriteListener#onWriteError(NablarchListenerContext, List, Exception)}で
     * Domaのトランザクションがロールバックされていること。
     */
    @Test
    @SuppressWarnings("unchecked")
    public void testOnWriteError() {
        final LocalTransaction mockLocalTransaction = mock(LocalTransaction.class);
        try (final MockedStatic<DomaConfig> mocked = mockStatic(DomaConfig.class)) {
            mocked.when(DomaConfig::singleton).thenReturn(domaConfig);
            when(domaConfig.getLocalTransaction()).thenReturn(mockLocalTransaction);

            sut.onWriteError(mockContext, Collections.EMPTY_LIST, new Exception());
            
            verify(mockLocalTransaction).rollback();
        }
    }

}
