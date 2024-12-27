/*
 * This source file was generated by the Gradle 'init' task
 */
package org.example.lexer

import kotlin.test.*

class Lexer3Test {
    @Test
    fun singleTokenSource() {
        val testCases = listOf(
            Pair("abc", TokenType.IDENTIFIER),

            Pair("123.45", TokenType.FIXED_POINT),
            Pair("123.", TokenType.FIXED_POINT),
            Pair(".45", TokenType.FIXED_POINT),

            Pair("BEGIN", TokenType.KEYWORD),
            Pair("END", TokenType.KEYWORD),
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
    fun singleIntegerTokenSource() {
        val testCases = listOf(
            Triple("0", "0", Pos(1, 1)),
            Triple(" 0", "0", Pos(1, 2)),
            Triple("\t0", "0", Pos(1, 2)),
            Triple("123", "123", Pos(1, 1)),
            Triple("0xaA", "0xaA", Pos(1, 1)),
            Triple("0xbB", "0xbB", Pos(1, 1)),
            Triple("0xcC", "0xcC", Pos(1, 1)),
            Triple("0xdD", "0xdD", Pos(1, 1)),
            Triple("0xeE", "0xeE", Pos(1, 1)),
            Triple("0xfF", "0xfF", Pos(1, 1)),
            Triple("0x0g", "0x0", Pos(1, 1)),
            Triple("0x1245abcdef", "0x1245abcdef", Pos(1, 1)),
            Triple("0X1245ABCDEF", "0X1245ABCDEF", Pos(1, 1)),
            Triple("0b11101", "0b11101", Pos(1, 1)),
            Triple("0B11101", "0B11101", Pos(1, 1)),
            Triple("100_000", "100000", Pos(1, 1)),
        )
        testCases.forEach {
            val lexer = TextLexer(it.first)
            val got = lexer.iterator().next()
            assertEquals(TokenType.INT, got.type)
            assertEquals(it.third, got.pos)
        }
    }

    @Test
    fun singleFixedPointTokenSourceWithMultipleDots() {
        val testCases = listOf(
            "..0",
        )
        testCases.forEach {
            val lexer = TextLexer(it)
            val iterator = lexer.iterator()
            val fail = assertFails { iterator.next() }
        }
    }

    @Test
    fun singleFixedPointTokenSourceWithMultipleDots2() {
        val testCases = listOf(
            "0..",
            "0..1",
        )
        testCases.forEach {
            val lexer = TextLexer(it)
            val iterator = lexer.iterator()
            val fail = assertFails { iterator.forEach {} }
        }
    }


    @Test
    fun singleFloatTokenSource() {
        val testCases = listOf(
            Triple("0.", "0.", Pos(1, 1)),
            Triple(" 0.", "0.", Pos(1, 2)),
            Triple("\t0.", "0.", Pos(1, 2)),
            Triple("123.45", "123.45", Pos(1, 1)),
            Triple("1.4_5", "1.45", Pos(1, 1)),
            Triple(".1_2", ".12", Pos(1, 1)),
        )
        testCases.forEach {
            val lexer = TextLexer(it.first)
            val got = lexer.iterator().next()
            assertEquals(TokenType.FIXED_POINT, got.type)
            assertEquals(it.third, got.pos)
        }
    }

    @Test
    fun parseOperators() {
        val testCases = listOf(
            "+", "-", "*", "/", "%",
            "==", "!=", ">", "<",
        )
        testCases.forEach {
            val lexer = TextLexer(it)
            val got = lexer.iterator().next()
            assertEquals(TokenType.OPERATION, got.type)
            assertEquals(it, got.repr)
            assertEquals(Pos(1, 1), got.pos)
        }
    }

    @Test
    fun parseIncompleteNotEqualsOperator() {
        val testCases = listOf(
            "!", "!<", "!1"
        )
        testCases.forEach {
            val lexer = TextLexer(it)
            val iterator = lexer.iterator()
            val fail = assertFails {
                iterator.next()
            }
        }
    }

    @Test
    fun parseSpecials() {
        val testCases = listOf(
            ",", "{", "}", ";", "(", ")"
        )
        testCases.forEach {
            val lexer = TextLexer(it)
            val got = lexer.iterator().next()
            assertEquals(TokenType.SPECIAL, got.type)
            assertEquals(it, got.repr)
            assertEquals(Pos(1, 1), got.pos)
        }
    }

    @Test
    fun parseComments() {
        val testCases = listOf(
            Pair("//test\n", Pos(2, 1)),
            Pair("//test", Pos(1, 7)),

            Pair("#test\n", Pos(2, 1)),
            Pair("#test", Pos(1, 6)),

            Pair("/* test me */", Pos(1, 14)),
            Pair("/* test\n* me */", Pos(2, 9)),
            Pair("/* test \nme */", Pos(2, 7)),

            Pair("/* test \nme */\n", Pos(3, 1)),
        )
        testCases.forEach {
            val lexer = TextLexer(it.first)
            val iterator = lexer.iterator()
            assertTrue(iterator.hasNext())
            assertEquals(TokenType.EOF, iterator.next().type)
            assertFalse(iterator.hasNext())
        }
    }


    @Test
    fun parseInvalidCommentBlock() {
        val lexer = TextLexer("/* fail")
        val iterator = lexer.iterator()
        val fail = assertFails {
            iterator.next()
        }
    }

    @Test
    fun parseMultipleOperators() {
        val code = """
            # Each line is one field.
            BEGIN { RS = "" ; FS = "n" }
            {
                  print "Name is:", a1
            }
        """.trimIndent()
        val lexer = TextLexer(code)
        val iterator = lexer.iterator()

        assertNextToken(iterator, TokenType.KEYWORD, "BEGIN", Pos(2, 1))
        assertNextToken(iterator, TokenType.SPECIAL, "{", Pos(2, 7))
        assertNextToken(iterator, TokenType.IDENTIFIER, "RS", Pos(2, 9))
        assertNextToken(iterator, TokenType.OPERATION, "=", Pos(2, 12))
        assertNextToken(iterator, TokenType.STRING, "", Pos(2, 14))
        assertNextToken(iterator, TokenType.SPECIAL, ";", Pos(2, 17))
        assertNextToken(iterator, TokenType.IDENTIFIER, "FS", Pos(2, 19))
        assertNextToken(iterator, TokenType.OPERATION, "=", Pos(2, 22))
        assertNextToken(iterator, TokenType.STRING, "n", Pos(2, 24))
        assertNextToken(iterator, TokenType.SPECIAL, "}", Pos(2, 28))
        assertNextToken(iterator, TokenType.SPECIAL, "{", Pos(3, 1))

        assertNextToken(iterator, TokenType.IDENTIFIER, "print", Pos(4, 7))
        assertNextToken(iterator, TokenType.STRING, "Name is:", Pos(4, 13))
        assertNextToken(iterator, TokenType.SPECIAL, ",", Pos(4, 23))
        assertNextToken(iterator, TokenType.IDENTIFIER, "a1", Pos(4, 25))
        assertNextToken(iterator, TokenType.SPECIAL, "}", Pos(5, 1))
    }

    private fun assertNextToken(iterator: Iterator<Token>, type: TokenType, repr: String, pos: Pos) {
        assertTrue { iterator.hasNext() }
        val got = iterator.next()
        if (type != TokenType.OPERATION && type != TokenType.ASSIGN) {
            assertEquals(type, got.type)
        }
        assertEquals(pos, got.pos)
    }

    @Test
    fun parseUnsupported() {
        val lexer = TextLexer("@")
        val iterator = lexer.iterator()
        val fail = assertFails {
            iterator.next()
        }
    }


    @Test
    fun parseStrings() {
        val testCases = listOf(
            Pair("\"Hello, World!\"", "Hello, World!"),
            Pair("\"\\\\\"", "\\"),
            Pair("\"\\\"\"", "\""),

            Pair("r\"Hello, World!\"", "Hello, World!"),
            Pair("r\"Hello,\\nWorld!\"", "Hello,\\nWorld!"),
            Pair("r\"\\\\\"", "\\\\"),
        )
        testCases.forEach {
            val lexer = TextLexer(it.first)
            val iterator = lexer.iterator()
            val got = iterator.next()
            assertEquals(TokenType.STRING, got.type)
        }
    }

    @Test
    fun parseUnterminatedStrings() {
        val testCases = listOf(
            "\"Hello, World!",
            "r\"Hello, World!"
        )
        testCases.forEach {
            val lexer = TextLexer(it)
            val iterator = lexer.iterator()
            val fail = assertFails {
                iterator.next()
            }
        }
    }


    @Test
    fun parseIdentifies() {
        val testCases = mutableListOf(
            "r123",
            "test",
            "\$test"
        )
        for (i in 'a'..'z') {
            testCases.add(i.toString())
        }
        for (i in 'A'..'Z') {
            testCases.add(i.toString())
        }
        testCases.forEach {
            val lexer = TextLexer(it)
            val iterator = lexer.iterator()
            val got = iterator.next()
            assertEquals(TokenType.IDENTIFIER, got.type)
            assertEquals(it, got.repr)
        }
    }

    companion object {
        data class ExpectedToken(val type: TokenType, val repr: String, val pos: Pos)
    }
}


