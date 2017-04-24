package nablarch.integration.doma.batch.ee.listener;

import nablarch.fw.batch.ee.listener.NablarchListenerContext;
import nablarch.fw.batch.ee.listener.step.AbstractNablarchStepListener;
import nablarch.fw.batch.ee.listener.step.NablarchStepListener;
import nablarch.integration.doma.DomaConfig;
import org.seasar.doma.jdbc.tx.LocalTransaction;

/**
 * ステップレベルで、Domaのトランザクション制御を行う{@link NablarchStepListener}の実装クラス。
 * <p>
 * ステップ開始時にトランザクションを開始し、ステップ終了時に正常終了していれば{@link LocalTransaction#commit()}、
 * そうでなければ、{@link LocalTransaction#rollback()}を呼び出す。
 *
 * @author d-maeno
 */
public class DomaTransactionStepListener extends AbstractNablarchStepListener {

    @Override
    public void beforeStep(final NablarchListenerContext context) {
        DomaConfig.singleton().getLocalTransaction().begin();
    }

    @Override
    public void afterStep(final NablarchListenerContext context) {
        final LocalTransaction localTransaction = DomaConfig.singleton().getLocalTransaction();
        try {
            if (context.isStepProcessSucceeded()) {
                localTransaction.commit();
            }
        } finally {
            localTransaction.rollback();
        }
    }
}
