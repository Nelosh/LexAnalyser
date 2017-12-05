package app.lex

class LexicalAnalyser {

    var literals: Set<String> = emptySet()
    var identifiers: Set<String> = emptySet()
    var keyWords: Set<String> = emptySet()
    var results: List<LexicalEntry> = emptyList()

    private var lexemeProcessor: LexemeProcessor = LexemeProcessor()

    init {
        lexemeProcessor.pushResult = this::processResult
    }


    fun analyse(input: String) {
        for (char in input) {
            lexemeProcessor.process(char)
        }
        lexemeProcessor.process(' ')
    }

    fun loadKeyWords(keyWords: Set<String>){
        this.keyWords = keyWords
    }

    fun loadDelimiters(delimiters: Set<Char>){
        lexemeProcessor.delimiters = delimiters
    }

    fun clean() {
        literals = emptySet()
        identifiers = emptySet()
        results = emptyList()
    }

    private fun processResult(result: LexemeProcessor.Result) {
        var pointer = 0
        var (lexeme, type) = result
        if (type == "ID") {
            type = getIdType(lexeme)
        }

        val wordSet = when(type) {
            "ID" -> {identifiers += lexeme; identifiers}
            "KEYWORD" -> keyWords
            "DELIM" -> keyWords
            "LITERAL" -> {literals += lexeme; literals}
            else -> throw InvalidLexemeTypeException(type)
        }
        pointer = wordSet.indexOf(lexeme)

        results += LexicalEntry(lexeme, type, pointer)
    }

    private fun isKeyWord(lexeme: String): Boolean = keyWords.contains(lexeme)
    private fun getIdType(lexeme: String): String = if (isKeyWord(lexeme)) "KEYWORD" else "ID"

    data class LexicalEntry(val lexeme: String, val type: String, val pointer: Int) {
        override fun toString(): String = String.format("%10s | %7s | %3d", lexeme, type, pointer)
    }
    class InvalidLexemeTypeException(type: String) : Throwable("Unrecognized lexeme type: " + type)
}