package org.example.lexer

import org.example.LexerException

interface Lexer : Sequence<Token>

data class Pos(val line: Int, val col: Int)

enum class TokenType {
    OPERATION, ASSIGN, SPECIAL,
    IDENTIFIER, KEYWORD, STRING, INT, FIXED_POINT, EOF
}

interface Token {
    val type: TokenType
    val repr: String
    val pos: Pos
}

class TextLexer(private val src: String) : Lexer {
    private var currPos: Int = 0
    private var line: Int = 1
    private var col: Int = 1

    override fun iterator(): Iterator<Token> = object : Iterator<Token> {
        override fun hasNext(): Boolean {
            if (currPos == src.length) {
                return true
            }
            removeComments()
            return currPos <= src.length
        }

        override fun next(): Token {
            removeComments()
            if (currPos == src.length) {
                currPos++
                return object : Token {
                    override val type: TokenType = TokenType.EOF
                    override val repr: String = ""
                    override val pos: Pos = Pos(line, col)
                }
            }
            checkCurrPos("Error: line=$line col=$col")
            return when (true) {
                regexAssign.matchesAt(src, currPos) -> createToken(regexAssign, TokenType.ASSIGN)
                regexSpecial.matchesAt(src, currPos) -> createToken(regexSpecial, TokenType.SPECIAL)
                regexKeyword.matchesAt(src, currPos) -> createToken(regexKeyword, TokenType.KEYWORD)
                regexString.matchesAt(src, currPos) -> createToken(regexString, TokenType.STRING)
                regexIdentifier.matchesAt(src, currPos) -> createToken(regexIdentifier, TokenType.IDENTIFIER)
                regexFixedPoint.matchesAt(src, currPos) -> createToken(regexFixedPoint, TokenType.FIXED_POINT)
                regexInt.matchesAt(src, currPos) -> createToken(regexInt, TokenType.INT)
                regexOperation.matchesAt(src, currPos) -> createToken(regexOperation, TokenType.OPERATION)
                else -> throw LexerException("No next: line = $line  col = $col")
            }
        }

        private fun removeComments() {
            while (currPos != src.length) {
                if (src[currPos] == ' ' || src[currPos] == '\t') {
                    currPos++
                    col++
                } else if (src[currPos] == '\n' || src[currPos] == '#' || src.startsWith("//", currPos)) {
                    while (currPos < src.length && src[currPos++] != '\n') {
                        col++
                        continue
                    }
                    line++
                    col = 1
                } else if (src.startsWith("/*", currPos)) {
                    while (!src.startsWith("*/", currPos)) {
                        if (src[currPos++] == '\n') {
                            col = 1
                            line++
                        }
                        col++
                        checkCurrPos("No */ in comment: line=$line col=$col")
                    }
                    col += 2
                    currPos += 2
                } else {
                    break
                }
            }
        }

        private fun checkCurrPos(message: String) {
            if (currPos >= src.length) {
                throw LexerException(message)
            }
        }

        private fun createToken(regex: Regex, tokenType: TokenType): Token {
            val matchResult = regex.matchAt(src, currPos)
            if (matchResult == null) {
                throw LexerException("Error: line=$line col=$col")
            }
            currPos += matchResult.value.length
            col += matchResult.value.length
            return object : Token {
                override val type: TokenType = tokenType
                override val repr: String = matchResult.value
                override val pos: Pos = Pos(line, col - matchResult.value.length)
            }
        }
    }

    companion object {
        private val regexOperation = Regex("[+\\-*/%><]|==|!=")
        private val regexAssign = Regex("=(?!=)")
        private val regexSpecial = Regex("[,{}();]")
        private val regexIdentifier = Regex("[\$_a-zA-Z][_a-zA-Z0-9]*")
        private val regexKeyword = Regex("BEGIN|END")
        private val regexString = Regex("r?\"((?:\\\\.|[^\"\\\\])*)\"")
        private val regexInt = Regex("-?(?<![0-9])(?:0b[01]+|0x[0-9A-Fa-f]+|[1-9][0-9]*(?:_[0-9]+)*)(?![0-9_])")
        private val regexFixedPoint = Regex("-?0*[0-9]{0,20}\\.[0-9]{0,10}0*")
    }
}
