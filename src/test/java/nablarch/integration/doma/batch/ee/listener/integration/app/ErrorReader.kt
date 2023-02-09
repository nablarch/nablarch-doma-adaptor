package nablarch.integration.doma.batch.ee.listener.integration.app

import nablarch.integration.doma.*
import java.io.*
import java.util.stream.*
import jakarta.batch.api.*
import jakarta.batch.api.chunk.*
import jakarta.enterprise.context.*
import jakarta.inject.*

@Named
@Dependent
class ErrorReader : AbstractItemReader() {
    private var stream: Stream<Int>? = null
    private lateinit var iterator: Iterator<Int>

    @BatchProperty
    @Inject
    private lateinit var errorPosition: String

    override fun open(checkpoint: Serializable?) {
        stream = DomaDaoRepository.get(InputDao::class.java).find()
        iterator = stream!!.iterator()
    }

    override fun readItem(): Any? {
        return if (iterator.hasNext()) {
            val next = iterator.next()
            if (next == errorPosition.toInt()) {
                throw IllegalStateException("reader error")
            }
            next
        } else {
            null
        }
    }

    override fun close() {
        stream?.close()
    }
}