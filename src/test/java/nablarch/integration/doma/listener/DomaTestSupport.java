package nablarch.integration.doma.listener;

import nablarch.core.db.connection.AppDbConnection;
import nablarch.core.db.connection.DbConnectionContext;
import nablarch.core.db.connection.TransactionManagerConnection;
import nablarch.core.repository.di.DiContainer;
import nablarch.core.repository.di.config.xml.XmlComponentDefinitionLoader;
import nablarch.core.transaction.TransactionContext;
import nablarch.test.support.db.helper.VariousDbTestHelper;
import org.slf4j.bridge.SLF4JBridgeHandler;

import javax.batch.operations.JobOperator;
import javax.batch.runtime.BatchRuntime;
import javax.batch.runtime.BatchStatus;
import javax.batch.runtime.JobExecution;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.LogManager;

/**
 * Domaの結合テストのヘルパー
 */
public class DomaTestSupport {

    /**
     * テストの前処理でDIコンテナ及びデータソースの初期化を行う。
     * @throws IOException
     */
    protected static void beforeClass() throws IOException{
        final DiContainer container = new DiContainer(new XmlComponentDefinitionLoader("db-default.xml"));
        VariousDbTestHelper.initialize(container);
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();

        // jBatch実装側のログをより詳細まで出るよう変更
        LogManager.getLogManager()
                .readConfiguration(new ByteArrayInputStream(
                        ("handlers=org.slf4j.bridge.SLF4JBridgeHandler\n"
                                + ".level=INFO\n"
                                + "com.ibm.level=FINE\n"
                                + "org.slf4j.bridge.SLF4JBridgeHandler.level=FINEST\n"
                                + "org.slf4j.bridge.SLF4JBridgeHandler.formatter=java.util.logging.SimpleFormatter\n").getBytes()));
    }

    /**
     *　テストの後処理でDBコネクションとトランザクションを削除する。
     */
    protected static void afterClass() {
        try {
            final Field field = DbConnectionContext.class.getDeclaredField("connection");
            field.setAccessible(true);
            final ThreadLocal<Map<String, AppDbConnection>> connectionList = (ThreadLocal<Map<String, AppDbConnection>>) field.get(null);
            for (Map.Entry<String, AppDbConnection> entry : connectionList.get().entrySet()) {
                final String key = entry.getKey();
                final TransactionManagerConnection connection = DbConnectionContext.getTransactionManagerConnection(
                        key);
                try {
                    connection.terminate();
                } catch (Exception ignore) {
                }
                DbConnectionContext.removeConnection(key);
                TransactionContext.removeTransaction(key);
            }
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 指定されたジョブを実行する。
     * @param jobXmlName 実行するジョブ名
     */
    protected void executeJob(final String jobXmlName) {
        final JobOperator jobOperator = BatchRuntime.getJobOperator();
        final long executionId = jobOperator.start(jobXmlName, null);
        final JobExecution jobExecution = jobOperator.getJobExecution(executionId);
        final Set<BatchStatus> endStatuses = new HashSet<>();
        endStatuses.add(BatchStatus.ABANDONED);
        endStatuses.add(BatchStatus.COMPLETED);
        endStatuses.add(BatchStatus.FAILED);
        endStatuses.add(BatchStatus.STOPPED);
        int count = 0;
        while (true) {
            try {
                if (endStatuses.contains(jobExecution.getBatchStatus())) {
                    break;
                }
                Thread.sleep(100);
                if( count++ > 50 ) {
                    throw new RuntimeException("timeout");
                }
            } catch (InterruptedException e) {
                throw new RuntimeException("interrupted.",e);
            }
        }
    }

    @Entity
    @Table(name = "TEST_ENTITY")
    public static class TestEntity {

        public TestEntity() {
        }

        public TestEntity(String name) {
            this.name = name;
        }

        @Id
        @Column(name = "NAME")
        public String name;
    }
}
