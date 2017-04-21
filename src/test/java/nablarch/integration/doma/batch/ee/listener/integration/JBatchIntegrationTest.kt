package nablarch.integration.doma.batch.ee.listener.integration

import nablarch.integration.doma.*
import nablarch.integration.doma.batch.ee.listener.integration.app.*
import nablarch.test.support.*
import org.assertj.core.api.*
import org.assertj.core.api.Assertions.*
import org.jboss.arquillian.container.test.api.*
import org.jboss.arquillian.junit.*
import org.jboss.shrinkwrap.api.*
import org.jboss.shrinkwrap.api.spec.*
import org.junit.*
import org.junit.runner.*
import org.seasar.doma.jdbc.*
import java.io.*
import java.util.concurrent.*
import java.util.stream.*
import javax.batch.api.chunk.*
import javax.batch.runtime.*
import javax.enterprise.context.*
import javax.inject.*
import javax.sql.*

/**
 * JSR352配下でのテスト。
 */
@RunWith(Arquillian::class)
class JBatchIntegrationTest {

    @get:Rule
    val systemRepositoryResource = SystemRepositoryResource(
        "integration-test/datasource.xml")

    @Before
    fun setUp() {
        val dataSource = systemRepositoryResource.getComponentByType<DataSource>(DataSource::class.java)
        dataSource.connection.use { connection ->
            connection.createStatement().use { statement ->
                statement.execute("create table if not exists input (id bigint not null primary key)")
                statement.execute("create table if not exists output (id bigint not null primary key)")
            }
            connection.prepareStatement("insert into input values (?)").use { statement ->
                (1..111).forEach {
                    statement.setInt(1, it)
                    statement.addBatch()
                }
                statement.executeBatch()
            }
        }
    }

    @Named
    @Dependent
    class SimpleReader : AbstractItemReader() {

        private var stream: Stream<Int>? = null
        private lateinit var iterator: Iterator<Int>

        override fun open(checkpoint: Serializable?) {
            stream = DomaDaoRepository.get(InputDao::class.java).find()
            iterator = stream!!.iterator()
        }

        override fun readItem(): Any? {
            return if (iterator.hasNext()) {
                iterator.next()
            } else {
                null
            }
        }

        override fun close() {
            stream?.close()
        }
    }

    @Named
    @Dependent
    class SimpleProcessor : ItemProcessor {
        override fun processItem(item: Any?): Any {
            val itemNo = item as Int
            val selectOptions = SelectOptions.get()
                .forUpdate()
            DomaDaoRepository.get(OutputDao::class.java)
                .lockInput(itemNo, selectOptions)
            return OutputEntity(itemNo)
        }

    }

    @Named
    @Dependent
    class SimpleWriter : AbstractItemWriter() {
        override fun writeItems(items: MutableList<Any>?) {
            @Suppress("UNCHECKED_CAST")
            DomaDaoRepository.get(OutputDao::class.java).batchInsert(items as List<OutputEntity>)
        }
    }

    /**
     * シンプルなチャンクのテスト
     *
     * * 入力テーブルの状態が出力テーブルに書き込めること
     * * トランザクションがコミットされても入力データの読み込みが継続できること
     */
    @Test
    fun simpleChunk() {
        val jobOperator = BatchRuntime.getJobOperator()
        val executionId = jobOperator.start("simple-chunk", null)
        val jobExecution = jobOperator.getJobExecution(executionId)
        while (true) {
            if (jobExecution.endTime != null) {
                break
            }
            TimeUnit.SECONDS.sleep(5)
        }

        assertThat(jobExecution.batchStatus)
            .isEqualTo(BatchStatus.COMPLETED)

        val result = systemRepositoryResource.getComponentByType<DataSource>(DataSource::class.java).connection.use {
            it.createStatement().use {
                it.executeQuery("select * from output order by id").use {
                    generateSequence {
                        it
                    }.takeWhile {
                        it.next()
                    }.map {
                        it.getInt(1)
                    }.toList()
                }
            }
        }

        assertThat(result)
            .isEqualTo((1..111).toList())
    }

    companion object {

        @Deployment
        @JvmStatic
        fun createDeployment(): WebArchive {
            return ShrinkWrap
                .create<WebArchive>(WebArchive::class.java, "batch.war")
                .addPackages(true, "nablarch")
        }
    }
}
