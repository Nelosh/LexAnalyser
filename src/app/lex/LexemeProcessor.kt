package app.lex

import app.lex.LexemeProcessor.State.*

class LexemeProcessor {

    var lexeme: String = ""

    var delimiters: Set<Char> = emptySet()

    var pushResult: (Result) -> Unit = {}

    private var state: State = START

    fun isDelimiter(char: Char): Boolean = delimiters.contains(char)

    fun process(char: Char) {
        when(state){
            START -> start(char)
            INT -> processInt(char)
            ID -> processId(char)
            CONST -> procesConst(char)
            DELIM2_ASSIGN -> processDelim2(char, '=')
            DELIM2_NE -> processDelim2(char, '>')
        }
    }

    fun start(char: Char) {
        lexeme = "" + char
        state = when {
            char.isDigit() -> INT
            char.isLetter() -> ID
            char == '\'' -> CONST
            char == ':' -> DELIM2_ASSIGN
            char == '<' -> DELIM2_NE
            isDelimiter(char) -> return putResult("KEYWORD")
            char.isWhitespace() -> return
            else -> throw IllegalLexemeCharacter(char)

        }
    }

    fun processInt(char: Char) {
        when {
            char.isDigit() -> lexeme += char
            else -> {putResult("LITERAL"); process(char) }
        }
    }

    fun processId(char: Char) {
        when {
            char.isLetter() -> lexeme += char
            else -> {putResult("ID"); process(char) }

        }
    }

    fun procesConst(char: Char) {
        lexeme += char
        if (char == '\'')
            putResult("LITERAL")
    }

    fun processDelim2(char: Char, expected: Char) {
        when (char) {
            expected -> {lexeme += char; putResult("KEYWORD")}
            else -> {putResult("KEYWORD"); process(char)}
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