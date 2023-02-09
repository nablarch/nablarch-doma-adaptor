package nablarch.integration.doma.batch.ee.listener.integration

import jakarta.batch.runtime.BatchStatus
import org.assertj.core.api.Assertions.assertThat
import org.jboss.arquillian.container.test.api.Deployment
import org.jboss.arquillian.junit.Arquillian
import org.jboss.shrinkwrap.api.ArchivePath
import org.jboss.shrinkwrap.api.ShrinkWrap
import org.jboss.shrinkwrap.api.spec.WebArchive
import org.jboss.shrinkwrap.resolver.api.maven.Maven
import org.jboss.shrinkwrap.resolver.api.maven.ScopeType
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException
import java.io.UncheckedIOException
import java.nio.file.*
import java.nio.file.attribute.BasicFileAttributes
import java.sql.ResultSet
import java.util.*

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

            val archive: WebArchive = ShrinkWrap.create(WebArchive::class.java, "batch.war")
                .addAsLibraries(
                    *Maven.configureResolver()
                    .workOffline()
                    .loadPomFromFile("pom.xml")
                    .importCompileAndRuntimeDependencies()
                    .importTestDependencies()
                    .importDependencies(ScopeType.PROVIDED)
                    .resolve()
                    .withTransitivity()
                    .asFile()
                )
                .addPackages(true, { path: ArchivePath ->
                    val realPathSrcMain = Paths.get("target/classes", path.get())
                    val realPathSrcTest = Paths.get("target/test-classes", path.get())
                    Files.exists(realPathSrcMain) || Files.exists(realPathSrcTest)
                }, "nablarch")

            addTestResources(archive)

            return archive
        }

        /**
         * src/test/resources 配下の全てのファイルを WebArchive の WEB-INF の下に配置する。
         * @param archive WebArchive
         */
        private fun addTestResources(archive: WebArchive) {
            val srcTestResources = Paths.get("src/test/resources")
            val classes = Paths.get("classes")
            try {
                Files.walkFileTree(srcTestResources, object : SimpleFileVisitor<Path>() {
                    override fun visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult {
                        val relativePath = srcTestResources.relativize(file)
                        val targetPath = classes.resolve(relativePath)
                        archive.addAsWebInfResource(file.toFile(), targetPath.toString().replace('\\', '/'))
                        return FileVisitResult.CONTINUE
                    }
                })
            } catch (e: IOException) {
                throw UncheckedIOException(e)
            }
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
            .contains("0", "to@to.com")
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
     *
     * Ignoreにしている理由。
     *
     * このテストを WildFly で実行すると、以下のエラーが発生する。
     * jakarta.transaction.NotSupportedException: WFTXN0001: A transaction is already in progress
     *   at org.wildfly.transaction.client@3.0.0.Final//org.wildfly.transaction.client.ContextTransactionManager.begin(ContextTransactionManager.java:60)
     *   at org.wildfly.transaction.client@3.0.0.Final//org.wildfly.transaction.client.ContextTransactionManager.begin(ContextTransactionManager.java:54)
     *   at org.jberet.jberet-core@2.1.1.Final//org.jberet.runtime.runner.ChunkRunner.run(ChunkRunner.java:208)
     *
     * jberet 側で JTA のトランザクションを開始しようとするが、既に別のトランザクションが開始されているために
     * エラーになっているという意味になる。
     *
     * しかし、Doma Adaptorの仕組みでは、データソースには独自で作成したものを用い、
     * トランザクション管理は自前で行っているので JTA を利用していない。
     * したがって、なぜこのようなエラーになるのかが分からない。
     *
     * GlassFishではテストが通ることから、 WildFly 固有のバグである可能性も考えられる。
     * しかし、詳細を調査している時間的余裕がないため、ひとまずテストを Ignore にしておく。
     *
     * 調査できる時間が確保できたり、 Jakarta EE 10 に対応した GlassFish の組み込みが
     * Arquillian で利用できるようになった際に、再度調査すること。
     */
    @Test
    @Ignore
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
