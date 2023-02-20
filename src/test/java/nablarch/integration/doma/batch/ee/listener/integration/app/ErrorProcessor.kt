package nablarch.integration.doma.batch.ee.listener.integration.app

import nablarch.integration.doma.*
import org.seasar.doma.jdbc.*
import jakarta.batch.api.*
import jakarta.batch.api.chunk.*
import jakarta.enterprise.context.*
import jakarta.inject.*

@Named
@Dependent
class ErrorProcessor : ItemProcessor {

    @BatchProperty
    @Inject
    private lateinit var errorPosition: String

    override fun processItem(item: Any?): Any {
        val itemNo = item as Int
        val selectOptions = SelectOptions.get()
            .forUpdate()
        DomaDaoRepository.get(OutputDao::class.java)
            .lockInput(itemNo, selectOptions)

        if (itemNo == errorPosition.toInt()) {
            throw ProcessorException()
        }
        return OutputEntity(itemNo)
    }

    class ProcessorException : RuntimeException()
}

