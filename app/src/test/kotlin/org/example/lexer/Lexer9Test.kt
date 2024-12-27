package org.example.lexer

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class Lexer9Test {
    @Test
    fun emptySource() {        // add eof
        val emptyLexer = TextLexer("")
        val iterator = emptyLexer.iterator()
        assertTrue(iterator.hasNext())
        assertEquals(TokenType.EOF, iterator.next().type)
        assertFalse(iterator.hasNext())
    }

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
    fun tokens() {
        val testCases = listOf(
            Pair(
                "13\npp=10;",
                listOf(
                    ExpectedToken(TokenType.INT, "13", Pos(1, 1)),
                    ExpectedToken(TokenType.IDENTIFIER, "pp", Pos(2, 1)),
                    ExpectedToken(TokenType.ASSIGN, "=", Pos(2, 3)),
                    ExpectedToken(TokenType.INT, "10", Pos(2, 4)),
                    ExpectedToken(TokenType.SPECIAL, ";", Pos(2, 6)),
                ),
            ),
            Pair(
                "1, 38,  5\n pgf =10.4;\n   BEGIN  \n 0x10FA == 0b101",
                listOf(
                    ExpectedToken(TokenType.INT, "1", Pos(1, 1)),
                    ExpectedToken(TokenType.SPECIAL, ",", Pos(1, 2)),
                    ExpectedToken(TokenType.INT, "38", Pos(1, 4)),
                    ExpectedToken(TokenType.SPECIAL, ",", Pos(1, 6)),
                    ExpectedToken(TokenType.INT, "5", Pos(1, 9)),
                    ExpectedToken(TokenType.IDENTIFIER, "pgf", Pos(2, 2)),
                    ExpectedToken(TokenType.ASSIGN, "=", Pos(2, 6)),
                    ExpectedToken(TokenType.FIXED_POINT, "10.4", Pos(2, 7)),
                    ExpectedToken(TokenType.SPECIAL, ";", Pos(2, 11)),
                    ExpectedToken(TokenType.KEYWORD, "BEGIN", Pos(3, 4)),
                    ExpectedToken(TokenType.INT, "0x10FA", Pos(4, 2)),
                    ExpectedToken(TokenType.OPERATION, "==", Pos(4, 9)),
                    ExpectedToken(TokenType.INT, "0b101", Pos(4, 12)),
                ),
            ),
        )
        testCases.forEach {
            val lexer = TextLexer(it.first)
            var i = 0
            while (lexer.iterator().hasNext() && i < it.second.size) {
                val got = lexer.iterator().next()
                val expected = it.second[i]
                assertEquals(expected.type, got.type)
                assertEquals(expected.pos, got.pos)
                i++
            }
            assertEquals(it.second.size, i)
        }
    }

    @Test
    fun comments() {
        val testCases = listOf(
            Pair(
                " 124/*wgf\negrthytg3t 4thj\n\n\n*/// yet another commentp=20\n   \$_p = 32.7;.234313",
                listOf(
                    ExpectedToken(TokenType.INT, "124", Pos(1, 2)),
                    ExpectedToken(TokenType.IDENTIFIER, "\$_p", Pos(6, 4)),
                    ExpectedToken(TokenType.ASSIGN, "=", Pos(6, 8)),
                    ExpectedToken(TokenType.FIXED_POINT, "32.7", Pos(6, 10)),
                    ExpectedToken(TokenType.SPECIAL, ";", Pos(6, 14)),
                    ExpectedToken(TokenType.FIXED_POINT, ".234313", Pos(6, 15)),
                ),
            ),
            Pair(
                "r\"erther\\\\5h\" \n\n rs=\"\\\\\"",
                listOf(
                    ExpectedToken(TokenType.STRING, "erther\\\\5h", Pos(1, 1)),
                    ExpectedToken(TokenType.IDENTIFIER, "rs", Pos(3, 2)),
                    ExpectedToken(TokenType.ASSIGN, "=", Pos(3, 4)),
                    ExpectedToken(TokenType.STRING, "\\", Pos(3, 5)),
                )
            )
        )
        testCases.forEach {
            val lexer = TextLexer(it.first)
            var i = 0
            while (lexer.iterator().hasNext() && i < it.second.size) {
                val got = lexer.iterator().next()
                val expected = it.second[i]
                assertEquals(expected.type, got.type)
                assertEquals(expected.pos, got.pos)
                i++
            }
            assertEquals(it.second.size, i)
        }
    }

    companion object {
        data class ExpectedToken(val type: TokenType, val repr: String, val pos: Pos)
    }
}
