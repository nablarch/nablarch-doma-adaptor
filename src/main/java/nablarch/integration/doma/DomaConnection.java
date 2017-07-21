package nablarch.integration.doma;

import nablarch.core.db.DbExecutionContext;
import nablarch.core.db.connection.BasicDbConnection;
import nablarch.core.db.connection.DbConnectionContext;
import nablarch.core.db.dialect.Dialect;
import nablarch.core.db.statement.StatementFactory;
import nablarch.core.repository.SystemRepository;
import nablarch.core.transaction.TransactionContext;
import nablarch.core.util.annotation.Published;
import nablarch.fw.ExecutionContext;
import nablarch.fw.Interceptor;
import org.seasar.doma.internal.jdbc.util.JdbcUtil;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * DomaのコネクションをNablarchのデータベースアクセスで使うためのアノテーション。
 *
 * @author MAENO Daisuke.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Interceptor(DomaConnection.Impl.class)
@Published
public @interface DomaConnection {
    class Impl extends Interceptor.Impl<Object, Object, DomaConnection> {

        /**
         * Domaの設定情報
         */
        private final DomaConfig domaConfig = DomaConfig.singleton();

        @Override
        public Object handle(final Object data, final ExecutionContext context) {
            preHandle();
            registerConnection();
            Object handle = null;
            try {
                handle = getOriginalHandler().handle(data, context);
                postHandle();
            } catch (Exception e) {
                errorHandle();
            }
            removeConnection();
            return handle;
        }

        /**
         * コネクションを削除する。
         */
        private void removeConnection() {
            DbConnectionContext.removeConnection();
        }

        /**
         * 例外発生時の処理を行う。
         * ここでは、Domaのトランザクションのロールバックを行う。
         */
        private void errorHandle() {
            domaConfig.getLocalTransaction().rollback();
        }

        /**
         * DomaのコネクションをNablarchの{@link DbConnectionContext}に登録する。
         */
        private void registerConnection() {
            final Dialect dialect = SystemRepository.get("dialect");
            final StatementFactory statementFactory = SystemRepository.get("statementFactory");

            final BasicDbConnection basicDbConnection = new BasicDbConnection(JdbcUtil.getConnection(domaConfig.getDataSource()));
            basicDbConnection.setContext(
                    new DbExecutionContext(
                            basicDbConnection,
                            dialect,
                            TransactionContext.DEFAULT_TRANSACTION_CONTEXT_KEY)
            );
            basicDbConnection.setFactory(statementFactory);

            DbConnectionContext.setConnection(basicDbConnection);
        }

        /**
         * 後処理を行う。
         * ここでは、Domaのトランザクションコミットを行う。
         */
        private void postHandle() {
            domaConfig.getLocalTransaction().commit();
        }

        /**
         * 前処理を行う。
         * ここでは、Domaのトランザクションを開始する。
         */
        private void preHandle() {
            domaConfig.getLocalTransaction().begin();
        }
    }
}
