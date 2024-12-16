package org.example.runtime

import org.example.parser.*
import org.example.runtime.LineReader.Companion.LINE
import org.example.runtime.LineReader.Companion.NF
import java.io.Reader

class Runtime(private val programNode: ProgramNode, input: Reader) : AutoCloseable {
    private val lineReader = LineReader(input)
    private val printFunc = { args: List<ExprNode> ->
        args.forEach { arg -> print("${processNode(arg)} ") }
        println()
    }

    init {
        for (node in programNode.beginCondNode) {
            processNode(node)
        }
        lineReader.readNextLine()
    }

    fun hasNext() = lineReader.lineNotNull()

    fun next() {
        assert(hasNext())
        programNode.exprCondNode.forEach { node -> processNode(node) }
        lineReader.readNextLine()
    }

    private fun processNode(node: ExprNode): String = when (node) {
        is IntNode -> processNode(node)
        is FixedPointNode -> processNode(node)
        is StringNode -> processNode(node)
        is IdentifierNode -> processNode(node)
        is BinaryOpNode -> processNode(node)
        is BraceExprNode -> processNode(node)
    }

    private fun processNode(node: IntNode) = node.value.toString()
    private fun processNode(node: FixedPointNode) = node.value.toString()
    private fun processNode(node: StringNode) = node.value
    private fun processNode(node: IdentifierNode) = lineReader.getValue(node.name)

    private fun processNode(node: BinaryOpNode): String {
        return node.params
            .map { Pair(it.first, processNode(it.second)) }
            .fold(processNode(node.initial)) { acc, elem -> BinaryOpNode.evaluate(acc, elem.first, elem.second) }
    }

    private fun processNode(node: BraceExprNode) = processNode(node.expr)

    private fun processNode(node: SentenceNode) = when (node) {
        is FuncCallNode -> processNode(node)
        is AssignmentNode -> processNode(node)
    }

    private fun processNode(node: FuncCallNode) {
        if (node.name.name != "print") {
            throw IllegalArgumentException("No such function")
        }
        printFunc(node.params)
    }

    private fun processNode(node: AssignmentNode) {
        val newValue = processNode(node.expr)
        val nodeName = node.name.name
        when (true) {
            (node.name.name == NF) -> lineReader.updateNF(newValue)
            (node.name.name == LINE) -> lineReader.updateCurrentLine(newValue)
            (node.name.name.startsWith("$")) ->
                lineReader.updateField(newValue, nodeName.substring(1).toLong(), nodeName)

            else -> lineReader.updateIdentifier(newValue, node.name.name)
        }
    }

    private fun processNode(node: BeginCondNode) {
        node.sentences.forEach { node -> processNode(node) }
    }

    private fun processNode(node: EndCondNode) {
        node.sentences.forEach { node -> processNode(node) }
    }

    private fun processNode(node: ExprCondNode) {
        if (node.expr == null || LiteralNode.getValue(processNode(node.expr)) != 0L) {
            node.sentences.forEach { node -> processNode(node) }
        }
    }

    override fun close() {
        for (node in programNode.endCondNode) {
            processNode(node)
        }
        lineReader.close()
    }


}