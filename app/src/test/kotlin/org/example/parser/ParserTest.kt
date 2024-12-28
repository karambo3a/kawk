package org.example.parser

import org.example.lexer.Pos
import org.example.lexer.TextLexer
import org.example.lexer.Token
import org.example.lexer.TokenType
import kotlin.collections.listOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails


class ParserTest {
    @Test
    fun testEmpty() {
        val parser = Parser(TextLexer("").iterator())
        val actualTree = parser.parse()
        println(actualTree)
    }
    @Test
    fun testEmptyBlock() {
        val parser = Parser(TextLexer("{}").iterator())
        val actualTree = parser.parse()
        println(actualTree)
    }
    @Test
    fun testJustSemi() {
        // sentences ::= | sentence | ';' sentences
        val parser = Parser(TextLexer("{;}").iterator())
        val actualTree = parser.parse()
        println(actualTree)
    }

    @Test
    fun testSemiMany() {
        // sentences ::= | sentence | ';' sentences
        val code = "{;;;;;;;;;;;}"
        val parser = Parser(TextLexer(code).iterator())
        val actualTree = parser.parse()
        println(actualTree)
    }

    @Test
    fun testConstIsNotStatement() {
        // sentence ::= func_call | assignment
        val code = "{1}"
        val parser = Parser(TextLexer(code).iterator())
        assertFails {
            parser.parse()
        }
    }

    @Test
    fun testVarIsNotStatement() {
        // sentence ::= func_call | assignment
        val code = "{v}"
        val parser = Parser(TextLexer(code).iterator())
        assertFails {
            parser.parse()
        }
    }


    @Test
    fun testJustAssign() {
        val code = "{x=5}"
        val parser = Parser(TextLexer(code).iterator())
        val actualTree = parser.parse()
        println(actualTree)
    }

    @Test
    fun testAssignSemi() {
        // должно быть можно, потому что sentences может быть пуст
        val code = "{x=5;}"
        val parser = Parser(TextLexer(code).iterator())
        val actualTree = parser.parse()
        println(actualTree)
    }

    @Test
    fun testAssignSemi2() {
        // должно быть можно, потому что sentences может быть пуст
        val code = "{x=5;;}"
        val parser = Parser(TextLexer(code).iterator())
        val actualTree = parser.parse()
        println(actualTree)
    }

    @Test
    fun testAssignSemi3() {
        val code = "{x=5;x=10}"
        val parser = Parser(TextLexer(code).iterator())
        val actualTree = parser.parse()
        println(actualTree)
    }

    @Test
    fun testFuncNotPrint() {
        val code = "{f(1)}"
        val parser = Parser(TextLexer(code).iterator())
        val actualTree = parser.parse()
        println(actualTree)
    }


    @Test
    fun testFuncNoParams() {
        //func_call ::= IDENTIFIER  '(' params ')' | IDENTIFIER  param
        val code = "{f()}"
        val parser = Parser(TextLexer(code).iterator())
        val actualTree = parser.parse()
        println(actualTree)
    }

    @Test
    fun testFuncManyParams() {
        //func_call ::= IDENTIFIER  '(' params ')' | IDENTIFIER  param
        val code = "{f(1, 2, x)}"
        val parser = Parser(TextLexer(code).iterator())
        val actualTree = parser.parse()
        println(actualTree)
    }

    @Test
    fun testFuncNoParens() {
        //func_call ::= IDENTIFIER  '(' params ')' | IDENTIFIER  param
        val code = "{f 1+123}"
        val parser = Parser(TextLexer(code).iterator())
        val actualTree = parser.parse()
    }


    @Test
    fun testExpr1() {
        val code = "{f = 1 + b + a}"
        val parser = Parser(TextLexer(code).iterator())
        val actualTree = parser.parse()
    }

    @Test
    fun testExpr2() {
        val code = "{f = 1 + (b + 2)}"
        val parser = Parser(TextLexer(code).iterator())
        val actualTree = parser.parse()
    }

    @Test
    fun testMany() {
        val code = "BEGIN{a=1}END{a=1}END{a=1}1{a=1}\"\"{a=1}"
        val parser = Parser(TextLexer(code).iterator())
        val actualTree = parser.parse()
        println(actualTree)
    }


