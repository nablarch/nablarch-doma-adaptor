package nablarch.integration.doma.batch.ee.listener.integration.app

import nablarch.integration.doma.*
import javax.batch.api.*
import javax.batch.api.chunk.*
import javax.enterprise.context.*
import javax.inject.*

@Named
@Dependent
class ErrorWriter : AbstractItemWriter() {

    @BatchProperty
    @Inject
    private lateinit var errorPosition: String

    override fun writeItems(items: MutableList<Any>?) {
        @Suppress("UNCHECKED_CAST")
        DomaDaoRepository.get(OutputDao::class.java).batchInsert(items as List<OutputEntity>)
        if (items.map { it.id }.contains(errorPosition.toInt())) {
            throw IllegalArgumentException("error!!!!")
        }
    }
}