package nablarch.integration.doma.batch.ee.listener.integration.app

import nablarch.common.dao.*
import nablarch.common.mail.*
import nablarch.core.date.*
import javax.batch.api.*
import javax.batch.api.chunk.*
import javax.enterprise.context.*
import javax.inject.*
import javax.persistence.*

@Named
@Dependent
class NablarchIntegrationProcessor : ItemProcessor {
    
    override fun processItem(item: Any?): Any? {
        if (BusinessDateUtil.getDate() != "20170720") {
            throw IllegalStateException("業務日付が想定外")
        }

        return item?.let {
            val o = O()
            o.id = it as Int
            o
        }
    }
}

@Named
@Dependent
class NablarchIntegrationWriter : AbstractItemWriter() {

    @BatchProperty
    @Inject
    var errorMode:String = "false"
    
    override fun writeItems(items: MutableList<Any>?) {
        if (BusinessDateUtil.getDate() != "20170720") {
            throw IllegalStateException("業務日付が想定外")
        }
        UniversalDao.batchInsert(items)

        val mailRequester = MailUtil.getMailRequester()
        items?.forEach { 
            val mailContext = FreeTextMailContext()
            mailContext.addTo("to@mail.com")
            mailContext.subject = "title"
            mailContext.mailBody = "body"
            mailRequester.requestToSend(mailContext)
        }
        
        if (errorMode == "true") {
            if ((items as List<O>).firstOrNull {it.id in listOf(31, 91) } != null) {
                throw IllegalStateException("ろーるばっくされる")
            }
        }
    }
}

@Entity
@Table(name = "input")
class I {
    var id: Int? = null
}

@Entity
@Table(name = "output")
class O {
    var id: Int? = null
}
