package nablarch.integration.doma;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import nablarch.fw.ExecutionContext;
import nablarch.fw.Interceptor;
import nablarch.fw.Interceptor.Impl;
import nablarch.integration.doma.Transactional.TransactionalImpl;
import org.seasar.doma.jdbc.tx.TransactionIsolationLevel;

/**
 * Domaによるトランザクション制御を行うことを表すアノテーション。
 *
 * @author Naoki Yamamoto
 */
@Interceptor(TransactionalImpl.class)
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Transactional {

    /** トランザクション分離レベル */
    TransactionIsolationLevel transactionIsolationLevel() default TransactionIsolationLevel.DEFAULT;

    /**
     * {@link Transactional}アノテーションの{@link Interceptor}クラス。
     *
     * @author Naoki Yamamoto
     */
    class TransactionalImpl extends Impl<Object, Object, Transactional> {

        @Override
        public Object handle(final Object data, final ExecutionContext context) {
            return DomaConfig.singleton().getTransactionManager().requiresNew(
                    getInterceptor().transactionIsolationLevel(), () -> getOriginalHandler().handle(data, context)
            );
        }
    }
}
