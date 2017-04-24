package nablarch.integration.doma.batch.ee.listener.integration.app

import nablarch.integration.doma.*
import javax.batch.api.*
import javax.enterprise.context.*
import javax.inject.*
import kotlin.streams.*

@Named
@Dependent
class Db2DbBatchlet : AbstractBatchlet() {

    override fun process(): String {
        val outputDao = DomaDaoRepository.get(OutputDao::class.java)
        val outputs = DomaDaoRepository.get(InputDao::class.java)
            .find()
            .use {
                it.map(::OutputEntity)
                    .toList()
            }
        outputDao.batchInsert(outputs)
        return "ok"
    }
}

