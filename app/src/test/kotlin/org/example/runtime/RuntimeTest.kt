package org.example.runtime

import org.example.lexer.TextLexer
import org.example.parser.Parser
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileReader
import java.io.PrintStream
import java.io.Reader
import java.io.StringReader

class RuntimeTest {

    private fun setNewOutput(): ByteArrayOutputStream {
        val outputStream = ByteArrayOutputStream()
        val newOut = PrintStream(outputStream)
        System.setOut(newOut)
        return outputStream
    }

    @Test
    fun scriptFromInInputFromFileTest() {
        for (i in 0..N - 1) {
            val testFile = SOURCES_FILE[i]
            val script = SCRIPTS_IN[i]
            val program = Parser(TextLexer(script).iterator()).parse()
            val input = FileReader(File(testFile).absolutePath)
            val outputStream = setNewOutput()
            Runtime(program, input as Reader).use {
                while (it.hasNext()) {
                    it.next()
                }
            }
            val expected = RESULTS[i]
            val actual = outputStream.toString()
            assertEquals(expected, actual)
            println(i)
        }
    }

    @Test
    fun scriptFromFileInputFromFileTest() {
        for (i in 0..N - 1) {
            val testFile = SOURCES_FILE[i]
            val script = File(SCRIPTS_FILE[i]).readText()
            val program = Parser(TextLexer(script).iterator()).parse()
            val input = FileReader(testFile)
            val outputStream = setNewOutput()

            Runtime(program, input).use {
                while (it.hasNext()) {
                    it.next()
                }
            }
            val expected = RESULTS[i]
            val actual = outputStream.toString()
            assertEquals(expected, actual)
            println(i)
        }
    }

    @Test
    fun scriptFromInInputFromInTest() {
        for (i in 0..N - 1) {
            val script = SCRIPTS_IN[i]
            val program = Parser(TextLexer(script).iterator()).parse()
            val input = StringReader(SOURCES_IN[i])
            val outputStream = setNewOutput()

            Runtime(program, input).use {
                while (it.hasNext()) {
                    it.next()
                }
            }
            val expected = RESULTS[i]
            val actual = outputStream.toString()
            assertEquals(expected, actual)
            println(i)
        }
    }

    @Test
    fun scriptFromFileInputFromInTest() {
        for (i in 0..N - 1) {
            val script = File(SCRIPTS_FILE[i]).readText()
            val program = Parser(TextLexer(script).iterator()).parse()
            val input = StringReader(SOURCES_IN[i])
            val outputStream = setNewOutput()

            Runtime(program, input).use {
                while (it.hasNext()) {
                    it.next()
                }
            }
            val expected = RESULTS[i]
            val actual = outputStream.toString()
            assertEquals(expected, actual)
            println(i)
        }
    }


    companion object {
        val SOURCES_FILE: List<String> = listOf(
            "/home/karambo3a/Desktop/study/kotlin/kawk-karambo3a/app/src/test/resources/Sample1",
            "/home/karambo3a/Desktop/study/kotlin/kawk-karambo3a/app/src/test/resources/Sample2",
            "/home/karambo3a/Desktop/study/kotlin/kawk-karambo3a/app/src/test/resources/Sample3",
            "/home/karambo3a/Desktop/study/kotlin/kawk-karambo3a/app/src/test/resources/Sample4",
            "/home/karambo3a/Desktop/study/kotlin/kawk-karambo3a/app/src/test/resources/Sample5",
            "/home/karambo3a/Desktop/study/kotlin/kawk-karambo3a/app/src/test/resources/Sample6",
            "/home/karambo3a/Desktop/study/kotlin/kawk-karambo3a/app/src/test/resources/Sample7"
        )
        val SOURCES_IN: List<String> = listOf(
            "hello,12,23\nbye,234,123\nvasya,1223,432\nhello,33,22",
            "hello,12,23,1,10\nbye,234,123,1,10\nvasya,1223,432,1,10\nhello,33,22,1,10",
            "first line \nsecond line \nlast line",
            "11 22 33 44 55",
            "11 22 33\n11 22 33 44 55 66 77 88 99",
            "line 1\nline 2\nline 3\nline 4",
            "line 1 2 3\n" +
                    "line 2 3 4\n" +
                    "line 3 4 5\n" +
                    "line 4 5 6\n" +
                    "line 5 6 7\n" +
                    "line 6 7 8\n" +
                    "line 7 8 9\n" +
                    "line 8 9 10\n" +
                    "line 9 10 11\n" +
                    "line 10 11 12"
        )
        val SCRIPTS_FILE: List<String> = listOf(
            "/home/karambo3a/Desktop/study/kotlin/kawk-karambo3a/app/src/test/resources/sample1.awk",
            "/home/karambo3a/Desktop/study/kotlin/kawk-karambo3a/app/src/test/resources/sample2.awk",
            "/home/karambo3a/Desktop/study/kotlin/kawk-karambo3a/app/src/test/resources/sample3.awk",
            "/home/karambo3a/Desktop/study/kotlin/kawk-karambo3a/app/src/test/resources/sample4.awk",
            "/home/karambo3a/Desktop/study/kotlin/kawk-karambo3a/app/src/test/resources/sample5.awk",
            "/home/karambo3a/Desktop/study/kotlin/kawk-karambo3a/app/src/test/resources/sample6.awk",
            "/home/karambo3a/Desktop/study/kotlin/kawk-karambo3a/app/src/test/resources/sample7.awk"
        )
        val SCRIPTS_IN: List<String> = listOf(
            "BEGIN {FS = \",\"} $1==\"hello\" {print (NR, $2 + $3, $2 * $3) }",
            "BEGIN {FS = \",\"} $1==\"hello\" { print ( NR, $2 + 3 + $3 + 3, $2 * $3 * 3 * 3, $4 - 1 - 2)}",
            "{print(\"1.1\" + 5, \"10q\" / 2.5, 2.5 * \"10\", \"30\" - 15, \"someString\" * 100)}",
            "{ print $0; NF = 2 ; print $0 ; NF = 3 ; print $0  }",
            "{ print $0; $9=\"hello\" ; print $0 ;  print NF   }",
            "{print $0; $0 = $2; print ($0, $1, NF)}",
            "BEGIN { NR = 10 } { print (NR, $0)}"
        )
        val RESULTS: List<String> = listOf(
            "1 35 276 \n4 55 726 \n",
            "1 41 2484 2 \n4 61 6534 2 \n",
            "6.1 4.0 25.0 15 0 \n6.1 4.0 25.0 15 0 \n6.1 4.0 25.0 15 0 \n",
            "11 22 33 44 55 \n11 22 \n11 22 \n",
            "11 22 33 \n11 22 33      hello " +
                    "\n9 " +
                    "\n11 22 33 44 55 66 77 88 99 \n" +
                    "11 22 33 44 55 66 77 88 hello \n9 \n",
            "line 1 \n1 1 1 \nline 2 \n2 2 1 \nline 3 \n3 3 1 \nline 4 \n4 4 1 \n",
            "11 line 1 2 3 \n" +
                    "12 line 2 3 4 \n" +
                    "13 line 3 4 5 \n" +
                    "14 line 4 5 6 \n" +
                    "15 line 5 6 7 \n" +
                    "16 line 6 7 8 \n" +
                    "17 line 7 8 9 \n" +
                    "18 line 8 9 10 \n" +
                    "19 line 9 10 11 \n" +
                    "20 line 10 11 12 \n"
        )
        const val N: Int = 7
    }
}