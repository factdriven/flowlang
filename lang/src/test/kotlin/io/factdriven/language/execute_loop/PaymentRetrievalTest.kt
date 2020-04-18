package io.factdriven.language.execute_loop

import io.factdriven.Flows
import io.factdriven.definition.*
import io.factdriven.language.Execution
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
class PaymentRetrievalTest {

    init {
        Flows.initialize(PaymentRetrieval::class)
        Flows.initialize(CustomerAccount::class)
        Flows.initialize(CreditCardCharge::class)
    }

    @Test
    fun testDefinition() {

        val definition = Flows.get(PaymentRetrieval::class)
        assertEquals(PaymentRetrieval::class, definition.entity)
        assertEquals(3, definition.children.size)

        val instance = PaymentRetrieval(RetrievePayment(3F))

        val on = definition.children[0] as Catching
        assertEquals(PaymentRetrieval::class, on.entity)
        assertEquals(RetrievePayment::class, on.catching)
        assertEquals(definition, on.parent)

        val loop = definition.children[1] as Looping
        assertEquals(PaymentRetrieval::class, loop.entity)
        assertEquals("Payment retrieval", loop.label)
        assertEquals(2, loop.children.size)
        assertTrue(Executing::class.isInstance(loop.children[0]))
        assertTrue(Conditional::class.isInstance(loop.children[1]))

        val emit = definition.children[2] as Throwing
        assertEquals(PaymentRetrieval::class, emit.entity)
        assertEquals(PaymentRetrieved::class, emit.throwing)
        assertEquals(PaymentRetrieved(3F), emit.instance.invoke(instance))
        assertEquals(definition, emit.parent)


    }

}