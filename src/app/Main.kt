package app

import app.lex.*
import java.io.File
import java.io.PrintWriter

fun main(args: Array<String>) {
    val output = File("output.txt")
    var exceptionMessage: String = ""

    val programFile = File("program.txt")
    if (!programFile.exists()) {
        exceptionMessage = "Program file does not exist, should be program.txt"
        return output.writeText(exceptionMessage)
    }

    val program = programFile.readText()

    val configFile = File("config.txt")
    if (!configFile.exists()) {
        exceptionMessage = "Config file does not exist, should be config.txt"
        return output.writeText(exceptionMessage)
    }

    val config = configFile.readLines()
    if (config.size < 2) {
        exceptionMessage = "Config file does contain all necessary information, " +
                "should be two line with values separated by single space " +
                "\n First should contain single character delimiters" +
                "\n Second should contain keywords"
        return output.writeText(exceptionMessage)
    }

    val delimiters = configLine(config, 0).map { elem -> elem[0] }.toSet()
    val keyWords = configLine(config, 1).toSet().plus(configLine(config, 0))

    val analyser = LexicalAnalyser()
    analyser.loadKeyWords(keyWords)
    analyser.loadDelimiters(delimiters)

    try {
        analyser.analyse(program)
    } catch (e: LexemeProcessor.IllegalLexemeCharacter) {
        if (e.message != null) exceptionMessage = e.message
    } catch (e: LexicalAnalyser.InvalidLexemeTypeException) {
        if (e.message != null) exceptionMessage = e.message
    }


    output.printWriter().use { out ->
        if (exceptionMessage.isNotEmpty()) {
            out.println(exceptionMessage + "\n")
        }
        out.println("Key Words: ")
        println("Key Words: ")
        printToFile(analyser.keyWords, out)

        out.println("\nLiterals: ")
        println("\nLiterals: ")
        printToFile(analyser.literals, out)

        out.println("\nIdentifiers: ")
        println("\nIdentifiers: ")
        printToFile(analyser.identifiers, out)

        out.println("\nResult Table: ")
        println("\nResult Table: ")
        printToFile(analyser.results, out)
    }


    val statementFile = File("statement.txt")
    if (!statementFile.exists()) {
        exceptionMessage = "Statement file does not exist, should be statement.txt"
        return output.appendText(exceptionMessage)
    }

    val text: String = statementFile.readText()

    analyser.clean()

    try {
        analyser.analyse(text)
    } catch (e: LexemeProcessor.IllegalLexemeCharacter) {
        if (e.message != null) exceptionMessage = e.message
    } catch (e: LexicalAnalyser.InvalidLexemeTypeException) {
        if (e.message != null) exceptionMessage = e.message
    }

    val semanticValidator = OldSemanticValidator(analyser.identifiers)
    try {
        val list = analyser.results.map { x -> x.lexeme }
        semanticValidator.validate(list)
        println("\nStatement is correct")
        output.appendText("\nStatement is correct")
    } catch (e: RuleBuilder.UnexpectedLexemeException) {
        if (e.message != null) exceptionMessage = e.message
    } catch (e: RuleBuilder.UnrecognizedLexemeException) {
        if (e.message != null) exceptionMessage = e.message
    }

    if (exceptionMessage.isNotEmpty()) {
        output.appendText(exceptionMessage)
    }

    for (value in TetradCollector.tetrads) {
        println(value)
    }

}

fun configLine(config: List<String>, index: Int): List<String> = config[index].split(" ")

fun printToFile(set: Collection<Any>, out: PrintWriter) {
    for (value in set) {
        println("       " + value.toString())
        out.println("       " + value.toString())
    }
}
