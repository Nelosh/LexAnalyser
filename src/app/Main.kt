package app

import app.lex.LexemeProcessor
import app.lex.LexicalAnalyser
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
        printToFile(analyser.keyWords, out)

        out.println("\nLiterals: ")
        printToFile(analyser.literals, out)

        out.println("\nIdentifiers: ")
        printToFile(analyser.identifiers, out)

        out.println("\nResult Table: ")
        printToFile(analyser.results, out)
    }
}

fun configLine(config: List<String>, index: Int): List<String> = config[index].split(" ")

fun printToFile(set: Collection<Any>, out: PrintWriter) {
    for (value in set) {
        out.println("       " + value.toString())
    }
}
