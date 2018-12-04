package io.factdriven.flowlang

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */

typealias Message = Any
typealias Messages = List<Message>
typealias Listeners = List<FlowListener<Message>>
typealias FlowInstance = Any

/**
 * Reconstruct the past flow instance state based on a given history of messages.
 * @param history of (consumed and produced) messages
 * @param flow definition
 * @return instance summarizing the state of a specific flow
 */
fun <I: FlowInstance> past(history: Messages, flow: FlowExecutionImpl<I>): I {
    TODO()
}

/**
 * Produce new messages based on a given history of messages and a trigger message.
 * @param history of (consumed and produced) messages
 * @param flow definition
 * @param trigger message coming in and influencing the flow instance
 * @return new messages produced
 */
fun <I: FlowInstance> present(history: Messages, flow: FlowExecutionImpl<I>, trigger: Message): Messages {
    TODO()
}

/**
 * Produce a list of listeners based on a given history of messages and a trigger message.
 * @param history of (consumed and produced) messages
 * @param flow definition
 * @param trigger message coming in and influencing the flow instance
 * @return new message listeners produced
 */
fun <I: FlowInstance> future(history: Messages, flow: FlowExecutionImpl<I>, trigger: Message): Listeners {
    TODO()
}