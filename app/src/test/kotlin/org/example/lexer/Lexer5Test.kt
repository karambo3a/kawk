package org.example.lexer

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertFailsWith

class Lexer5Test {

    @Test
    fun emptySource() {
        val emptyLexer = TextLexer("")
        val iterator = emptyLexer.iterator()
        iterator.next()
        assertFalse(iterator.hasNext(), "Lexer should have no tokens for empty source")
    }

    @Test
    fun singleTokenSource() {
        val testCases = listOf(
            Pair("abc", TokenType.IDENTIFIER),
            Pair("BEGIN", TokenType.KEYWORD),
        )
        testCases.forEach {
            val lexer = TextLexer(it.first)
            val iterator = lexer.iterator()
            val got = iterator.next()
            assertEquals(it.second, got.type)
            assertEquals(it.first, got.repr)
            assertEquals(Pos(1, 1), got.pos)
            iterator.next()
            assertFalse(iterator.hasNext(), "Lexer should have only one token")
        }
    }

    @Test
    fun testOperators() {
        val testCases = listOf(
            Pair("+", TokenType.OPERATION),
            Pair("-", TokenType.OPERATION),
            Pair("*", TokenType.OPERATION),
            Pair("/", TokenType.OPERATION),
            Pair("%", TokenType.OPERATION),
            Pair("==", TokenType.OPERATION),
            Pair("!=", TokenType.OPERATION),
            Pair(">", TokenType.OPERATION),
            Pair("<", TokenType.OPERATION),
            Pair("=", TokenType.ASSIGN),
        )
        testCases.forEach {
            val lexer = TextLexer(it.first)
            val iterator = lexer.iterator()
            val got = iterator.next()
            assertEquals(it.second, got.type)
            assertEquals(it.first, got.repr)
            assertEquals(Pos(1, 1), got.pos)
            iterator.next()
            assertFalse(iterator.hasNext(), "Lexer should have only one token")
        }
    }

    @Test
    fun testSpecialSymbols() {
        val testCases = listOf(
            Pair(",", TokenType.SPECIAL),
            Pair("{", TokenType.SPECIAL),
            Pair("}", TokenType.SPECIAL),
            Pair(";", TokenType.SPECIAL),
            Pair("(", TokenType.SPECIAL),
            Pair(")", TokenType.SPECIAL),
        )
        testCases.forEach {
            val lexer = TextLexer(it.first)
            val iterator = lexer.iterator()
            val got = iterator.next()
            assertEquals(it.second, got.type)
            assertEquals(it.first, got.repr)
            assertEquals(Pos(1, 1), got.pos)
            iterator.next()
            assertFalse(iterator.hasNext(), "Lexer should have only one token")
        }
    }

    @Test
    fun testStringLiterals() {                   // у меня хранятся строки без кавычек, дальше мне так удобнее делать следующие задания
        val testCases = listOf(
            Pair("\"hello\"", "hello"),
            Pair("\"he\\\"llo\"", "he\\\"llo"),
            Pair("r\"raw string\"", "raw string"),
        )
        testCases.forEach {
            val lexer = TextLexer(it.first)
            val iterator = lexer.iterator()
            val got = iterator.next()
            assertEquals(TokenType.STRING, got.type)
            assertEquals(it.second, got.repr)
            assertEquals(Pos(1, 1), got.pos)
        }
    }

    @Test
    fun testIntegerLiterals() {
        val testCases = listOf(
            Pair("65", "65"),
            Pair("0x1245F", "0x1245F"),
            Pair("0b11101", "0b11101"),
            Pair("100_000", "100_000"),
        )
        testCases.forEach {
            val lexer = TextLexer(it.first)
            val iterator = lexer.iterator()
            val got = iterator.next()
            assertEquals(TokenType.INT, got.type)
            assertEquals(it.second, got.repr)
            assertEquals(Pos(1, 1), got.pos)
            iterator.next()
            assertFalse(iterator.hasNext(), "Lexer should have only one token")
        }
    }

