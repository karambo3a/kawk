package org.example.lexer

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFailsWith

class Lexer6Test {
    @Test fun emptySource() {
        val emptyLexer = TextLexer("")
    }

    @Test fun singleTokenSource() {
        val testCases = listOf(
            "abc" to ExpectedToken(TokenType.IDENTIFIER, "abc", Pos(1, 1)),
            "a1" to ExpectedToken(TokenType.IDENTIFIER, "a1", Pos(1, 1)),
            "BEGIN" to ExpectedToken(TokenType.KEYWORD, "BEGIN", Pos(1, 1)),
            "=" to ExpectedToken(TokenType.ASSIGN, "=", Pos(1, 1)),
            "\$a" to ExpectedToken(TokenType.IDENTIFIER, "\$a", Pos(1, 1)),
            "42" to ExpectedToken(TokenType.INT, "42", Pos(1, 1)),
            "0x1A" to ExpectedToken(TokenType.INT, "0x1A", Pos(1, 1)),
            "\"string\"" to ExpectedToken(TokenType.STRING, "string", Pos(1, 1)),
            "12.34" to ExpectedToken(TokenType.FIXED_POINT, "12.34", Pos(1, 1)),
            "0.12" to ExpectedToken(TokenType.FIXED_POINT, "0.12", Pos(1, 1)),
            "12." to ExpectedToken(TokenType.FIXED_POINT, "12.", Pos(1, 1)),
            ".1" to ExpectedToken(TokenType.FIXED_POINT, ".1", Pos(1, 1))
        )

        testCases.forEach { (input, expected) ->
            val lexer = TextLexer(input)
            val token = lexer.iterator().next()
            assertEquals(expected.type, token.type, message = token.repr)
            assertEquals(expected.pos, token.pos)
        }
    }


    @Test
    fun comments() {
        val lexer = TextLexer("""
            # Это комментарий
            BEGIN // Тоже комментарий
            /* Многострочный
               комментарий */
            END
        """.trimIndent())

        val tokens = lexer.toList()
        assertEquals(2, tokens.size)
        assertEquals(TokenType.KEYWORD, tokens[0].type)
        assertEquals("BEGIN", tokens[0].repr)
        assertEquals(Pos(2, 1), tokens[0].pos)

        assertEquals(TokenType.KEYWORD, tokens[1].type)
        assertEquals("END", tokens[1].repr)
        assertEquals(Pos(5, 1), tokens[1].pos)
    }

    @Test
    fun multipleTokensInLine() {
        val lexer = TextLexer("BEGIN { abc = \"123\" + 0.1 + 1 }")
        val expectedTokens = listOf(
            ExpectedToken(TokenType.KEYWORD, "BEGIN", Pos(1, 1)),
            ExpectedToken(TokenType.SPECIAL, "{", Pos(1, 7)),
            ExpectedToken(TokenType.IDENTIFIER, "abc", Pos(1, 9)),
            ExpectedToken(TokenType.ASSIGN, "=", Pos(1, 13)),
            ExpectedToken(TokenType.STRING, "123", Pos(1, 15)),
            ExpectedToken(TokenType.OPERATION, "+", Pos(1, 21)),
            ExpectedToken(TokenType.FIXED_POINT, "0.1", Pos(1, 23)),
            ExpectedToken(TokenType.OPERATION, "+", Pos(1, 27)),
            ExpectedToken(TokenType.INT, "1", Pos(1, 29)),
            ExpectedToken(TokenType.SPECIAL, "}", Pos(1, 31))
        )

        val tokens = lexer.toList()
        assertEquals(expectedTokens.size, tokens.size)
        tokens.forEachIndexed { index, token ->
            val expected = expectedTokens[index]
            assertEquals(expected.type, token.type)
            assertEquals(expected.pos, token.pos)
        }
    }

    @Test
    fun unknownToken() {
        val lexer = TextLexer("BEGIN . abc")
        val iterator = lexer.iterator()
        iterator.next()
        assertFailsWith<Exception> {
            iterator.next()
        }
    }

    companion object {
        data class ExpectedToken(val type: TokenType, val repr: String, val pos: Pos)
    }
}
