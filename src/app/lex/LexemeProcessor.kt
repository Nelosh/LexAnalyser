package app.lex

import app.lex.LexemeProcessor.State.*

class LexemeProcessor {


    var delimiters: Set<Char> = emptySet()

    var pushResult: (Result) -> Unit = {}


    fun process(char: Char) {
        when(state){
            START -> start(char)
            INT -> processInt(char)
            ID -> processId(char)
            CONST -> processConst(char)
            DELIM2_ASSIGN -> processDelim2(char, '=')
            DELIM2_NE -> processDelim2(char, '>')
        }
    }

    private var lexeme: String = ""

    private var state: State = START

    private fun isDelimiter(char: Char): Boolean = delimiters.contains(char)

    private fun start(char: Char) {
        lexeme = "" + char
        state = when {
            char.isDigit() -> INT
            char.isLetter() -> ID
            char == '\'' -> CONST
            char == ':' -> DELIM2_ASSIGN
            char == '<' -> DELIM2_NE
            isDelimiter(char) -> return putResult("DELIM")
            char.isWhitespace() -> return
            else -> throw IllegalLexemeCharacter(char)

        }
    }

    private fun processInt(char: Char) {
        when {
            char.isDigit() -> lexeme += char
            else -> {putResult("LITERAL"); process(char) }
        }
    }

    private fun processId(char: Char) {
        when {
            char.isLetter() -> lexeme += char
            else -> {putResult("ID"); process(char) }

        }
    }

    private fun processConst(char: Char) {
        lexeme += char
        if (char == '\'')
            putResult("LITERAL")
    }

    fun processDelim2(char: Char, expected: Char) {
        when (char) {
            expected -> {lexeme += char; putResult("KEYWORD")}
            else -> {putResult("DELIM"); process(char)}
        }
    }

    fun putResult(type: String) {
        pushResult(Result(lexeme, type))
        state = START
    }

    class IllegalLexemeCharacter(char: Char) : Throwable("Unexpected character: " + char)
    data class Result(val lexeme: String, val type: String)
    enum class State {
        START, INT, ID, CONST, DELIM2_ASSIGN, DELIM2_NE
    }

}