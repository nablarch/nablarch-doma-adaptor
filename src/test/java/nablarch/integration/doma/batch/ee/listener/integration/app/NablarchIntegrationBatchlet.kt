package nablarch.integration.doma.batch.ee.listener.integration.app

import nablarch.common.dao.*
import nablarch.common.mail.*
import nablarch.core.date.*
import nablarch.integration.doma.*
import javax.batch.api.*
import javax.enterprise.context.*
import javax.inject.*
import javax.persistence.*

@Named
@Dependent
class NablarchIntegrationBatchlet : AbstractBatchlet() {
    
    @BatchProperty
    @Inject
    var errorMode:String = "false"
    
    override fun process(): String {

        universalDao()
        doma()
        mailSender()

        if (BusinessDateUtil.getDate() != "20170720") {
            throw IllegalStateException("業務日付が想定外")
        }

        if (errorMode == "true") {
            throw IllegalStateException("ロールバックさせます")
        }
        
        return "ok"
    }

    private fun mailSender() {
        val freeTextMailContext = FreeTextMailContext()
        freeTextMailContext.addTo("to@to.com")
        freeTextMailContext.subject = "title"
        freeTextMailContext.mailBody = "本文"
        MailUtil.getMailRequester()
            .requestToSend(freeTextMailContext)
    }

    private fun doma() {
        DomaDaoRepository.get(OutputDao::class.java)
            .batchInsert(listOf(OutputEntity(2)))
    }

    private fun universalDao() {
        val outputEntity = Output()
        outputEntity.id = 1
        UniversalDao.insert(outputEntity)
    }


    @Entity
    @Table(name = "output")
    class Output {
        @get:Id
        var id:Long? = null
    }
}