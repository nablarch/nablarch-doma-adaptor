package nablarch.integration.doma.batch.ee.listener.integration.app

import nablarch.integration.doma.*
import javax.batch.api.*
import javax.enterprise.context.*
import javax.inject.*

@Named
@Dependent
class DoubleBatchlet : AbstractBatchlet() {
    override fun process(): String {
        DomaDaoRepository.get(InputDao::class.java).update10Times()
        return "ok"
    }
}