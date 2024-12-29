package org.example.parser

import org.example.lexer.Pos
import java.io.PrintStream

sealed class ASTNode {
    abstract val pos: Pos
}

sealed class ExprNode : ASTNode()

sealed class LiteralNode : ExprNode() {
    abstract fun getValue(): Number

    companion object {
        val regexFixedPoint = Regex("-?0*[0-9]{0,20}\\.[0-9]{0,10}0*")
        val regexInt = Regex("-?(?<![0-9])(?:0b[01]+|0x[0-9A-Fa-f]+|[1-9][0-9]*(?:_[0-9]+)*)(?![0-9_])")
        fun getValue(value: String): Number {
            return regexFixedPoint.matchAt(value, 0)?.value?.toDouble() as? Number ?: regexInt.matchAt(
                value,
                0
            )?.value?.toLong() as? Number ?: 0L as Number
        }
    }
}

data class IntNode(val value: Long, override val pos: Pos) : LiteralNode() {
    override fun getValue() = value
}

data class FixedPointNode(val value: Double, override val pos: Pos) : LiteralNode() {
    override fun getValue() = value
}

data class StringNode(val value: String, override val pos: Pos) : LiteralNode() {
    override fun getValue() = getValue(value)
}

data class IdentifierNode(val name: String, override val pos: Pos) : ExprNode()

data class BinaryOpNode(
    val initial: ExprNode,
    val params: List<Pair<String, ExprNode>>,
    override val pos: Pos
) : ExprNode() {
    companion object {
        fun boolToInt(b: Boolean): Number = if (b) 1 else 0
        fun evaluate(lhs: Double, op: String, rhs: Double) = when (op) {
            "+" -> lhs + rhs
            "-" -> lhs - rhs
            "*" -> lhs * rhs
            "/" -> lhs / rhs
            "%" -> lhs % rhs
            "==" -> boolToInt(lhs == rhs)
            "!=" -> boolToInt(lhs != rhs)
            "<" -> boolToInt(lhs < rhs)
            ">" -> boolToInt(lhs > rhs)
            else -> throw IllegalArgumentException("Unknown operation")
        }

        fun evaluate(lhs: Long, op: String, rhs: Long) = when (op) {
            "+" -> lhs + rhs
            "-" -> lhs - rhs
            "*" -> lhs * rhs
            "/" -> lhs / rhs
            "%" -> lhs % rhs
            "==" -> boolToInt(lhs == rhs)
            "!=" -> boolToInt(lhs != rhs)
            "<" -> boolToInt(lhs < rhs)
            ">" -> boolToInt(lhs > rhs)
            else -> throw IllegalArgumentException("Unknown operation")
        }

        fun evaluate(lhs: Number, op: String, rhs: Number): Number {
            return if (lhs is Float || lhs is Double) {
                evaluate(lhs.toDouble(), op, rhs.toDouble())
            } else if (rhs is Float || rhs is Double) {
                evaluate(lhs.toDouble(), op, rhs.toDouble())
            } else {
                evaluate(lhs.toLong(), op, rhs.toLong())
            }
        }

        fun evaluate(lhs: String, op: String, rhs: String) = when (op) {
            "==" -> boolToInt(lhs == rhs)
            "!=" -> boolToInt(lhs != rhs)
            "<" -> boolToInt(lhs < rhs)
            ">" -> boolToInt(lhs > rhs)
            else -> evaluate(LiteralNode.getValue(lhs), op, LiteralNode.getValue(rhs))
        }.toString()
    }
}

sealed class SentenceNode : ASTNode()

data class EmptySentence(
    override val pos: Pos
) : SentenceNode()

data class FuncCallNode(
    val name: IdentifierNode,
    val params: List<ExprNode>,
    override val pos: Pos
) : SentenceNode()

data class AssignmentNode(
    val name: IdentifierNode,
    val expr: ExprNode,
    override val pos: Pos
) : SentenceNode()

sealed class CondBlockNode : ASTNode()

data class BeginCondNode(
    val sentences: List<SentenceNode>,
    override val pos: Pos
) : CondBlockNode()

data class EndCondNode(
    val sentences: List<SentenceNode>,
    override val pos: Pos
) : CondBlockNode()

