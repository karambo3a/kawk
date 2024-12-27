package org.example.lexer

import kotlin.collections.listOf
import kotlin.test.Test
import kotlin.test.assertEquals

class LexerTest {
    @Test
    fun emptySource() {
        val testCases = listOf(
            TextLexer("").iterator().next(),
            TextLexer("      ").iterator().next(),
            TextLexer("// comment \n  /* comment */").iterator().next()
        )
        val expected = listOf(
            ExpectedToken(TokenType.EOF, "", Pos(1, 1)),
            ExpectedToken(TokenType.EOF, "", Pos(1, 7)),
            ExpectedToken(TokenType.EOF, "", Pos(2, 15)),
        )
        var i = 0
        testCases.forEach {
            assertEquals(it.type, expected[i].type)
            assertEquals(it.repr, expected[i].repr)
            assertEquals(it.pos, expected[i].pos)
            i++
        }
    }

    @Test
    fun testSingleToken() {
        val testCases = listOf(
            ExpectedToken(TokenType.IDENTIFIER, "abc", Pos(1, 1)),
            ExpectedToken(TokenType.KEYWORD, "BEGIN", Pos(1, 1)),
        )
        testCases.forEach {
            val lexer = TextLexer(it.repr)
            val got = lexer.iterator().next()
            assertEquals(it.type, got.type)
            assertEquals(it.repr, got.repr)
            assertEquals(it.pos, got.pos)
        }
    }

    @Test
    fun testSimple() {
        val testCases = listOf(
            ExpectedToken(TokenType.SPECIAL, "{", Pos(1, 1)),
            ExpectedToken(TokenType.IDENTIFIER, "print", Pos(1, 2)),
            ExpectedToken(TokenType.IDENTIFIER, "$1", Pos(1, 8)),
            ExpectedToken(TokenType.SPECIAL, "}", Pos(1, 10)),
            ExpectedToken(TokenType.EOF, "", Pos(1, 11)),
        )
        val lexer = TextLexer("{print $1}")
        testCases.forEach {
            val got = lexer.iterator().next()
            assertEquals(it.type, got.type)
            assertEquals(it.repr, got.repr)
            assertEquals(it.pos, got.pos)
        }
    }

    @Test
    fun test1() {
        val testCases = listOf(
            ExpectedToken(TokenType.KEYWORD, "BEGIN", Pos(1, 1)),
            ExpectedToken(TokenType.SPECIAL, "{", Pos(1, 7)),
            ExpectedToken(TokenType.IDENTIFIER, "FS", Pos(1, 8)),
            ExpectedToken(TokenType.ASSIGN, "=", Pos(1, 11)),
            ExpectedToken(TokenType.STRING, ",", Pos(1, 13)),
            ExpectedToken(TokenType.SPECIAL, "}", Pos(1, 16)),
            ExpectedToken(TokenType.IDENTIFIER, "$1", Pos(1, 18)),
            ExpectedToken(TokenType.OPERATION, "==", Pos(1, 20)),
            ExpectedToken(TokenType.STRING, "hello", Pos(1, 22)),
            ExpectedToken(TokenType.SPECIAL, "{", Pos(1, 30)),
            ExpectedToken(TokenType.IDENTIFIER, "print", Pos(1, 32)),
            ExpectedToken(TokenType.SPECIAL, "(", Pos(1, 38)),
            ExpectedToken(TokenType.IDENTIFIER, "NR", Pos(1, 40)),
            ExpectedToken(TokenType.SPECIAL, ",", Pos(1, 42)),
            ExpectedToken(TokenType.IDENTIFIER, "$2", Pos(1, 44)),
            ExpectedToken(TokenType.OPERATION, "+", Pos(1, 47)),
            ExpectedToken(TokenType.IDENTIFIER, "$3", Pos(1, 49)),
            ExpectedToken(TokenType.SPECIAL, ",", Pos(1, 51)),
            ExpectedToken(TokenType.IDENTIFIER, "$2", Pos(1, 53)),
            ExpectedToken(TokenType.OPERATION, "*", Pos(1, 56)),
            ExpectedToken(TokenType.IDENTIFIER, "$3", Pos(1, 58)),
            ExpectedToken(TokenType.SPECIAL, ")", Pos(1, 61)),
            ExpectedToken(TokenType.SPECIAL, "}", Pos(1, 63)),
            ExpectedToken(TokenType.EOF, "", Pos(1, 64)),
        )
        val lexer = TextLexer("BEGIN {FS = \",\"} \$1==\"hello\" { print ( NR, \$2 + \$3, \$2 * \$3 ) }")
        testCases.forEach {
            val got = lexer.iterator().next()
            assertEquals(it.type, got.type)
            assertEquals(it.repr, got.repr)
            assertEquals(it.pos, got.pos)
        }
    }

