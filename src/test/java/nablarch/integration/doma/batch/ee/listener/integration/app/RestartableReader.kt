package nablarch.integration.doma.batch.ee.listener.integration

import nablarch.integration.doma.*
import nablarch.integration.doma.batch.ee.listener.integration.app.*
import java.io.*
import java.util.stream.*
import javax.batch.api.*
import javax.batch.api.chunk.*
import javax.enterprise.context.*
import javax.inject.*

@Named
@Dependent
class RestartableReader : AbstractItemReader() {

    private var stream: Stream<Int>? = null
    private lateinit var iterator: Iterator<Int>
    private var now:Int? = null
    
    @BatchProperty
    @Inject
    private var errorPosition: String = "-1"

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
            now = iterator.next()
            if (now == errorPosition.toInt()) {
                throw RuntimeException("error")
            }
            return now
        } else {
            null
        }
    }

    override fun close() {
        stream?.close()
    }
}