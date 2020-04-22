package io.factdriven.execution.camunda.diagram

class Artefact(val inner: Dimension, val margin: Int): Box() {

    constructor(width: Int, height: Int, margin: Int): this(Dimension(width, height), margin)

    override val dimension: Dimension = Dimension(inner.width + margin * 2, inner.height + margin * 2)

    override val leftEntry: Position
        get() = Position(
            0,
            dimension.height / 2
        )
    override val rightExit: Position
        get() = Position(
            dimension.width,
            dimension.height / 2
        )
    override val topEntry: Position
        get() = Position(
            dimension.width / 2,
            0
        )
    override val bottomExit: Position
        get() = Position(
            dimension.width / 2,
            dimension.height
        )

}