    @Test
    fun test2() {
        val testCases = listOf(
            ExpectedToken(TokenType.SPECIAL, "{", Pos(1, 1)),
            ExpectedToken(TokenType.IDENTIFIER, "if", Pos(1, 2)),
            ExpectedToken(TokenType.SPECIAL, "(", Pos(1, 5)),
            ExpectedToken(TokenType.IDENTIFIER, "$0", Pos(1, 6)),
            ExpectedToken(TokenType.OPERATION, ">", Pos(1, 9)),
            ExpectedToken(TokenType.INT, "80", Pos(1, 11)),
            ExpectedToken(TokenType.SPECIAL, ")", Pos(1, 13)),
            ExpectedToken(TokenType.IDENTIFIER, "print", Pos(1, 15)),
            ExpectedToken(TokenType.IDENTIFIER, "$0", Pos(1, 21)),
            ExpectedToken(TokenType.SPECIAL, "}", Pos(1, 23)),
            ExpectedToken(TokenType.EOF, "", Pos(1, 24)),

            )
        val lexer = TextLexer("{if (\$0 > 80) print \$0}")
        testCases.forEach {
            val got = lexer.iterator().next()
            assertEquals(it.type, got.type)
            assertEquals(it.repr, got.repr)
            assertEquals(it.pos, got.pos)
        }
    }


    @Test
    fun testRawString() {
        val testCases = listOf(
            ExpectedToken(TokenType.SPECIAL, "{", Pos(1, 1)),
            ExpectedToken(TokenType.IDENTIFIER, "if", Pos(1, 2)),
            ExpectedToken(TokenType.SPECIAL, "(", Pos(1, 5)),
            ExpectedToken(TokenType.IDENTIFIER, "$1", Pos(1, 6)),
            ExpectedToken(TokenType.OPERATION, "==", Pos(1, 9)),
            ExpectedToken(TokenType.STRING, "wow!", Pos(1, 12)),
            ExpectedToken(TokenType.SPECIAL, ")", Pos(1, 19)),
            ExpectedToken(TokenType.SPECIAL, ";", Pos(1, 20)),
            ExpectedToken(TokenType.IDENTIFIER, "print", Pos(1, 22)),
            ExpectedToken(TokenType.IDENTIFIER, "$0", Pos(1, 28)),
            ExpectedToken(TokenType.SPECIAL, "}", Pos(1, 30)),
            ExpectedToken(TokenType.KEYWORD, "END", Pos(1, 32)),
            ExpectedToken(TokenType.SPECIAL, "{", Pos(1, 36)),
            ExpectedToken(TokenType.IDENTIFIER, "print", Pos(1, 37)),
            ExpectedToken(TokenType.STRING, "done", Pos(1, 43)),
            ExpectedToken(TokenType.SPECIAL, "}", Pos(1, 49)),
            ExpectedToken(TokenType.EOF, "", Pos(1, 50)),
        )
        val lexer = TextLexer("{if ($1 == r\"wow!\"); print $0} END {print \"done\"}")
        testCases.forEach {
            val got = lexer.iterator().next()
            assertEquals(it.type, got.type)
            assertEquals(it.repr, got.repr)
            assertEquals(it.pos, got.pos)
        }
    }

    @Test
    fun testOneLineComments() {
        val testCases = listOf(
            ExpectedToken(TokenType.SPECIAL, "{", Pos(2, 1)),
            ExpectedToken(TokenType.IDENTIFIER, "print", Pos(2, 2)),
            ExpectedToken(TokenType.IDENTIFIER, "$1", Pos(2, 8)),
            ExpectedToken(TokenType.SPECIAL, "}", Pos(2, 10)),
            ExpectedToken(TokenType.EOF, "", Pos(3, 1)),
        )
        val lexer = TextLexer("// comment \n{print $1} # comment \n")
        testCases.forEach {
            val got = lexer.iterator().next()
            assertEquals(it.type, got.type)
            assertEquals(it.repr, got.repr)
            assertEquals(it.pos, got.pos)
        }
    }

    @Test
    fun testMultiLineComments() {
        val testCases = listOf(
            ExpectedToken(TokenType.SPECIAL, "{", Pos(3, 2)),
            ExpectedToken(TokenType.IDENTIFIER, "print", Pos(3, 3)),
            ExpectedToken(TokenType.IDENTIFIER, "$1", Pos(3, 9)),
            ExpectedToken(TokenType.SPECIAL, "}", Pos(3, 11)),
            ExpectedToken(TokenType.EOF, "", Pos(4, 14)),
        )
        val lexer = TextLexer("/* comment \n comment */\n {print $1} \n /* comment */")
        testCases.forEach {
            val got = lexer.iterator().next()
            assertEquals(it.type, got.type)
            assertEquals(it.repr, got.repr)
            assertEquals(it.pos, got.pos)
        }
    }


    companion object {
        data class ExpectedToken(override val type: TokenType, override val repr: String, override val pos: Pos) : Token
    }
}


