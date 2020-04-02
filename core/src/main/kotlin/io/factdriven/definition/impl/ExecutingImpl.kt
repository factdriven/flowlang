package io.factdriven.definition.impl

import io.factdriven.definition.Definition
import io.factdriven.definition.Definitions
import io.factdriven.definition.api.Executing
import io.factdriven.definition.api.Node
import kotlin.reflect.KClass

open class ExecutingImpl(parent: Node): Executing, ThrowingImpl(parent) {

    override val catching: KClass<*>
        get() = Definitions.getPromisingNodeByCatchingType(
            throwing
        ).succeeding!!

}