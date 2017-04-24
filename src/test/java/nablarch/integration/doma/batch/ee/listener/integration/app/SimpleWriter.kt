package nablarch.integration.doma.batch.ee.listener.integration

import nablarch.integration.doma.*
import nablarch.integration.doma.batch.ee.listener.integration.app.*
import javax.batch.api.chunk.*
import javax.enterprise.context.*
import javax.inject.*

@Named
@Dependent
class SimpleWriter : AbstractItemWriter() {
    override fun writeItems(items: MutableList<Any>?) {
        @Suppress("UNCHECKED_CAST")
        DomaDaoRepository.get(OutputDao::class.java).batchInsert(items as List<OutputEntity>)
    }
}