    @Test
    fun testCrazyStaff() {
        val code = listOf(
            "BEGIN{",
            "BEGIN{a=()}",
            "a=()}",
            "a=(){}",
            "$123{",
            "BEGIN",
            "END",
            "{a+}",
            "{a+{}}",
            "{a=f(1)}",
            "{print 1, 2}",
        )
        code.forEach {
            val parser = Parser(TextLexer(it).iterator())
            assertFails {
                parser.parse()
            }
        }
    }

    @Test
    fun test1() {
        // "BEGIN {FS = \",\"} $1==\"hello\" { print ( NR, $2 + $3, $2 * $3 ) }"
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
        // {if ($0 > 80); print $0}
        val input = listOf(
            ExpectedToken(TokenType.SPECIAL, "{", Pos(1, 1)),
            ExpectedToken(TokenType.IDENTIFIER, "if", Pos(1, 2)),
            ExpectedToken(TokenType.SPECIAL, "(", Pos(1, 5)),
            ExpectedToken(TokenType.IDENTIFIER, "$0", Pos(1, 6)),
            ExpectedToken(TokenType.OPERATION, ">", Pos(1, 9)),
            ExpectedToken(TokenType.INT, "80", Pos(1, 11)),
            ExpectedToken(TokenType.SPECIAL, ")", Pos(1, 13)),
            ExpectedToken(TokenType.SPECIAL, ";", Pos(1, 14)),
            ExpectedToken(TokenType.IDENTIFIER, "print", Pos(1, 16)),
            ExpectedToken(TokenType.IDENTIFIER, "$0", Pos(1, 22)),
            ExpectedToken(TokenType.SPECIAL, "}", Pos(1, 24)),
            ExpectedToken(TokenType.EOF, "", Pos(1, 25)),
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
                            IdentifierNode("print", Pos(1, 16)),
                            listOf(IdentifierNode("$0", Pos(1, 22))),
                            Pos(1, 16)
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
        // "{if ($1 == r\"wow!\"); print $0} END {print \"done\"}"
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
        // "// comment \n{print $1} # comment "
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
        // "/* comment \n comment */\n {print $1} \n /* comment */"
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

    @Test
    fun testEvalConst() {
        // "BEGIN {FS = \",\"} $1==\"hello\" { print ( NR, $2 + 3 + $3 + 3, $2 * $3 * 3 * 3, $4 - 1 - 2)}"
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
            ExpectedToken(TokenType.INT, "3", Pos(1, 49)),
            ExpectedToken(TokenType.OPERATION, "+", Pos(1, 51)),
            ExpectedToken(TokenType.IDENTIFIER, "$3", Pos(1, 53)),
            ExpectedToken(TokenType.OPERATION, "+", Pos(1, 56)),
            ExpectedToken(TokenType.INT, "3", Pos(1, 58)),
            ExpectedToken(TokenType.SPECIAL, ",", Pos(1, 59)),
            ExpectedToken(TokenType.IDENTIFIER, "$2", Pos(1, 61)),
            ExpectedToken(TokenType.OPERATION, "*", Pos(1, 64)),
            ExpectedToken(TokenType.IDENTIFIER, "$3", Pos(1, 66)),
            ExpectedToken(TokenType.OPERATION, "*", Pos(1, 69)),
            ExpectedToken(TokenType.INT, "3", Pos(1, 71)),
            ExpectedToken(TokenType.OPERATION, "*", Pos(1, 73)),
            ExpectedToken(TokenType.INT, "3", Pos(1, 75)),
//            ExpectedToken(TokenType.SPECIAL, ",", Pos(1, 76)),
//            ExpectedToken(TokenType.IDENTIFIER, "$4", Pos(1, 78)),
//            ExpectedToken(TokenType.OPERATION, "-", Pos(1, 80)),
//            ExpectedToken(TokenType.INT, "1", Pos(1, 82)),
//            ExpectedToken(TokenType.OPERATION, "-", Pos(1, 84)),
//            ExpectedToken(TokenType.INT, "2", Pos(1, 86)),
            ExpectedToken(TokenType.SPECIAL, ")", Pos(1, 87)),
            ExpectedToken(TokenType.SPECIAL, "}", Pos(1, 88)),
            ExpectedToken(TokenType.EOF, "", Pos(1, 89)),
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
                                    listOf(
                                        Pair("+", IdentifierNode("$3", Pos(1, 53))),
                                        Pair("+", IntNode(6, Pos(1, 44)))
                                    ),
                                    Pos(1, 44)
                                ),
                                BinaryOpNode(
                                    IdentifierNode("$2", Pos(1, 61)),
                                    listOf(
                                        Pair("*", IdentifierNode("$3", Pos(1, 66))),
                                        Pair("*", IntNode(9, Pos(1, 61)))
                                    ),
                                    Pos(1, 61)
                                ),
//                                BinaryOpNode(
//                                    IdentifierNode("$4", Pos(1, 78)),
//                                    listOf(
//                                        Pair("-", IntNode(3, Pos(1, 78)))
//                                    ),
//                                    Pos(1, 78)
//                                ),
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
    fun testEvalFloatConst() {
        // "{print(1.1 + 5.4, 1.1 / 1.1)}"
        val input = listOf(
            ExpectedToken(TokenType.SPECIAL, "{", Pos(1, 1)),
            ExpectedToken(TokenType.IDENTIFIER, "print", Pos(1, 2)),
            ExpectedToken(TokenType.SPECIAL, "(", Pos(1, 7)),
            ExpectedToken(TokenType.FIXED_POINT, "1.1", Pos(1, 8)),
            ExpectedToken(TokenType.OPERATION, "+", Pos(1, 12)),
            ExpectedToken(TokenType.FIXED_POINT, "5.4", Pos(1, 14)),
            ExpectedToken(TokenType.SPECIAL, ",", Pos(1, 18)),
            ExpectedToken(TokenType.FIXED_POINT, "1.1", Pos(1, 20)),
            ExpectedToken(TokenType.OPERATION, "/", Pos(1, 24)),
            ExpectedToken(TokenType.FIXED_POINT, "1.1", Pos(1, 26)),
            ExpectedToken(TokenType.SPECIAL, ")", Pos(1, 30)),
            ExpectedToken(TokenType.SPECIAL, "}", Pos(1, 31)),
            ExpectedToken(TokenType.EOF, "", Pos(1, 32)),
        )
        val parser = Parser(input.iterator())
        val actualTree = parser.parse()
        val expectedTree = ProgramNode(
            listOf(),
            listOf(
                ExprCondNode(
                    null,
                    listOf(
                        FuncCallNode(
                            IdentifierNode("print", Pos(1, 2)),
                            listOf(FixedPointNode(6.5, Pos(1, 8)), FixedPointNode(1.0, Pos(1, 20))),
                            Pos(1, 2)
                        )
                    ),
                    Pos(1, 1)
                )
            ),
            listOf(),
            Pos(1, 1)
        )
        assertEquals(expectedTree, actualTree)
    }

    @Test
    fun testEvalString() {
        // "{print(\"1.1\" + \"5.4\", \"10\" / \"2\", \"123.qq\" + \"12\", \"-123\" + \"12\", '--123' + '12q')}"
        val input = listOf(
            ExpectedToken(TokenType.SPECIAL, "{", Pos(1, 1)),
            ExpectedToken(TokenType.IDENTIFIER, "print", Pos(1, 2)),
            ExpectedToken(TokenType.SPECIAL, "(", Pos(1, 7)),

            ExpectedToken(TokenType.STRING, "1.1", Pos(1, 8)),
            ExpectedToken(TokenType.OPERATION, "+", Pos(1, 14)),
            ExpectedToken(TokenType.STRING, "5.4", Pos(1, 16)),
            ExpectedToken(TokenType.SPECIAL, ",", Pos(1, 22)),

            ExpectedToken(TokenType.STRING, "10", Pos(1, 24)),
            ExpectedToken(TokenType.OPERATION, "/", Pos(1, 29)),
            ExpectedToken(TokenType.STRING, "2", Pos(1, 31)),
            ExpectedToken(TokenType.SPECIAL, ",", Pos(1, 32)),

            ExpectedToken(TokenType.STRING, "123.qq", Pos(1, 25)),
            ExpectedToken(TokenType.OPERATION, "/", Pos(1, 35)),
            ExpectedToken(TokenType.STRING, "12", Pos(1, 37)),
            ExpectedToken(TokenType.SPECIAL, ",", Pos(1, 32)),

            ExpectedToken(TokenType.STRING, "-123", Pos(1, 34)),
            ExpectedToken(TokenType.OPERATION, "+", Pos(1, 41)),
            ExpectedToken(TokenType.STRING, "12", Pos(1, 43)),
            ExpectedToken(TokenType.SPECIAL, ",", Pos(1, 44)),

            ExpectedToken(TokenType.STRING, "--123", Pos(1, 46)),
            ExpectedToken(TokenType.OPERATION, "+", Pos(1, 54)),
            ExpectedToken(TokenType.STRING, "12q", Pos(1, 56)),

            ExpectedToken(TokenType.SPECIAL, ")", Pos(1, 62)),
            ExpectedToken(TokenType.SPECIAL, "}", Pos(1, 63)),
            ExpectedToken(TokenType.EOF, "", Pos(1, 64)),
        )
        val parser = Parser(input.iterator())
        val actualTree = parser.parse()
        print(System.out, actualTree)
        val expectedTree = ProgramNode(
            listOf(),
            listOf(
                ExprCondNode(
                    null,
                    listOf(
                        FuncCallNode(
                            IdentifierNode("print", Pos(1, 2)),
                            listOf(
                                FixedPointNode(6.5, Pos(1, 8)),
                                IntNode(5, Pos(1, 24)),
                                FixedPointNode(10.25, Pos(1, 25)),
                                IntNode(-111, Pos(1, 34)),
                                IntNode(12, Pos(1, 46))
                            ),
                            Pos(1, 2)
                        )
                    ),
                    Pos(1, 1)
                )
            ),
            listOf(),
            Pos(1, 1)
        )
        assertEquals(expectedTree, actualTree)
    }

    @Test
    fun testEvalIntFixedPointString() {
        // "{print(\"1.1\" + 5, \"10q\" / 2.5, 2.5 * \"10\", \"30\" - 15, \"someString\" * 100)}"
        val input = listOf(
            ExpectedToken(TokenType.SPECIAL, "{", Pos(1, 1)),
            ExpectedToken(TokenType.IDENTIFIER, "print", Pos(1, 2)),
            ExpectedToken(TokenType.SPECIAL, "(", Pos(1, 7)),

            ExpectedToken(TokenType.STRING, "1.1", Pos(1, 8)),
            ExpectedToken(TokenType.OPERATION, "+", Pos(1, 14)),
            ExpectedToken(TokenType.INT, "5", Pos(1, 16)),
            ExpectedToken(TokenType.SPECIAL, ",", Pos(1, 17)),

            ExpectedToken(TokenType.STRING, "10q", Pos(1, 19)),
            ExpectedToken(TokenType.OPERATION, "/", Pos(1, 25)),
            ExpectedToken(TokenType.FIXED_POINT, "2.5", Pos(1, 26)),
            ExpectedToken(TokenType.SPECIAL, ",", Pos(1, 31)),

            ExpectedToken(TokenType.FIXED_POINT, "2.5", Pos(1, 33)),
            ExpectedToken(TokenType.OPERATION, "*", Pos(1, 37)),
            ExpectedToken(TokenType.STRING, "10", Pos(1, 39)),
            ExpectedToken(TokenType.SPECIAL, ",", Pos(1, 44)),

            ExpectedToken(TokenType.STRING, "30", Pos(1, 46)),
            ExpectedToken(TokenType.OPERATION, "-", Pos(1, 52)),
            ExpectedToken(TokenType.INT, "15", Pos(1, 54)),
            ExpectedToken(TokenType.SPECIAL, ",", Pos(1, 55)),

            ExpectedToken(TokenType.STRING, "someString", Pos(1, 57)),
            ExpectedToken(TokenType.OPERATION, "*", Pos(1, 70)),
            ExpectedToken(TokenType.INT, "100", Pos(1, 72)),

            ExpectedToken(TokenType.SPECIAL, ")", Pos(1, 75)),
            ExpectedToken(TokenType.SPECIAL, "}", Pos(1, 76)),
            ExpectedToken(TokenType.EOF, "", Pos(1, 77)),
        )
        val parser = Parser(input.iterator())
        val actualTree = parser.parse()
        print(System.out, actualTree)
        val expectedTree = ProgramNode(
            listOf(),
            listOf(
                ExprCondNode(
                    null,
                    listOf(
                        FuncCallNode(
                            IdentifierNode("print", Pos(1, 2)),
                            listOf(
                                FixedPointNode(6.1, Pos(1, 8)),
                                FixedPointNode(4.0, Pos(1, 19)),
                                FixedPointNode(25.0, Pos(1, 33)),
                                IntNode(15, Pos(1, 46)),
                                IntNode(0, Pos(1, 57))
                            ),
                            Pos(1, 2)
                        )
                    ),
                    Pos(1, 1)
                )
            ),
            listOf(),
            Pos(1, 1)
        )
        assertEquals(expectedTree, actualTree)
    }

    companion object {
        data class ExpectedToken(override val type: TokenType, override val repr: String, override val pos: Pos) : Token
    }
}
