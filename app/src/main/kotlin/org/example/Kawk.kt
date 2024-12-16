package org.example

import org.example.lexer.TextLexer
import org.example.parser.*
import org.example.runtime.Runtime
import java.io.File
import java.io.FileReader
import java.io.InputStreamReader

fun main(args: Array<String>) {
    val input = if (args.size == 1) {
        InputStreamReader(System.`in`)
    } else if (args.size == 2) {
        FileReader(args[1])
    } else {
        throw IllegalArgumentException("Wrong arguments")
    }

    val script = if (args[0].endsWith(".awk")) {
        File(args[0]).readText()
    } else {
        args[0]
    }

    val program = Parser(TextLexer(script).iterator()).parse()
    Runtime(program, input).use {
        while (it.hasNext()) {
            it.next()
        }
    }
}
