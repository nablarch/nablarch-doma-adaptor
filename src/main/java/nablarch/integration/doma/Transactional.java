package nablarch.integration.doma;

import nablarch.core.db.connection.DbConnectionContext;
import nablarch.core.repository.SystemRepository;
import nablarch.core.transaction.TransactionContext;
import nablarch.core.util.annotation.Published;
import nablarch.fw.ExecutionContext;
import nablarch.fw.Interceptor;
import nablarch.fw.Interceptor.Impl;
import nablarch.integration.doma.Transactional.TransactionalImpl;
import org.seasar.doma.jdbc.tx.TransactionIsolationLevel;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Domaによるトランザクション制御を行うことを表すアノテーション。
 *
 * @author Naoki Yamamoto
 */
@Interceptor(TransactionalImpl.class)
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Published
public @interface Transactional {

    /** トランザクション分離レベル */
    TransactionIsolationLevel transactionIsolationLevel() default TransactionIsolationLevel.READ_COMMITTED;

    /**
     * {@link Transactional}アノテーションの{@link Interceptor}クラス。
     *
     * @author Naoki Yamamoto
     */
    class TransactionalImpl extends Impl<Object, Object, Transactional> {

        /**
         * Nablarchのデータベースアクセスが利用出来るようにコネクションを{@link DbConnectionContext}の設定する。
         */
        private void addConnectionToNablarch() {
            ConnectionFactoryFromDomaConnection connectionFactory =
                    SystemRepository.get("connectionFactory");
            if (connectionFactory != null) {
                DbConnectionContext.setConnection(
                        connectionFactory.getConnection(TransactionContext.DEFAULT_TRANSACTION_CONTEXT_KEY));
            }
        }

        @Override
        public Object handle(final Object data, final ExecutionContext context) {
            return DomaConfig.singleton().getTransactionManager().requiresNew(
                    getInterceptor().transactionIsolationLevel(),
                    () -> {
                        addConnectionToNablarch();
                        return getOriginalHandler().handle(data, context);
                    }
            );
        }
    }
}
