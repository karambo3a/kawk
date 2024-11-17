package org.example.parser

import org.example.lexer.Pos
import org.example.lexer.Token
import org.example.lexer.TokenType
import kotlin.collections.listOf
import kotlin.test.Test
import kotlin.test.assertEquals


class ParserTest {
    @Test
    fun test1() {
        val input = listOf(
            ExpectedToken(TokenType.KEYWORD, "BEGIN", Pos(1, 1)),
            ExpectedToken(TokenType.SPECIAL, "{", Pos(1, 7)),
            ExpectedToken(TokenType.IDENTIFIER, "FS", Pos(1, 8)),
            ExpectedToken(TokenType.ASSIGN, "=", Pos(1, 11)),
            ExpectedToken(TokenType.STRING, "\",\"", Pos(1, 13)),
            ExpectedToken(TokenType.SPECIAL, "}", Pos(1, 16)),
            ExpectedToken(TokenType.IDENTIFIER, "$1", Pos(1, 18)),
            ExpectedToken(TokenType.OPERATION, "==", Pos(1, 20)),
            ExpectedToken(TokenType.STRING, "\"hello\"", Pos(1, 22)),
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
        val parser = Parser(input.iterator())
        val actualTree = parser.parse()
        print(System.out, actualTree)
        val expectedTree = ProgramNode(
            listOf(
                BeginCondNode(
                    listOf(
                        AssignmentNode(
                            IdentifierNode("FS", Pos(1, 8)),
                            StringNode("\",\"", Pos(1, 13)),
                            Pos(1, 8)
                        )
                    ), Pos(1, 1)
                )
            ),
            listOf(
                ExprCondNode(
                    BinaryOpNode(
                        IdentifierNode("$1", Pos(1, 18)),
                        listOf(Pair("==", StringNode("\"hello\"", Pos(1, 22)))), Pos(1, 18)
                    ),
                    listOf(
                        FuncCallNode(
                            IdentifierNode("print", Pos(1, 32)),
                            listOf(
                                IdentifierNode("NR", Pos(1, 40)),
                                BinaryOpNode(
                                    IdentifierNode("$2", Pos(1, 44)),
                                    listOf(Pair("+", IdentifierNode("$3", Pos(1, 49)))),
                                    Pos(1, 44)
                                ),
                                BinaryOpNode(
                                    IdentifierNode("$2", Pos(1, 53)),
                                    listOf(Pair("*", IdentifierNode("$3", Pos(1, 58)))),
                                    Pos(1, 53)
                                )
                            ),
                            Pos(1, 32)
                        )
                    ), Pos(1, 30)
                )
            ), emptyList(), Pos(1, 1)
        )
        assertEquals(expectedTree, actualTree)
    }

    @Test
    fun test2() {
        val input = listOf(
            ExpectedToken(TokenType.SPECIAL, "{", Pos(1, 1)),
            ExpectedToken(TokenType.IDENTIFIER, "if", Pos(1, 2)),
            ExpectedToken(TokenType.SPECIAL, "(", Pos(1, 5)),
            ExpectedToken(TokenType.IDENTIFIER, "$0", Pos(1, 6)),
            ExpectedToken(TokenType.OPERATION, ">", Pos(1, 9)),
            ExpectedToken(TokenType.INT, "80", Pos(1, 11)),
            ExpectedToken(TokenType.SPECIAL, ")", Pos(1, 13)),
            ExpectedToken(TokenType.SPECIAL, ";", Pos(1, 13)),
            ExpectedToken(TokenType.IDENTIFIER, "print", Pos(1, 15)),
            ExpectedToken(TokenType.IDENTIFIER, "$0", Pos(1, 21)),
            ExpectedToken(TokenType.SPECIAL, "}", Pos(1, 22)),
            ExpectedToken(TokenType.EOF, "", Pos(1, 23)),
        )
        val parser = Parser(input.iterator())
        val actualTree = parser.parse()
        val expectedTree = ProgramNode(
            emptyList(),
            listOf(
                ExprCondNode(
                    null,
                    listOf(
                        FuncCallNode(
                            IdentifierNode("if", Pos(1, 2)),
                            listOf(
                                BinaryOpNode(
                                    IdentifierNode("$0", Pos(1, 6)),
                                    listOf(Pair(">", IntNode(80, Pos(1, 11)))),
                                    Pos(1, 6)
                                )
                            ),
                            Pos(1, 2)
                        ),
                        FuncCallNode(
                            IdentifierNode("print", Pos(1, 15)),
                            listOf(IdentifierNode("$0", Pos(1, 21))),
                            Pos(1, 15)
                        )
                    ),
                    Pos(1, 1)
                )
            ), emptyList(), Pos(1, 1)
        )
        assertEquals(expectedTree, actualTree)
    }


    @Test
    fun testRawString() {
        val input = listOf(
            ExpectedToken(TokenType.SPECIAL, "{", Pos(1, 1)),
            ExpectedToken(TokenType.IDENTIFIER, "if", Pos(1, 2)),
            ExpectedToken(TokenType.SPECIAL, "(", Pos(1, 5)),
            ExpectedToken(TokenType.IDENTIFIER, "$1", Pos(1, 6)),
            ExpectedToken(TokenType.OPERATION, "==", Pos(1, 9)),
            ExpectedToken(TokenType.STRING, "r\"wow!\"", Pos(1, 12)),
            ExpectedToken(TokenType.SPECIAL, ")", Pos(1, 19)),
            ExpectedToken(TokenType.SPECIAL, ";", Pos(1, 20)),
            ExpectedToken(TokenType.IDENTIFIER, "print", Pos(1, 22)),
            ExpectedToken(TokenType.IDENTIFIER, "$0", Pos(1, 28)),
            ExpectedToken(TokenType.SPECIAL, "}", Pos(1, 30)),
            ExpectedToken(TokenType.KEYWORD, "END", Pos(1, 32)),
            ExpectedToken(TokenType.SPECIAL, "{", Pos(1, 36)),
            ExpectedToken(TokenType.IDENTIFIER, "print", Pos(1, 37)),
            ExpectedToken(TokenType.STRING, "\"done\"", Pos(1, 43)),
            ExpectedToken(TokenType.SPECIAL, "}", Pos(1, 49)),
            ExpectedToken(TokenType.EOF, "", Pos(1, 50)),
        )
        val parser = Parser(input.iterator())
        val actualTree = parser.parse()
        val expectedTree = ProgramNode(
            emptyList(),
            listOf(
                ExprCondNode(
                    null,
                    listOf(
                        FuncCallNode(
                            IdentifierNode("if", Pos(1, 2)),
                            listOf(
                                BinaryOpNode(
                                    IdentifierNode("$1", Pos(1, 6)),
                                    listOf(
                                        Pair(
                                            "==",
                                            StringNode("r\"wow!\"", Pos(1, 12))
                                        )
                                    ),
                                    Pos(1, 6)
                                )
                            ),
                            Pos(1, 2)
                        ),
                        FuncCallNode(
                            IdentifierNode("print", Pos(1, 22)),
                            listOf(IdentifierNode("$0", Pos(1, 28))),
                            Pos(1, 22)
                        )
                    ),
                    Pos(1, 1)
                )
            ),
            listOf(
                EndCondNode(
                    listOf(
                        FuncCallNode(
                            IdentifierNode("print", Pos(1, 37)),
                            listOf(StringNode("\"done\"", Pos(1, 43))),
                            Pos(1, 37)
                        )
                    ), Pos(1, 32)
                )
            ), Pos(1, 1)
        )
        assertEquals(expectedTree, actualTree)
    }

    @Test
    fun testOneLineComments() {
        val input = listOf(
            ExpectedToken(TokenType.SPECIAL, "{", Pos(2, 1)),
            ExpectedToken(TokenType.IDENTIFIER, "print", Pos(2, 2)),
            ExpectedToken(TokenType.IDENTIFIER, "$1", Pos(2, 8)),
            ExpectedToken(TokenType.SPECIAL, "}", Pos(2, 10)),
            ExpectedToken(TokenType.EOF, "", Pos(2, 11)),
        )
        val parser = Parser(input.iterator())
        val actualTree = parser.parse()
        val expectedTree = ProgramNode(
            emptyList(),
            listOf(
                ExprCondNode(
                    null,
                    listOf(
                        FuncCallNode(
                            IdentifierNode("print", Pos(2, 2)),
                            listOf(IdentifierNode("$1", Pos(2, 8))),
                            Pos(2, 2)
                        )
                    ),
                    Pos(2, 1)
                )
            ), emptyList(), Pos(2, 1)
        )
        assertEquals(expectedTree, actualTree)
    }

    @Test
    fun testMultiLineComments() {
        val input = listOf(
            ExpectedToken(TokenType.SPECIAL, "{", Pos(3, 2)),
            ExpectedToken(TokenType.IDENTIFIER, "print", Pos(3, 3)),
            ExpectedToken(TokenType.IDENTIFIER, "$1", Pos(3, 9)),
            ExpectedToken(TokenType.SPECIAL, "}", Pos(3, 11)),
            ExpectedToken(TokenType.EOF, "", Pos(3, 12)),
        )
        val parser = Parser(input.iterator())
        val actualTree = parser.parse()
        val expectedTree = ProgramNode(
            emptyList(),
            listOf(
                ExprCondNode(
                    null,
                    listOf(
                        FuncCallNode(
                            IdentifierNode("print", Pos(3, 3)),
                            listOf(IdentifierNode("$1", Pos(3, 9))),
                            Pos(3, 3)
                        )
                    ),
                    Pos(3, 2)
                )
            ),
            emptyList(),
            Pos(3, 2)
        )
        assertEquals(expectedTree, actualTree)
    }

    companion object {
        data class ExpectedToken(override val type: TokenType, override val repr: String, override val pos: Pos) : Token
    }
}
