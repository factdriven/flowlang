package io.factdriven.flow.lang

import io.factdriven.flow.define
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
class FlowDefinitionTest {

    private val flow = define <PaymentRetrieval> {

        on message (RetrievePayment::class) create acceptance(PaymentRetrievalAccepted::class) by { PaymentRetrievalAccepted() }

        execute service {

            create intent ChargeCreditCard::class by { ChargeCreditCard() }
            on message (CreditCardCharged::class) having "reference" match { paymentId }

        }

        create success PaymentRetrieved::class by { PaymentRetrieved() }

    }

    @Test
    fun testMessagePatternCreditCardCharged() {

        val incoming = CreditCardCharged(reference = "value")
        val patterns: MessagePatterns = flow.patterns(incoming)

        assertEquals (1, patterns.size)
        assertEquals (MessagePattern(type = CreditCardCharged::class, properties = mapOf("reference" to "value")), patterns.iterator().next())

    }

    @Test
    fun testMessagePatternRetrievePayment() {

        val incoming = RetrievePayment(payment = 1F)
        val patterns: MessagePatterns = flow.patterns(incoming)

        assertEquals (1, patterns.size)
        assertEquals (MessagePattern(type = RetrievePayment::class), patterns.iterator().next())

    }

    @Test
    fun testMessagePatternPaymentRetrieved() {

        val incoming = PaymentRetrieved(paymentId = "id")
        val patterns: MessagePatterns = flow.patterns(incoming)

        assertEquals (0, patterns.size)

    }

    @Test
    fun testElementIds () {

        assertEquals("PaymentRetrieval", flow.id)
        assertEquals("PaymentRetrieval-RetrievePayment", flow.children[0].id)
        assertEquals("PaymentRetrieval-ChargeCreditCard", flow.children[1].id)
        assertEquals("PaymentRetrieval-ChargeCreditCard-ChargeCreditCard", (flow.children[1] as FlowDefinition).children[0].id)
        assertEquals("PaymentRetrieval-ChargeCreditCard-CreditCardCharged", (flow.children[1] as FlowDefinition).children[1].id)
        assertEquals("PaymentRetrieval-PaymentRetrieved", flow.children[2].id)

    }

    @Test
    fun testDescendants() {

        val descendants = flow.descendants

        assertEquals(6, descendants.size)

    }

    @Test
    fun testDescendantsMap() {

        val element1 = flow.descendantMap["PaymentRetrieval-RetrievePayment"]
        val element2 = flow.descendantMap["PaymentRetrieval-ChargeCreditCard"]
        val element22 = flow.descendantMap["PaymentRetrieval-ChargeCreditCard-CreditCardCharged"]
        val doesNotExist = flow.descendantMap["doesNotExist"]

        assertEquals("PaymentRetrieval-RetrievePayment", element1?.id)
        assertEquals("PaymentRetrieval-ChargeCreditCard", element2?.id)
        assertEquals("PaymentRetrieval-ChargeCreditCard-CreditCardCharged", element22?.id)
        assertEquals(null, doesNotExist?.id)

    }

    @Test
    fun testExpectedPatternForRetrievePayment() {

        val aggregate = null
        val retrievePayment = flow.descendantMap["PaymentRetrieval-RetrievePayment"] as FlowMessageReactionDefinition

        assertEquals(MessagePattern(RetrievePayment::class), retrievePayment.expected(aggregate))

    }

    @Test
    fun testExpectedPatternForCreditCardCharged() {

        val aggregate = PaymentRetrieval(RetrievePayment(id = "anId"))
        val retrievePayment = flow.descendantMap["PaymentRetrieval-ChargeCreditCard-CreditCardCharged"] as FlowMessageReactionDefinition

        assertEquals(MessagePattern(CreditCardCharged::class, mapOf("reference" to "anId")), retrievePayment.expected(aggregate))

    }

    @Test
    fun testMessageClass() {

        assertEquals(RetrievePayment::class, flow.messageType("RetrievePayment"))
        assertEquals(PaymentRetrievalAccepted::class, flow.messageType("PaymentRetrievalAccepted"))
        assertEquals(ChargeCreditCard::class, flow.messageType("ChargeCreditCard"))
        assertEquals(CreditCardCharged::class, flow.messageType("CreditCardCharged"))
        assertEquals(PaymentRetrieved::class, flow.messageType("PaymentRetrieved"))

        assertEquals(null, flow.messageType("NonExistingMessageName"))

    }

}