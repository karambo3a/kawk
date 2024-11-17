package org.example

import org.example.lexer.TextLexer
import org.example.parser.*

fun main(args: Array<String>) {
    print(System.out, Parser(TextLexer(readLine()!!).iterator()).parse())
}
