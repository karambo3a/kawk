package org.example.parser

import org.example.ParserException
import org.example.lexer.Pos
import org.example.lexer.Token
import org.example.lexer.TokenType

class Parser(private val tokenIterator: Iterator<Token>) {
    private var currentToken: Token? = if (tokenIterator.hasNext()) tokenIterator.next() else null

    fun parse(): ProgramNode = parseProgram()

    private fun parseProgram(): ProgramNode {
        val pos = currTokenPos()
        val beginConds = mutableListOf<BeginCondNode>()
        val exprConds = mutableListOf<ExprCondNode>()
        val endConds = mutableListOf<EndCondNode>()
        while (currentToken?.type != TokenType.EOF) {
            val block = parseCondBlock() ?: break
            when (block) {
                is BeginCondNode -> beginConds.add(block)
                is ExprCondNode -> exprConds.add(block)
                is EndCondNode -> endConds.add(block)
            }
        }
        val token = currentToken ?: throw ParserException("Unexpected end")
        if (token.type != TokenType.EOF) {
            throw ParserException("Expected '${TokenType.EOF}' at line=${token.pos.line} col=${token.pos.col}")
        }
        return ProgramNode(beginConds, exprConds, endConds, pos)
    }

    private fun parseCondBlock(): CondBlockNode? {
        val token = currentToken ?: return null

        return if (match(TokenType.KEYWORD)) {
            when (token.repr) {
                "BEGIN" -> parseBeginCond()
                "END" -> parseEndCond()
                else -> throw ParserException("Unexpected '${token.repr}' at line=${token.pos.line} col=${token.pos.col}")
            }
        } else if (match(TokenType.SPECIAL)) {
            parseExprCond(null)
        } else {
            parseExprCond(parseExpr())
        }
    }

    private fun parseBeginCond(): BeginCondNode {
        val beginToken = expect("BEGIN")
        return BeginCondNode(parseBlock(), beginToken.pos)
    }

    private fun parseEndCond(): EndCondNode {
        val endToken = expect("END")
        return EndCondNode(parseBlock(), endToken.pos)
    }

    private fun parseExprCond(pattern: ExprNode?): ExprCondNode {
        val pos = currTokenPos()
        return ExprCondNode(pattern, parseBlock(), pos)
    }

    private fun parseBlock(): List<SentenceNode> {
        expect("{")
        val statements = mutableListOf<SentenceNode>()
        while (!match(TokenType.SPECIAL, "}")) {
            if (statements.isNotEmpty() && match(TokenType.SPECIAL, ";")) {
                nextToken()
            } else if (statements.isNotEmpty()) {
                val pos = currTokenPos()
                throw ParserException("Expected ';' at line=${pos.line} col=${pos.col}")
            }
            statements.add(parseSentence())
        }
        expect("}")
        return statements
    }

    private fun parseSentence(): SentenceNode {
        if (!match(TokenType.IDENTIFIER)) {
            val pos = currTokenPos()
            throw ParserException("Unexpected '${currentToken?.repr}' at line=${pos.line} col=${pos.col}")
        }
        val identifierToken = nextToken()
        val identifierNode = IdentifierNode(identifierToken.repr, identifierToken.pos)
        return if (match(TokenType.ASSIGN, "=")) {
            parseAssignment(IdentifierNode(identifierToken.repr, identifierToken.pos))
        } else if (match(TokenType.SPECIAL, "(") || isParam()) {
            parseFuncCall(identifierNode)
        } else {
            val pos = currTokenPos()
            throw ParserException("Unexpected '${currentToken?.repr}' at line=${pos.line} col=${pos.col}")
        }
    }

    private fun parseAssignment(target: IdentifierNode): AssignmentNode {
        nextToken()
        return AssignmentNode(target, parseExpr(), target.pos)
    }

    private fun parseFuncCall(funcNameNode: IdentifierNode): FuncCallNode {
        val params = mutableListOf<ExprNode>()
        if (!match(TokenType.SPECIAL, "(")) {
            params.add(parseExpr())
        } else {
            expect("(")
            while (!match(TokenType.SPECIAL, ")")) {
                if (params.isNotEmpty() && match(TokenType.SPECIAL, ",")) {
                    nextToken()
                } else if (params.isNotEmpty()) {
                    val pos = currTokenPos()
                    throw ParserException("Expected ',' at line=${pos.line} col=${pos.col}")
                }
                params.add(parseExpr())
            }
            expect(")")
        }
        return FuncCallNode(funcNameNode, params, funcNameNode.pos)
    }

