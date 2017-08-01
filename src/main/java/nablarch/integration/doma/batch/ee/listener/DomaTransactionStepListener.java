package nablarch.integration.doma.batch.ee.listener;

import org.seasar.doma.jdbc.tx.LocalTransaction;

import nablarch.core.db.connection.DbConnectionContext;
import nablarch.core.db.connection.TransactionManagerConnection;
import nablarch.core.transaction.TransactionContext;
import nablarch.fw.batch.ee.listener.NablarchListenerContext;
import nablarch.fw.batch.ee.listener.step.AbstractNablarchStepListener;
import nablarch.fw.batch.ee.listener.step.NablarchStepListener;
import nablarch.integration.doma.ConnectionFactoryFromDomaConnection;
import nablarch.integration.doma.DomaConfig;

/**
 * ステップレベルで、Domaのトランザクション制御を行う{@link NablarchStepListener}の実装クラス。
 * <p>
 * ステップ開始時にトランザクションを開始し、ステップ終了時に正常終了していれば{@link LocalTransaction#commit()}、
 * そうでなければ、{@link LocalTransaction#rollback()}を呼び出す。
 *
 * @author d-maeno
 */
public class DomaTransactionStepListener extends AbstractNablarchStepListener {

    /**
     * コネクションファクトリ
     */
    private ConnectionFactoryFromDomaConnection connectionFactory;

    @Override
    public void beforeStep(final NablarchListenerContext context) {
        DomaConfig.singleton().getLocalTransaction().begin();

        if (connectionFactory != null) {
            DbConnectionContext.setConnection(
                    connectionFactory.getConnection(TransactionContext.DEFAULT_TRANSACTION_CONTEXT_KEY));
        }
    }

    @Override
    public void afterStep(final NablarchListenerContext context) {
        final LocalTransaction localTransaction = DomaConfig.singleton().getLocalTransaction();
        try {
            if (context.isStepProcessSucceeded()) {
                localTransaction.commit();
            }
        } finally {
            try {
                removeNablarchConnection();
            } finally {
                localTransaction.rollback();
            }
        }
    }

    /**
     * nablarch用のデータベース接続の破棄処理を行う。
     */
    private void removeNablarchConnection() {
        if (!DbConnectionContext.containConnection(TransactionContext.DEFAULT_TRANSACTION_CONTEXT_KEY)) {
            return;
        }
        final TransactionManagerConnection connection = DbConnectionContext.getTransactionManagerConnection();
        try {
            connection.terminate();
        } finally {
            DbConnectionContext.removeConnection();
        }
    }

    /**
     * コネクションファクトリを設定する。
     *
     * @param connectionFactory コネクションファクトリ
     */
    public void setConnectionFactory(final ConnectionFactoryFromDomaConnection connectionFactory) {
        this.connectionFactory = connectionFactory;
    }
}
