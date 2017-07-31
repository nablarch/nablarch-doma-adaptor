package nablarch.integration.doma;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.seasar.doma.jdbc.tx.TransactionIsolationLevel;

import nablarch.core.db.connection.DbConnectionContext;
import nablarch.core.repository.SystemRepository;
import nablarch.core.transaction.TransactionContext;
import nablarch.core.util.annotation.Published;
import nablarch.fw.ExecutionContext;
import nablarch.fw.Interceptor;
import nablarch.fw.Interceptor.Impl;
import nablarch.integration.doma.Transactional.TransactionalImpl;

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
            final ConnectionFactoryFromDomaConnection connectionFactory = getConnectionFactory();
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
                        try {
                            addConnectionToNablarch();
                            return getOriginalHandler().handle(data, context);
                        } finally {
                            DbConnectionContext.removeConnection();
                        }
                    }
            );
        }

        /**
         * DomaのコネクションからNablarch用のデータベース接続に変換するファクトリを取得する。
         *
         * @return DomaのコネクションをNablarch用に変換するファクトリ
         */
        private ConnectionFactoryFromDomaConnection getConnectionFactory() {
            return SystemRepository.get("connectionFactoryFromDoma");
        }
    }
}
