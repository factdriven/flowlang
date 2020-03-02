package io.factdriven.visualization.examples.payment1

import io.factdriven.definition.Definition
import io.factdriven.visualization.render
import org.junit.jupiter.api.Test

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
class PaymentRetrievalTest {

    @Test
    fun testView() {

        render(Definition.getDefinitionByType(PaymentRetrieval::class))

    }

}