    private fun parseExpr(): ExprNode {
        return parseEqExpr()
    }

    private fun parseEqExpr(): ExprNode {
        val pos = currTokenPos()
        val initial = parseRelExpr()
        val expr = mutableListOf<Pair<String, ExprNode>>()
        while (match(TokenType.OPERATION, "==", "!=")) {
            val op = nextToken()
            expr.add(Pair(op.repr, parseRelExpr()))
        }
        return if (expr.isEmpty()) initial else BinaryOpNode(initial, expr, pos)
    }

    private fun parseRelExpr(): ExprNode {
        val pos = currTokenPos()
        val initial = parseAddExpr()
        val expr = mutableListOf<Pair<String, ExprNode>>()
        while (match(TokenType.OPERATION, "<", ">")) {
            val op = nextToken()
            expr.add(Pair(op.repr, parseAddExpr()))
        }
        return if (expr.isEmpty()) initial else BinaryOpNode(initial, expr, pos)
    }

    private fun parseAddExpr(): ExprNode {
        val pos = currTokenPos()
        val initial = parseMulExpr()
        val expr = mutableListOf<Pair<String, ExprNode>>()
        while (match(TokenType.OPERATION, "+", "-")) {
            val op = nextToken()
            expr.add(Pair(op.repr, parseMulExpr()))
        }
        return if (expr.isEmpty()) initial else BinaryOpNode(initial, expr, pos)
    }

    private fun parseMulExpr(): ExprNode {
        val pos = currTokenPos()
        val initial = parseTokenExpr()
        val expr = mutableListOf<Pair<String, ExprNode>>()
        while (match(TokenType.OPERATION, "*", "%", "/")) {
            val op = nextToken()
            expr.add(Pair(op.repr, parseTokenExpr()))
        }
        return if (expr.isEmpty()) initial else BinaryOpNode(initial, expr, pos)
    }

    private fun parseTokenExpr(): ExprNode {
        val token = currentToken ?: throw ParserException("Unexpected end")
        return if (match(TokenType.INT)) {
            nextToken()
            IntNode(token.repr.toLong(), token.pos)
        } else if (match(TokenType.FIXED_POINT)) {
            nextToken()
            FixedPointNode(token.repr.toDouble(), token.pos)
        } else if (match(TokenType.STRING)) {
            nextToken()
            StringNode(token.repr, token.pos)
        } else if (match(TokenType.IDENTIFIER)) {
            nextToken()
            IdentifierNode(token.repr, token.pos)
        } else if (match(TokenType.SPECIAL, "(")) {
            nextToken()
            val expr = parseExpr()
            expect(")")
            BraceExprNode(expr, token.pos)
        } else {
            throw ParserException("Unexpected '${token.repr}' at line=${token.pos.line} col=${token.pos.col}")
        }
    }

    private fun nextToken(): Token {
        val token = currentToken ?: throw ParserException("Unexpected end")
        currentToken = if (tokenIterator.hasNext()) tokenIterator.next() else null
        return token
    }

    private fun expect(repr: String? = null): Token {
        val token = currentToken ?: throw ParserException("Unexpected end")
        if (repr != null && token.repr != repr) {
            throw ParserException("Expected '$repr' at line=${token.pos.line} col=${token.pos.col}")
        }
        nextToken()
        return token
    }

    private fun match(type: TokenType, vararg repr: String = emptyArray()): Boolean {
        return currentToken != null && (type == currentToken?.type && (repr.isEmpty() || repr.contains(currentToken?.repr)))
    }

    private fun currTokenPos(): Pos {
        return currentToken?.pos ?: Pos(-1, -1)
    }

    private fun isParam(): Boolean {
        return currentToken != null && setOf(
            TokenType.INT,
            TokenType.FIXED_POINT,
            TokenType.STRING,
            TokenType.IDENTIFIER
        ).contains(currentToken!!.type)
    }
}