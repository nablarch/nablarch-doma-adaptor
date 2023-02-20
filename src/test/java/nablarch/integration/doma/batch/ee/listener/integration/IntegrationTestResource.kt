package nablarch.integration.doma.batch.ee.listener.integration

import nablarch.core.repository.di.*
import nablarch.core.repository.di.config.xml.*
import org.junit.rules.*
import org.junit.runner.*
import org.junit.runners.model.Statement
import java.sql.*
import java.util.*
import java.util.concurrent.*
import jakarta.batch.runtime.*
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

    fun executeBatch(jobId: String, properties: Properties = Properties(), restart: Boolean = false, restartTarget:Long? = null): JobExecution {
        val jobOperator = BatchRuntime.getJobOperator()
        
        val executionId = if (restart) {
            jobOperator.restart(restartTarget!!, properties)
        } else {
            jobOperator.start(jobId, properties)
        }
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
            statement.execute("""
            create table if not exists mail_request (
              id bigint not null primary key,
              subject varchar(100),
              from_address varchar(100),
              reply varchar(100),
              return varchar(100),
              body varchar(1000),
              charset varchar(100),
              status varchar(10),
              request_dt timestamp,
              send_dt timestamp
            )
            """)
            
            statement.execute("""
            create table if not exists mail_recipient(
                id bigint not null,
                no int not null,
                mail_address varchar(100),
                type varchar(5),
                primary key(id, no)
            )
            """)
            
            statement.execute("""
            create table if not exists mail_file(
                id bigint not null,
                no int not null,
                content_type varchar(100),
                name varchar(100),
                file blob,
                primary key(id, no)
            )
            """)
            
            statement.execute("""
            create table if not exists b_date (
              segment varchar(20) not null primary key,
              dt char(8)
            )
            """)
            
            statement.execute("create sequence if not exists mail_id")
            statement.execute("truncate table input")
            statement.execute("truncate table output")
            statement.execute("truncate table mail_request")
            statement.execute("truncate table mail_recipient")
            statement.execute("truncate table mail_file")
            statement.execute("truncate table b_date")
            statement.execute("insert into b_date values ('default', '20170720')")
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