package nablarch.integration.doma.batch.ee.listener.integration

import nablarch.integration.doma.*
import nablarch.integration.doma.batch.ee.listener.integration.app.*
import java.io.*
import java.util.stream.*
import jakarta.batch.api.chunk.*
import jakarta.enterprise.context.*
import jakarta.inject.*

@Named
@Dependent
class SimpleReader : AbstractItemReader() {

    private var stream: Stream<Int>? = null
    private lateinit var iterator: Iterator<Int>
    private var now:Int? = null

    override fun open(checkpoint: Serializable?) {
        stream = DomaDaoRepository.get(InputDao::class.java).find()
        iterator = stream!!.iterator()
        if (checkpoint is Int) {
            while (iterator.hasNext()) {
                if (iterator.next() == checkpoint) {
                    break
                }
            }
        }
    }

    override fun checkpointInfo(): Serializable? {
        return now
    }

    override fun readItem(): Any? {
        return if (iterator.hasNext()) {
            val next = iterator.next()
            now = next
            next
        } else {
            null
        }
    }

    override fun close() {
        stream?.close()
    }
}