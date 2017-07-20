package nablarch.integration.doma.batch.ee.listener.integration

import org.assertj.core.api.Assertions.*
import org.jboss.arquillian.container.test.api.*
import org.jboss.arquillian.junit.*
import org.jboss.shrinkwrap.api.*
import org.jboss.shrinkwrap.api.spec.*
import org.junit.*
import org.junit.runner.*
import java.sql.*
import java.util.*
import javax.batch.runtime.*

/**
 * JSR352配下でのテスト。
 * 
 * このテストは、他のテストとは異なる接続先に対してテストを行うので、
 * IDEの機能などで他のクラスと同時にテストを実行することは出来ない。
 * 
 * 本モジュールのテストを一括実行したい場合は、Gradleタスクを使用して行うこと。
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

    /**
     * スキップ対象の例外が発生するケース
     *
     * * スキップ対象の例外が発生したChunkのデータは登録されていないこと
     * * 処理は正常終了すること
     */
    @Test
    fun chunk_skipError() {
        val properties = Properties()
        properties.put("error-point", "61")     // 61のデータでエラー
        properties.put("item-count", "50")       // item-countは50
        val jobExecution = integrationTestResource.executeBatch("skip-error", properties)

        assertThat(jobExecution.batchStatus)
            .isEqualTo(BatchStatus.COMPLETED)

        assertThat(integrationTestResource.findOutputTable())
            .`as`("61のデータがエラーでスキップされるのでそれ以外が登録されていること")
            .isEqualTo((1..60) + (62..111))
    }

    /**
     * リトライ対象の例外が発生するケース
     *
     * * リトライが実行され処理が正常終了すること
     */
    @Test
    fun chunk_retryError() {
        val properties = Properties()
        properties.put("item-count", "50")       // item-countは50
        val jobExecution = integrationTestResource.executeBatch("retry-error", properties)

        assertThat(jobExecution.batchStatus)
            .isEqualTo(BatchStatus.COMPLETED)

        assertThat(integrationTestResource.findOutputTable())
            .`as`("リトライされるので全データ登録されていること")
            .isEqualTo((1..111).toList())
    }

    /**
     * 複数のStepからなるジョブのテスト。
     */
    @Test
    fun multiStepJob() {
        val jobExecution = integrationTestResource.executeBatch("multi-step", Properties())

        assertThat(jobExecution.batchStatus)
            .isEqualTo(BatchStatus.COMPLETED)

        assertThat(integrationTestResource.findOutputTable())
            .`as`("batchletで×10した値がChunkでoutputテーブルに書き込まれること")
            .isEqualTo((1..111).map { it * 10 }.toList())
    }

    /**
     トランザクション制御の後ろのリスナの戻り処理で例外が発生するケース。
     
     afterの処理は後ろのリスナから実行されるので、処理が全てロールバックされること。
     */
    @Test
    fun listenerError() {
        integrationTestResource.insertOutputTable(listOf(200))
        val jobExecution = integrationTestResource.executeBatch("listener-error", Properties())

        assertThat(jobExecution.batchStatus)
            .isEqualTo(BatchStatus.FAILED)
        
        assertThat(integrationTestResource.findOutputTable())
            .`as`("output tableはロールバックされ元のレコードのみとなること")
            .isEqualTo(listOf(200))
    }

    @Test
    fun restart() {
        val properties = Properties()
        properties.put("item-count", "30")
        properties.put("error-position", "40")      // 値が40の場合リーダで例外が送出される
        val jobExecution = integrationTestResource.executeBatch("restart", properties)
        val executionId = jobExecution.executionId
        
        assertThat(jobExecution.batchStatus)
            .isEqualTo(BatchStatus.FAILED)
        
        assertThat(integrationTestResource.findOutputTable())
            .`as`("最初のチャンクの30レコードだけ登録されている")
            .isEqualTo((1..30).toList())

        // エラーを発生させないようにする
        properties.put("error-position", "-1")
        val restartJobExecution = integrationTestResource.executeBatch("restart", properties, true, executionId)
        
        assertThat(restartJobExecution.batchStatus)
            .isEqualTo(BatchStatus.COMPLETED)

        assertThat(integrationTestResource.findOutputTable())
            .`as`("リスタートされて最終的に全レコードアウトプットテーブルに登録される")
            .isEqualTo((1..111).toList())

    }

    /**
     * nablarchのデータベースアクセスがbatchletで問題なく使えることを検証する。
     */
    @Test
    fun nablarch_integration_batchlet() {
        val properties = Properties()
        properties.put("errorMode", "false")
        val jobExecution = integrationTestResource.executeBatch("nablarch-integration", properties)

        assertThat(jobExecution.batchStatus)
            .isEqualTo(BatchStatus.COMPLETED)

        // assert output table
        assertThat(integrationTestResource.findOutputTable())
            .isEqualTo((1..2).toList())

        // assert mail
        assertThat(findMailRequest())
            .hasSize(1)
            .first()
            .extracting("subject", "body")
            .contains("title", "本文")

        assertThat(findMailRecipient())
            .hasSize(1)
            .first()
            .extracting("type", "mail_address")
            .contains("1", "to@to.com")
    }
    
    /**
     * nablarchのデータベースアクセスがbatchletで使え、ロールバックした場合はdomaとセットでロールバックされること
     */
    @Test
    fun nablarch_integration_batchlet_rollback() {
        val properties = Properties()
        properties.put("errorMode", "true")
        val jobExecution = integrationTestResource.executeBatch("nablarch-integration", properties)

        assertThat(jobExecution.batchStatus)
            .isEqualTo(BatchStatus.FAILED)

        // assert output table
        assertThat(integrationTestResource.findOutputTable())
            .isEmpty()

        // assert mail
        assertThat(findMailRequest())
            .isEmpty()

        assertThat(findMailRecipient())
            .isEmpty()
    }

    /**
     * nablarchのデータベースアクセスがchunckで問題なく使えることを検証する。
     */
    @Test
    fun nablarch_integration_chunk() {
        val jobExecution = integrationTestResource.executeBatch("nablarch-integration-chunk")

        assertThat(jobExecution.batchStatus)
            .isEqualTo(BatchStatus.COMPLETED)

        // assert output table
        assertThat(integrationTestResource.findOutputTable())
            .isEqualTo((1..111).toList())

        // mail
        assertThat(findMailRequest())
            .hasSize(111)
    }

    /**
     * nablarchのデータベースアクセスがchunckで使え、ロールバックした場合はdomaとセットでロールバックされること
     */
    @Test
    fun nablarch_integration_chunk_rollback() {
        val properties = Properties()
        properties.put("errorMode", "true")
        val jobExecution = integrationTestResource.executeBatch("nablarch-integration-chunk", properties)

        assertThat(jobExecution.batchStatus)
            .isEqualTo(BatchStatus.COMPLETED)

        // assert output table
        assertThat(integrationTestResource.findOutputTable())
            .isEqualTo((1..30).toList() + (61..90))

        assertThat(findMailRequest())
            .hasSize(60)
    }
    
    private fun findMailRequest(): List<Map<String, Any>> {
        return integrationTestResource.connection.createStatement().use {
            it.executeQuery("select * from mail_request").use {
                generateSequence { it }
                    .takeWhile(ResultSet::next)
                    .map {
                        mapOf(
                            "id" to it.getLong("id"),
                            "subject" to it.getString("subject"),
                            "body" to it.getString("body")
                        )
                    }.toList()
            }
        }
    }

    private fun findMailRecipient(): List<Map<String, Any>> {
        return integrationTestResource.connection.createStatement().use {
            it.executeQuery("select * from mail_recipient").use {
                generateSequence { it }
                    .takeWhile(ResultSet::next)
                    .map {
                        mapOf(
                            "mail_address" to it.getString("mail_address"),
                            "type" to it.getString("type")
                        )
                    }.toList()
            }
        }
    }
}
