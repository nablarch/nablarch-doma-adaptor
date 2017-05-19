package nablarch.integration.doma.batch.ee.listener.integration.app

import nablarch.fw.batch.ee.listener.*
import nablarch.fw.batch.ee.listener.step.*

class ErrorStepListener : AbstractNablarchStepListener() {
    override fun afterStep(context: NablarchListenerContext?) {
        throw IllegalStateException()
    }
}
