package nablarch.integration.doma.batch.ee.listener.integration.app

import nablarch.integration.doma.*
import org.seasar.doma.jdbc.*
import javax.batch.api.*
import javax.batch.api.chunk.*
import javax.enterprise.context.*
import javax.inject.*

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

