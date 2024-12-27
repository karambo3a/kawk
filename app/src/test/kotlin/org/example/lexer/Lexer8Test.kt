package org.example.lexer

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class Lexer8Test {
    @Test
    fun singleTokenSource() {
        val testCases = listOf(
            Pair("abc", TokenType.IDENTIFIER),
            Pair("BEGIN", TokenType.KEYWORD),
        )
        testCases.forEach {
            val lexer = TextLexer(it.first)
            val got = lexer.iterator().next()
            assertEquals(it.second, got.type)
            assertEquals(it.first, got.repr)
            assertEquals(Pos(1, 1), got.pos)
        }
    }

    @Test
    fun testFromExample() {
        val testCases = listOf(
            Triple("BEGIN", TokenType.KEYWORD, Pos(1, 1)),
            Triple("{", TokenType.SPECIAL, Pos(1, 7)),
            Triple("FS", TokenType.IDENTIFIER, Pos(1, 8)),
            Triple("=", TokenType.ASSIGN, Pos(1, 11)),
            Triple("\",\"", TokenType.STRING, Pos(1, 13)),
            Triple("}", TokenType.SPECIAL, Pos(1, 16)),
            Triple("$1", TokenType.IDENTIFIER, Pos(1, 18)),
            Triple("==", TokenType.OPERATION, Pos(1, 20)),
            Triple("\"hello\"", TokenType.STRING, Pos(1, 22)),
            Triple("{", TokenType.SPECIAL, Pos(1, 30)),
            Triple("print", TokenType.IDENTIFIER, Pos(1, 31)),
            Triple("NR", TokenType.IDENTIFIER, Pos(1, 37)),
            Triple(",", TokenType.SPECIAL, Pos(1, 39)),
            Triple("$2", TokenType.IDENTIFIER, Pos(1, 41)),
            Triple("/", TokenType.OPERATION, Pos(1, 44)),
            Triple("$3", TokenType.IDENTIFIER, Pos(1, 46)),
            Triple(",", TokenType.SPECIAL, Pos(1, 48)),
            Triple("$2", TokenType.IDENTIFIER, Pos(1, 50)),
            Triple("*", TokenType.OPERATION, Pos(1, 53)),
            Triple("$3", TokenType.IDENTIFIER, Pos(1, 55)),
            Triple("}", TokenType.SPECIAL, Pos(1, 58)),
        )

        val lexer = TextLexer("BEGIN {FS = \",\"} \$1==\"hello\" {print NR, \$2 / \$3, \$2 * \$3 }")
        testCases.forEach { (repr, type, pos) ->
            val got = lexer.iterator().next()
            assertEquals(type, got.type)
            assertEquals(pos, got.pos)
        }
    }

    @Test
    fun testRawString() {
        val lexer = TextLexer("r\"Raw string with escaped symbols: \\ \\t \\n \\r \\$ \\b \"")
        val testCases = listOf(
            Triple(
                "\"Raw string with escaped symbols: \\ \\t \\n \\r \\$ \\b \"", TokenType.STRING, Pos(1, 1)
            )
        )

        testCases.forEach { (repr, expectedType, expectedPos) ->
            val got = lexer.iterator().next()
            assertEquals(expectedType, got.type)
            //assertEquals(repr, got.repr)
            assertEquals(expectedPos, got.pos)
        }
    }

    @Test
    fun testIntegerLiterals() {
        val lexer = TextLexer("65 0x1245F 0b11101 100_000 -34232 20_232_340")
        val testCases = listOf(
            Triple("65", TokenType.INT, Pos(1, 1)),
            Triple("0x1245F", TokenType.INT, Pos(1, 4)),
            Triple("0b11101", TokenType.INT, Pos(1, 12)),
            Triple("100_000", TokenType.INT, Pos(1, 20)),
            Triple("-34232", TokenType.INT, Pos(1, 28)),
            Triple("20_232_340", TokenType.INT, Pos(1, 35))
        )

        val tokens = lexer.iterator()
        testCases.forEach { (repr, expectedType, expectedPos) ->
            val got = tokens.next()
            assertEquals(expectedType, got.type)
            assertEquals(repr, got.repr)
            assertEquals(expectedPos, got.pos)
        }
    }


    @Test
    fun testFixedPointLiterals() {
        val lexer = TextLexer("123.45 123. 0.45 .45 100000.0")
        val testCases = listOf(
            Triple("123.45", TokenType.FIXED_POINT, Pos(1, 1)),
            Triple("123.", TokenType.FIXED_POINT, Pos(1, 8)),
            Triple("0.45", TokenType.FIXED_POINT, Pos(1, 13)),
            Triple(".45", TokenType.FIXED_POINT, Pos(1, 18)),
            Triple("100000.0", TokenType.FIXED_POINT, Pos(1, 22))
        )

        val tokens = lexer.iterator()
        testCases.forEach { (repr, expectedType, expectedPos) ->
            val got = tokens.next()
            assertEquals(expectedType, got.type)
            assertEquals(repr, got.repr)
            assertEquals(expectedPos, got.pos)
        }
    }

    @Test
    fun testOneLineComment() {
        val lexer = TextLexer("123.45 123. 0.45 .45 100000.0 // One line comment")
        val testCases = listOf(
            Triple("123.45", TokenType.FIXED_POINT, Pos(1, 1)),
            Triple("123.", TokenType.FIXED_POINT, Pos(1, 8)),
            Triple("0.45", TokenType.FIXED_POINT, Pos(1, 13)),
            Triple(".45", TokenType.FIXED_POINT, Pos(1, 18)),
            Triple("100000.0", TokenType.FIXED_POINT, Pos(1, 22))
        )

        val tokens = lexer.iterator()
        testCases.forEach { (repr, expectedType, expectedPos) ->
            val got = tokens.next()
            assertEquals(expectedType, got.type)
            assertEquals(repr, got.repr)
            assertEquals(expectedPos, got.pos)
        }
    }

    @Test
    fun testMultiLineComment() {    // add eof
        val lexer = TextLexer(
            """
        123.45 123. 0.45 .45 100000.0 /* Multi-line 
        comment example */ 
        567.89
        """.trimIndent()
        )
        val testCases = listOf(
            Triple("123.45", TokenType.FIXED_POINT, Pos(1, 1)),
            Triple("123.", TokenType.FIXED_POINT, Pos(1, 8)),
            Triple("0.45", TokenType.FIXED_POINT, Pos(1, 13)),
            Triple(".45", TokenType.FIXED_POINT, Pos(1, 18)),
            Triple("100000.0", TokenType.FIXED_POINT, Pos(1, 22)),
            Triple("567.89", TokenType.FIXED_POINT, Pos(3, 1))
        )

        val tokens = lexer.iterator()
        testCases.forEach { (repr, expectedType, expectedPos) ->
            val got = tokens.next()
            assertEquals(expectedType, got.type)
            assertEquals(repr, got.repr)
            assertEquals(expectedPos, got.pos)
        }
    }


    companion object {
        data class ExpectedToken(val type: TokenType, val repr: String, val pos: Pos)
    }
}
