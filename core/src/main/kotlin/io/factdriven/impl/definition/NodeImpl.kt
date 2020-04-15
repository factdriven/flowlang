package io.factdriven.impl.definition

import io.factdriven.definition.*
import io.factdriven.execution.*
import java.lang.IllegalArgumentException
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
abstract class NodeImpl(override val parent: Node?, override val entity: KClass<*> = parent!!.entity):
    Node {

    override val children: MutableList<Node> = mutableListOf()

    override val id: String get() = id()

    override val type: Type get() = entity.type

    override val label: String get() = label()

    override val position: Int get() = index()

    override val first: Node get() = first()
    override val last: Node get() = last()
    override val previous: Node? get() = previous()
    override val next: Node? get() = next()

    protected fun id(): String {
        val parentId =  if (isChild()) parent!!.id else "${entity.type.context}${idSeparator}${entity.type.name}"
        val nodeTypeCount = if (isChild()) parent!!.children.count { it.type == type } else 0
        val nodeTypePos = if (nodeTypeCount > 1) "${positionSeparator}${parent!!.children.count { it.type == type && it.position <= position }}" else ""
        return if (isChild()) "${parentId}${idSeparator}${type.name}${nodeTypePos}" else parentId
    }

    @Suppress("UNCHECKED_CAST")
    override fun <N: Node> find(nodeOfType: KClass<N>, dealingWith: KClass<*>?): N? =
        when {
            nodeOfType.isSubclassOf(Throwing::class) -> {
                children.find { it is Throwing && (it.throwing == dealingWith || dealingWith == null)}
            }
            nodeOfType.isSubclassOf(Promising::class) -> {
                children.find { it is Promising && (it.succeeding == dealingWith || dealingWith == null) }
            }
            else -> {
                children.find { it is Catching && (it.catching == dealingWith || dealingWith == null) }
            }
        } as N?

    @Suppress("UNCHECKED_CAST")
    override fun <N: Node> filter(nodesOfType: KClass<N>, dealingWith: KClass<*>?): List<N> =
        when {
            nodesOfType.isSubclassOf(Throwing::class) -> {
                children.filter { it is Throwing && (it.throwing == dealingWith || dealingWith == null)}
            }
            nodesOfType.isSubclassOf(Promising::class) -> {
                children.filter { it is Promising && (it.succeeding == dealingWith || dealingWith == null) }
            }
            else -> {
                children.filter { it is Catching && (it.catching == dealingWith || dealingWith == null) }
            }
        } as List<N>

    override fun get(id: String): Node? {
        return get(id, Node::class)
    }

    @Suppress("UNCHECKED_CAST")
    override fun <N: Node> get(id: String, type: KClass<in N>): N? {
        if (type.isInstance(this) && this.id == id)
            return this as N?
        else {
            children.forEach {
                val result = it.get(id, type)
                if (result != null)
                    return result
            }
        }
        return null
    }

    override fun isFirstChild(): Boolean = this == first

    override fun isLastChild(): Boolean = this == last

    override fun isParent() = children.isNotEmpty()

    override fun isRoot() = !isChild()

    override fun isChild() = parent != null

    override fun isFirstChildOfRoot() = isChild() && parent!!.isRoot() && isFirstChild()

    override fun isLastChildOfRoot() = isChild() && parent!!.isRoot() && isLastChild()

    override fun findReceptorsFor(message: Message): List<Receptor> {
        return children.map { it.findReceptorsFor(message) }.flatten()
    }

}

private fun Node.label(): String {
    return type.label
}

private fun Node.index(): Int {
    return parent?.children?.indexOf(this) ?: 0
}

private fun Node.first(): Node {
    return if (parent != null) parent!!.children.first() else this
}

private fun Node.last(): Node {
    return if (parent != null) parent!!.children.last() else this
}

private fun Node.previous(): Node? {
    return  if (!isFirstChild()) parent!!.children[position - 1] else null
}

private fun Node.next(): Node? {
    return if (!isLastChild()) parent!!.children[position + 1] else null
}

const val idSeparator = "-"
const val positionSeparator = "."