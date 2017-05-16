package nablarch.integration.doma.batch.ee.listener;

import nablarch.fw.batch.ee.listener.NablarchListenerContext;
import nablarch.fw.batch.ee.listener.chunk.AbstractNablarchItemWriteListener;
import nablarch.fw.batch.ee.listener.chunk.NablarchItemWriteListener;
import nablarch.integration.doma.DomaConfig;
import org.seasar.doma.jdbc.tx.LocalTransaction;

import java.util.List;

/**
 * {@link javax.batch.api.chunk.listener.ItemWriteListener}レベルでDomaのトランザクション制御を行う{@link NablarchItemWriteListener}の実装クラス。
 * <p>
 * 前段に配置した{@link DomaTransactionStepListener}からDomaの{@link LocalTransaction}を取得し、トランザクション制御を行う。
 * <p>
 * {@link javax.batch.api.chunk.ItemWriter}が正常に終了した場合には、トランザクションの確定({@link LocalTransaction#commit()})を実行し、
 * その後に{@link LocalTransaction}を開始({@link LocalTransaction#begin()})する。
 * <br>
 * {@link javax.batch.api.chunk.ItemWriter}で{@link Exception}が発生した場合には、トランザクションの破棄({@link LocalTransaction#rollback()}を行う。
 *
 * @author d-maeno
 */
public class DomaTransactionItemWriteListener extends AbstractNablarchItemWriteListener {

    @Override
    public void afterWrite(final NablarchListenerContext context, final List<Object> items) {
        LocalTransaction localTransaction = DomaConfig.singleton().getLocalTransaction();
        try {
            if (context.isProcessSucceeded()) {
                localTransaction.commit();
            }
        } finally {
            localTransaction.rollback();
            localTransaction.begin();
        }
    }

    @Override
    public void onWriteError(final NablarchListenerContext context, final List<Object> items, final Exception ex) {
        final LocalTransaction transaction = DomaConfig.singleton()
                                                       .getLocalTransaction();
        transaction.rollback();
        transaction.begin();
    }
}
