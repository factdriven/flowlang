package io.factdriven.play

import io.factdriven.def.Fact
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
class MessageTest {

    data class SomeFact(val property: String)

    @Test
    fun testMessage() {

        val fact = Fact(SomeFact("value"))
        val message = Message(fact)
        assertNotNull(message.id)
        assertNull(message.sender)
        assertEquals(fact, message.fact)

    }

    @Test
    fun testJson() {

        val message = Message(Fact(SomeFact("value")))
        assertNotNull(message.toJson())
        assertEquals(message, Message.fromJson(message.toJson()))

    }

    @Test
    fun testJsonList() {

        val messages = listOf(Message(Fact(SomeFact("value1"))), Message(
            Fact(SomeFact("value2"))
        ))
        assertEquals(messages, Message.list.fromJson(messages.toJson()))

    }

}
