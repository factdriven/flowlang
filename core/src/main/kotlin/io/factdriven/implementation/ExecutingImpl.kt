package io.factdriven.implementation

import io.factdriven.Flows
import io.factdriven.definition.api.Executing
import io.factdriven.definition.api.Node
import io.factdriven.definition.api.Promising
import kotlin.reflect.KClass

open class ExecutingImpl(parent: Node): Executing, ThrowingImpl(parent) {

    override val catching: KClass<*> get() = Flows.get(handling = throwing).find(nodeOfType = Promising::class)!!.succeeding!!

}