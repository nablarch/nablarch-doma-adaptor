package nablarch.integration.doma.listener;

import nablarch.fw.batch.ee.listener.NablarchListenerContext;
import nablarch.fw.batch.ee.listener.chunk.AbstractNablarchItemWriteListener;
import nablarch.fw.batch.ee.listener.chunk.NablarchItemWriteListener;
import nablarch.integration.doma.DomaConfig;
import org.seasar.doma.jdbc.tx.LocalTransaction;

import java.util.List;

/**
 * {@link javax.batch.api.chunk.listener.ItemWriteListener}レベルでDomaのトランザクション制御を行う{@link NablarchItemWriteListener}の実装クラス。
 * <p/>
 * {@link javax.batch.api.chunk.ItemWriter}が正常に終了した場合には、トランザクションの確定({@link LocalTransaction#commit()})を実行し、
 * {@link javax.batch.api.chunk.ItemWriter}で{@link Exception}が発生した場合には、トランザクションの破棄({@link LocalTransaction#rollback()}を行う。
 *
 * @author d-maeno
 */
public class DomaTransactionItemWriteListener extends AbstractNablarchItemWriteListener {

    @Override
    public void beforeWrite(final NablarchListenerContext context, final List<Object> items) {
        DomaConfig.singleton().getLocalTransaction().begin();
    }

    @Override
    public void afterWrite(final NablarchListenerContext context, final List<Object> items) {
        if (context.isProcessSucceeded()) {
            DomaConfig.singleton().getLocalTransaction().commit();
        } else {
            DomaConfig.singleton().getLocalTransaction().rollback();
        }
    }

    @Override
    public void onWriteError(final NablarchListenerContext context, final List<Object> items, final Exception ex) {
        DomaConfig.singleton().getLocalTransaction().rollback();
    }
}
