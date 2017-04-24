package nablarch.integration.doma.batch.ee.listener.integration

import nablarch.integration.doma.*
import nablarch.integration.doma.batch.ee.listener.integration.app.*
import java.io.Serializable
import java.util.stream.*
import javax.batch.api.chunk.*
import javax.enterprise.context.*
import javax.inject.*

@Named
@Dependent
class SimpleReader : AbstractItemReader() {

    private var stream: Stream<Int>? = null
    private lateinit var iterator: Iterator<Int>

    override fun open(checkpoint: Serializable?) {
        stream = DomaDaoRepository.get(InputDao::class.java).find()
        iterator = stream!!.iterator()
    }

    override fun readItem(): Any? {
        return if (iterator.hasNext()) {
            iterator.next()
        } else {
            null
        }
    }

    override fun close() {
        stream?.close()
    }
}