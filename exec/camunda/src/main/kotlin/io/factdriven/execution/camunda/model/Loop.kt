package io.factdriven.execution.camunda.model

import io.factdriven.definition.*
import io.factdriven.impl.definition.ConditionalImpl

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
class Loop(node: Looping, parent: Element<*,*>): Group<Flow>(node,parent) {

    var join: GatewaySymbol<*>
    var fork: GatewaySymbol<*>

    override val children: List<Element<*, *>> = let {
        join = ExclusiveGatewaySymbol(node.children.last(), this)
        val nodes = node.children.mapNotNull {
            when (it) {
                is Executing -> ServiceTaskSymbol(it, this)
                is Promising -> CatchingEventSymbol(it, this)
                is Awaiting -> ReceiveTaskSymbol(it, this)
                is Throwing -> if (it.isFinish()) ThrowingEventSymbol(it, this) else SendTaskSymbol(it, this)
                is Branching -> Branch(it, this)
                is Looping -> Loop(it, this)
                is Flow -> Sequence(it, this)
                is Conditional -> null
                else -> throw IllegalStateException()
            }
        }
        fork = ExclusiveGatewaySymbol(node.children.last(), this)
        listOf(join) + nodes + fork
    }

    override val paths: List<Path> =
        children.subList(1, children.size).map {
            Path(children.get(children.indexOf(it) - 1), it, this)
        } + Path(fork, join, this, ConditionalImpl<Any>(node))

    override val dimension: Dimension get() = Dimension(children.sumBy { it.dimension.width }, (children.maxBy { it.dimension.height }?.dimension?.height ?: BpmnModel.margin.height) + BpmnModel.margin.height)

    override fun entry(from: Direction): Position {
        return when(from) {
            Direction.North -> Position(dimension.width / 2, 0)
            Direction.East -> Position(dimension.width, (children.maxBy { it.entry(Direction.East).y }?.entry(Direction.East)?.y ?: dimension.height / 2) + BpmnModel.margin.height)
            Direction.South -> Position(dimension.width / 2, dimension.height)
            Direction.West -> Position(0, (children.maxBy { it.entry(Direction.West).y }?.entry(Direction.West)?.y ?: dimension.height / 2) + BpmnModel.margin.height)
        }
    }

    override fun position(child: Element<*,*>): Position {
        return position + entry(Direction.West) - child.entry(Direction.West) +
                Position(children.subList(0, children.indexOf(child)).sumBy { it.dimension.width }, 0)
    }


}