data class ExprCondNode(
    val expr: ExprNode?,
    val sentences: List<SentenceNode>,
    override val pos: Pos
) : CondBlockNode()

data class ProgramNode(
    val beginCondNode: List<BeginCondNode>,
    val exprCondNode: List<ExprCondNode>,
    val endCondNode: List<EndCondNode>,
    override val pos: Pos
) : ASTNode()

fun print(out: PrintStream, node: ProgramNode, indent: Int = 0) {
    out.println("${" ".repeat(indent)}ProgramNode(${node.pos})")
    for (block in node.beginCondNode) {
        print(out, block, indent + 1)
    }
    for (block in node.exprCondNode) {
        print(out, block, indent + 1)
    }
    for (block in node.endCondNode) {
        print(out, block, indent + 1)
    }
}

fun print(out: PrintStream, node: BeginCondNode, indent: Int) {
    out.println("${"\t".repeat(indent)}BeginBlockNode(${node.pos})")
    for (sentence in node.sentences) {
        print(out, sentence, indent + 1)
    }
}

fun print(out: PrintStream, node: EndCondNode, indent: Int) {
    out.println("${"\t".repeat(indent)}EndBlockNode(${node.pos})")
    for (sentence in node.sentences) {
        print(out, sentence, indent + 1)
    }
}

fun print(out: PrintStream, node: ExprCondNode, indent: Int) {
    out.println("${"\t".repeat(indent)}ExprCondNode(${node.pos})")
    if (node.expr != null) {
        print(out, node.expr, indent + 1)
    }
    for (sentence in node.sentences) {
        print(out, sentence, indent + 1)
    }
}

fun print(out: PrintStream, node: SentenceNode, indent: Int) {
    when (node) {
        is AssignmentNode -> print(out, node, indent)
        is FuncCallNode -> print(out, node, indent)
        is EmptySentence -> print(out, node, indent)
    }
}

fun print(out: PrintStream, node: AssignmentNode, indent: Int) {
    out.println("${"\t".repeat(indent)}AssignmentNode(${node.pos})")
    print(out, node.name, indent + 1)
    print(out, node.expr, indent + 1)
}

fun print(out: PrintStream, node: EmptySentence, indent: Int) {
    out.println("${"\t".repeat(indent)}EmptySentence(${node.pos})")
}

fun print(out: PrintStream, node: LiteralNode, indent: Int) {
    when (node) {
        is IntNode -> print(out, node, indent)
        is FixedPointNode -> print(out, node, indent)
        is StringNode -> print(out, node, indent)
    }
}

fun print(out: PrintStream, node: ExprNode, indent: Int) {
    when (node) {
        is LiteralNode -> print(out, node, indent)
        is IdentifierNode -> print(out, node, indent)
        is BinaryOpNode -> print(out, node, indent)
    }
}

fun print(out: PrintStream, node: IntNode, indent: Int) {
    out.println("${"\t".repeat(indent)}IntNode(${node.pos}, value=${node.value})")
}

fun print(out: PrintStream, node: FixedPointNode, indent: Int) {
    out.println("${"\t".repeat(indent)}FixedPointNode(${node.pos}, value=${node.value})")
}

fun print(out: PrintStream, node: StringNode, indent: Int) {
    out.println("${"\t".repeat(indent)}StringNode(${node.pos}, value=${node.value})")
}

fun print(out: PrintStream, node: IdentifierNode, indent: Int) {
    out.println("${"\t".repeat(indent)}IdentifierNode(${node.pos}, name=${node.name})")
}

fun print(out: PrintStream, node: BinaryOpNode, indent: Int) {
    out.println("${"\t".repeat(indent)}BinaryOpNode(${node.pos})")
    print(out, node.initial, indent + 1)
    for (param in node.params) {
        out.println("${"\t".repeat(indent + 1)}${param.first}")
        print(out, param.second, indent + 1)
    }
}


fun print(out: PrintStream, node: FuncCallNode, indent: Int) {
    out.println("${"\t".repeat(indent)}FuncCallNode(${node.pos})")
    print(out, node.name, indent + 1)
    for (param in node.params) {
        print(out, param, indent + 1)
    }
}
