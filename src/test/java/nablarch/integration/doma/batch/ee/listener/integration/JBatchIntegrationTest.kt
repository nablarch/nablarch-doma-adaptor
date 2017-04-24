package nablarch.integration.doma.batch.ee.listener.integration

import org.assertj.core.api.Assertions.*
import org.jboss.arquillian.container.test.api.*
import org.jboss.arquillian.junit.*
import org.jboss.shrinkwrap.api.*
import org.jboss.shrinkwrap.api.spec.*
import org.junit.*
import org.junit.runner.*
import java.util.*
import java.util.concurrent.*
import javax.batch.runtime.*

/**
 * JSR352配下でのテスト。
 */
@RunWith(Arquillian::class)
class JBatchIntegrationTest {

    @get:Rule
    val integrationTestResource: IntegrationTestResource = IntegrationTestResource()

    companion object {
        @Deployment
        @JvmStatic
        fun createDeployment(): WebArchive {
            return ShrinkWrap
                .create<WebArchive>(WebArchive::class.java, "batch.war")
                .addPackages(true, "nablarch")
        }
    }

    /**
     * db to dbのBatchletステップのテスト。
     *
     * * 処理が正常に終了すること。
     * * 入力テーブルの状態が出力テーブルに書き込めること
     */
    @Test
    fun dbInsertBatchlet() {
        val jobExecution = integrationTestResource.executeBatch("db2db-batchlet")
        assertThat(jobExecution.batchStatus)
            .isEqualTo(BatchStatus.COMPLETED)

        assertThat(integrationTestResource.findOutputTable())
            .isEqualTo(integrationTestResource.inputRecords())
    }

    /**
     * db to dbのBatchletで処理中に例外(一意制約違反を想定)が発生した場合のテスト。
     *
     * * トランザクションがロールバックされ、出力テーブルの状態は元のままとなること。
     * * バッチが異常終了すること
     */
    @Test
    fun dbInsertBatchlet_error() {
        // output tableにデータを登録
        integrationTestResource.insertOutputTable(listOf(123, 10))

        val jobExecution = integrationTestResource.executeBatch("db2db-batchlet-error")

        assertThat(jobExecution.batchStatus)
            .isEqualTo(BatchStatus.FAILED)

        assertThat(integrationTestResource.findOutputTable())
            .`as`("output tableはロールバックされ元のレコードのみとなること")
            .isEqualTo(listOf(10, 123))
    }

    /**
     * db to dbのテスト。
     *
     * * 処理が正常に終了すること。
     * * 入力テーブルの状態が出力テーブルに書き込めること
     * * トランザクションがコミットされても入力データの読み込みが継続できること
     */
    @Test
    fun db2dbChunkStep() {
        val jobExecution = integrationTestResource.executeBatch("db2db-chunk")

        assertThat(jobExecution.batchStatus)
            .isEqualTo(BatchStatus.COMPLETED)

        assertThat(integrationTestResource.findOutputTable())
            .isEqualTo(integrationTestResource.inputRecords())
    }

    /**
     * db insertでReaderで例外が発生するテスト
     *
     * * 処理済みのChunkまでのデータは処理されていること
     * * 処理は異常終了すること
     */
    @Test
    fun dbInsertChunk_ReaderError() {
        val properties = Properties()
        properties.put("error-point", "10")     // 10件目で例外を送出する
        properties.put("item-count", "4")       // item-countは4
        val jobExecution = integrationTestResource.executeBatch("db2db-chunk-reader-error", properties)

        assertThat(jobExecution.batchStatus)
            .isEqualTo(BatchStatus.FAILED)

        assertThat(integrationTestResource.findOutputTable())
            .`as`("item-countが4で10件目で例外発生なので8レコードは登録されているはず")
            .isEqualTo((1..8).toList())

    }

    /**
     * db insertでReaderで例外が発生するテスト
     *
     * * 処理済みのChunkまでのデータは処理されていること
     * * 処理は異常終了すること
     */
    @Test
    fun dbInsertChunk_processorError() {
        val properties = Properties()
        properties.put("error-point", "13")     // 10件目で例外を送出する
        properties.put("item-count", "5")       // item-countは4
        val jobExecution = integrationTestResource.executeBatch("db2db-chunk-processor-error", properties)

        assertThat(jobExecution.batchStatus)
            .isEqualTo(BatchStatus.FAILED)

        assertThat(integrationTestResource.findOutputTable())
            .`as`("item-countが5で13件目で例外発生なので10レコードは登録されているはず")
            .isEqualTo((1..10).toList())
    }

    /**
     * db insertでWriterで例外が派生するケース
     * 
     * * 処理済みのChunkまでのデータは処理されていること
     * * 処理は異常終了すること
     */
    @Test
    fun dbInsertChunk_writerError() {
        val properties = Properties()
        properties.put("error-point", "60")     // 60件目で例外を送出する
        properties.put("item-count", "50")       // item-countは50
        val jobExecution = integrationTestResource.executeBatch("db2db-chunk-writer-error", properties)

        assertThat(jobExecution.batchStatus)
            .isEqualTo(BatchStatus.FAILED)

        assertThat(integrationTestResource.findOutputTable())
            .`as`("item-countが50で60件目で例外発生なので50レコードは登録されているはず")
            .isEqualTo((1..50).toList())
    }
}
