package io.factdriven.language.execution.aws.event

import com.amazonaws.services.stepfunctions.model.SendTaskFailureRequest
import com.amazonaws.services.stepfunctions.model.SendTaskSuccessRequest
import com.amazonaws.services.stepfunctions.model.TaskTimedOutException
import io.factdriven.execution.Fact
import io.factdriven.execution.Message
import io.factdriven.language.definition.Correlating
import io.factdriven.language.definition.Waiting
import io.factdriven.language.execution.aws.event.repository.EventRepository
import io.factdriven.language.execution.aws.event.repository.TimerEventRepository
import io.factdriven.language.execution.aws.service.StateMachineService
import io.factdriven.language.execution.aws.lambda.EventContext
import io.factdriven.language.execution.aws.lambda.ProcessContext
import io.factdriven.language.execution.aws.lambda.hashString
import io.factdriven.language.impl.utils.compactJson
import java.time.Duration
import java.time.LocalDateTime
import kotlin.reflect.full.memberProperties

class EventService {

    val eventRepository = EventRepository()
    val timerEventRepository = TimerEventRepository()
    val stateMachineService = StateMachineService()

    fun registerEvent(processContext: ProcessContext, node: Correlating) {
        eventRepository.save(node.consuming.qualifiedName!!,
                processContext.token,
                EventReactionType.SUCCESS,
                extractCorrelationValue(processContext, node),
                processContext.stateMachine.name,
                processContext.messageList)
    }

    private fun extractCorrelationValue(processContext: ProcessContext, node: Correlating): String {
        val references = node.correlating.entries.
                map { e -> e.key to  e.value.invoke(processContext.processInstance).toString()}
                .toMap()
        return hashReference(references)
    }

    fun correlateEvent(eventContext: EventContext){
        val correlating = eventContext.node as Correlating
        val fact = eventContext.event
        val eventReferenceValue = getEventReferenceValue(fact, eventContext.node)
        val eventEntities = eventRepository.getEventsByNameAndReference(correlating.consuming.qualifiedName!!, eventReferenceValue)
        val message = Message(fact.javaClass::class, Fact(fact))

        for(eventEntity in eventEntities){
            val messageList = eventEntity.messages
            correlateToken(eventEntity.token, eventEntity.reactionType, (messageList.plus(message).map { m -> m.compactJson }).compactJson)
        }

        val tokenList = eventEntities.map { t -> t.token }
        eventRepository.deleteTaskTokens(tokenList)
        timerEventRepository.deleteTaskTokens(tokenList)
    }

    private fun getEventReferenceValue(fact: Any, node: Correlating): String {
        val key = node.correlating.entries.map { e -> e.key }.toList()
        val references = fact::class.memberProperties
                .filter {r -> key.contains(r.name)}
                .map { r -> r.name to r.getter.call(fact).toString() }
                .toMap()

        return hashReference(references)
    }

    private fun hashReference(references: Map<String, String>) : String{
        val map = references.toSortedMap()
        return hashString(map.toString())
    }

    fun registerTimerEvent(processContext: ProcessContext, waiting: Waiting){
        val limit = waiting.limit?.invoke(processContext.processInstance)
        val duration = waiting.period?.invoke(processContext.processInstance)
        val dateTime : LocalDateTime = limit ?: LocalDateTime.now().plus(Duration.parse(duration))

        timerEventRepository.save(processContext.token, EventReactionType.SUCCESS, dateTime, processContext.stateMachine.name, processContext.messageList)
    }

    fun correlateTimerEvents(){
        val timerEvents = timerEventRepository.getDueTimerEvents()

        timerEvents.forEach { timerEvent ->
            val messageList = timerEvent.messages
            correlateToken(timerEvent.token, timerEvent.reactionType, (messageList.map { m -> m.compactJson }).compactJson)
        }
        val tokenList = timerEvents.map { timerEvent -> timerEvent.token }.toList()
        timerEventRepository.deleteTaskTokens(tokenList)
        eventRepository.deleteTaskTokens(tokenList)
    }

    private fun correlateToken(taskToken : String, reactionType: EventReactionType, output : String){
        val client = stateMachineService.createClient()
        when(reactionType){
            EventReactionType.SUCCESS -> {
                val sendTaskSuccessRequest = SendTaskSuccessRequest()
                        .withTaskToken(taskToken)
                        .withOutput(output)
                try {
                    client.sendTaskSuccess(sendTaskSuccessRequest)
                } catch (e : TaskTimedOutException){
                    // ignore
                }
            }
            EventReactionType.ERROR -> {
                val sendTaskFailureRequest = SendTaskFailureRequest()
                        .withTaskToken(taskToken)
                        .withError(output)
                try {
                    client.sendTaskFailure(sendTaskFailureRequest)
                } catch (e : TaskTimedOutException){
                    // ignore
                }
            }
            else -> {
                throw ReactionTypeNotSupportedException("this event reaction type has no correlation method")
            }
        }
    }
}