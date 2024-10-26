package org.example.lexer

interface Lexer : Sequence<Token>

data class Pos(val line: Int, val col: Int)

enum class TokenType { 
    OPERATION, ASSIGN, SPECIAL,
    IDENTIFIER, KEYWORD, STRING, INT, FIXED_POINT
}

interface Token {
    val type: TokenType
    val repr: String // представление в исходном тексте
    val pos: Pos
}
 
class TextLexer(src: String): Lexer {
    override fun iterator(): Iterator<Token> {
        TODO("Not yet implemented")
    }
}
