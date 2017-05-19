package nablarch.integration.doma.batch.ee.listener.integration.app

import nablarch.integration.doma.*
import javax.batch.api.chunk.*
import javax.enterprise.context.*
import javax.inject.*

@Named
@Dependent
class RetryErrorWriter : AbstractItemWriter() {
    var error = true
    override fun writeItems(items: MutableList<Any>?) {
        val list = items as List<OutputEntity>
        list.firstOrNull { 
            it.id == 75
        }?.let {
            if (error) {
                error = false
                throw IllegalStateException()
            }
        }
        @Suppress("UNCHECKED_CAST")
        DomaDaoRepository.get(OutputDao::class.java).batchInsert(list)
    }
}