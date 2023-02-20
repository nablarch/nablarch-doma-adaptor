package nablarch.integration.doma.batch.ee.listener.integration

import nablarch.integration.doma.*
import nablarch.integration.doma.batch.ee.listener.integration.app.*
import org.seasar.doma.jdbc.*
import jakarta.batch.api.chunk.*
import jakarta.enterprise.context.*
import jakarta.inject.*

@Named
@Dependent
class SimpleProcessor : ItemProcessor {
    override fun processItem(item: Any?): Any {
        val itemNo = item as Int
        val selectOptions = SelectOptions.get()
            .forUpdate()
        DomaDaoRepository.get(OutputDao::class.java)
            .lockInput(itemNo, selectOptions)
        return OutputEntity(itemNo)
    }

}