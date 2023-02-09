package nablarch.integration.doma.batch.ee.listener.integration

import nablarch.integration.doma.*
import nablarch.integration.doma.batch.ee.listener.integration.app.*
import jakarta.batch.api.chunk.*
import jakarta.enterprise.context.*
import jakarta.inject.*

@Named
@Dependent
class SimpleWriter : AbstractItemWriter() {
    override fun writeItems(items: MutableList<Any>?) {
        @Suppress("UNCHECKED_CAST")
        DomaDaoRepository.get(OutputDao::class.java).batchInsert(items as List<OutputEntity>)
    }
}