package nablarch.integration.doma.batch.ee.listener.integration

import nablarch.core.repository.di.*
import nablarch.core.repository.di.config.xml.*
import org.junit.rules.*
import org.junit.runner.*
import org.junit.runners.model.Statement
import java.sql.*
import java.util.*
import java.util.concurrent.*
import javax.batch.runtime.*
import javax.sql.*

class IntegrationTestResource : ExternalResource() {

    lateinit var connection: Connection

    override fun before() {
        val xmlComponentDefinitionLoader = XmlComponentDefinitionLoader("integration-test/datasource.xml")
        val diContainer = DiContainer(xmlComponentDefinitionLoader)
        val dataSource = diContainer.getComponentByType(DataSource::class.java)
        connection = dataSource.connection
        setupDb()
    }

    override fun after() {
        connection.close()
    }

    override fun apply(base: Statement?, description: Description?): Statement {
        return super.apply(base, description)
    }

    fun findOutputTable(): List<Int> {
        return connection.createStatement().use {
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

    /**
     * 入力のデータベースに入っている処理対象データのリスト
     */
    fun inputRecords() = (1..111).toList()

    fun insertOutputTable(items: List<Int>) {
        connection.prepareStatement("insert into output values (?)").use { st ->
            items.forEach {
                st.setInt(1, it)
                st.addBatch()
            }
            st.executeBatch()
        }
    }

    fun executeBatch(jobId: String, properties: Properties = Properties()): JobExecution {
        val jobOperator = BatchRuntime.getJobOperator()
        val executionId = jobOperator.start(jobId, properties)
        val jobExecution = jobOperator.getJobExecution(executionId)
        while (true) {
            if (jobExecution.endTime != null) {
                break
            }
            TimeUnit.SECONDS.sleep(5)
        }
        return jobExecution
    }

    private fun setupDb() {
        connection.createStatement().use { statement ->
            statement.execute("create table if not exists input (id bigint not null primary key)")
            statement.execute("create table if not exists output (id bigint not null primary key)")
            statement.execute("truncate table input")
            statement.execute("truncate table output")
        }
        connection.prepareStatement("insert into input values (?)").use { statement ->
            inputRecords().forEach {
                statement.setInt(1, it)
                statement.addBatch()
            }
            statement.executeBatch()
        }
    }


}