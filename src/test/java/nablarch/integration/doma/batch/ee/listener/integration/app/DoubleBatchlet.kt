package nablarch.integration.doma.batch.ee.listener.integration.app

import nablarch.integration.doma.*
import jakarta.batch.api.*
import jakarta.enterprise.context.*
import jakarta.inject.*

@Named
@Dependent
class DoubleBatchlet : AbstractBatchlet() {
    override fun process(): String {
        DomaDaoRepository.get(InputDao::class.java).update10Times()
        return "ok"
    }
}