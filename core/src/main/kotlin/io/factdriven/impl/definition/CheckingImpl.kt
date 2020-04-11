package io.factdriven.impl.definition

import io.factdriven.definition.Checking
import io.factdriven.definition.Node
import io.factdriven.execution.Type
import io.factdriven.execution.type
import io.factdriven.language.Given
import io.factdriven.language.GivenCondition

open class CheckingImpl<T: Any>(parent: Node):

    Given<T>,

    Checking,
    NodeImpl(parent)

{

    override lateinit var condition: Any.() -> Boolean
    override lateinit var label: String protected set

    override val type: Type
        get() = Type(
            entity.type.context,
            Given::class.java.simpleName.toLowerCase()
        )

    override fun invoke(case: String): Given<T> {
        this.label = case
        return this
    }

    override fun condition(condition: T.() -> Boolean): GivenCondition<T> {
        @Suppress("UNCHECKED_CAST")
        this.condition = condition as Any.() -> Boolean
        return this
    }

}