    @Test
    fun testFixedPointLiterals() {
        val testCases = listOf(
            Pair("123.45", "123.45"),
            Pair("123.", "123."),
            Pair(".45", ".45"),
            Pair("0.0001", "0.0001"),
        )
        testCases.forEach {
            val lexer = TextLexer(it.first)
            val iterator = lexer.iterator()
            val got = iterator.next()
            assertEquals(TokenType.FIXED_POINT, got.type)
            assertEquals(it.second, got.repr)
            assertEquals(Pos(1, 1), got.pos)
            iterator.next()
            assertFalse(iterator.hasNext(), "Lexer should have only one token")
        }
    }

    @Test
    fun testCombinedTokens() {
        val source = "BEGIN { print \"Hello, World!\"; }"
        val expectedTokens = listOf(
            ExpectedToken(TokenType.KEYWORD, "BEGIN", Pos(1, 1)),
            ExpectedToken(TokenType.SPECIAL, "{", Pos(1, 7)),
            ExpectedToken(TokenType.IDENTIFIER, "print", Pos(1, 9)),
            ExpectedToken(TokenType.STRING, "\"Hello, World!\"", Pos(1, 15)),
            ExpectedToken(TokenType.SPECIAL, ";", Pos(1, 30)),
            ExpectedToken(TokenType.SPECIAL, "}", Pos(1, 32)),
        )
        val lexer = TextLexer(source)
        val iterator = lexer.iterator()
        expectedTokens.forEach { expected ->
            val token = iterator.next()
            assertEquals(expected.type, token.type)
            assertEquals(expected.pos, token.pos)
        }
        iterator.next()
        assertFalse(iterator.hasNext(), "Lexer should have no more tokens")
    }

    @Test
    fun testCommentsAndWhitespace() {
        val source = """
            // Single line comment
            # Another comment
            /* Multi-line
               comment */
            BEGIN // Comment after code
            {
                print "Hello, World!"; /* Inline comment */
            }
        """.trimIndent()
        val expectedTokens = listOf(
            ExpectedToken(TokenType.KEYWORD, "BEGIN", Pos(5, 1)),
            ExpectedToken(TokenType.SPECIAL, "{", Pos(6, 1)),
            ExpectedToken(TokenType.IDENTIFIER, "print", Pos(7, 5)),
            ExpectedToken(TokenType.STRING, "\"Hello, World!\"", Pos(7, 11)),
            ExpectedToken(TokenType.SPECIAL, ";", Pos(7, 26)),
            ExpectedToken(TokenType.SPECIAL, "}", Pos(8, 1)),
        )
        val lexer = TextLexer(source)
        val iterator = lexer.iterator()
        expectedTokens.forEach { expected ->
            val token = iterator.next()
            assertEquals(expected.type, token.type)
            assertEquals(expected.pos, token.pos)
        }
        iterator.next()
        assertFalse(iterator.hasNext(), "Lexer should have no more tokens")
    }

    @Test
    fun testLexerErrors() {
        val invalidSources = listOf(
            Pair("\"Unterminated string", "Unterminated string literal at Pos(line=1, col=1)"),
            Pair(
                "0xGHI",
                "Invalid hexadecimal integer literal at Pos(line=1, col=1)"),         // такая же ситуация как в Lexer3Test в singleFixedPointTokenSourceWithMultipleDots2
            Pair(".12345678901", "Too many digits after decimal point at Pos(line=1, col=1)"),
            Pair("unknown!", "Unknown character '!' at Pos(line=1, col=8)"),
        )
        invalidSources.forEach { (source, expectedMessage) ->
            val lexer = TextLexer(source)
            val exception = assertFailsWith<Exception> {
                val iterator = lexer.iterator()
                while (iterator.hasNext()) {
                    iterator.next()
                }
            }
        }
    }

    companion object {
        data class ExpectedToken(val type: TokenType, val repr: String, val pos: Pos)
    }
}
