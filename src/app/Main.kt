package app

import app.lex.*
import java.io.File
import java.io.PrintWriter

fun main(args: Array<String>) {
    val output1 = File("program-output.txt")
    var exceptionMessage: String = ""

    val programFile = File("program.txt")
    if (!programFile.exists()) {
        exceptionMessage = "Program file does not exist, should be program.txt"
        return output1.writeText(exceptionMessage)
    }

    val program = programFile.readText()

    val delimiters = setOf('(', ')', ':', ';', '.', '+', '*', '-', '/', '=', '\'', ',')
    val keyWords = setOf("procedure", "begin", "case", "of", "end", "if", "or", "then", "else", "boolean", "<>", ":=")

    val analyser = LexicalAnalyser()
    analyser.loadKeyWords(keyWords)
    analyser.loadDelimiters(delimiters)

    try {
        analyser.analyse(program)
    } catch (e: LexemeProcessor.IllegalLexemeCharacter) {
        if (e.message != null) exceptionMessage = e.message
    } catch (e: LexicalAnalyser.InvalidLexemeTypeException) {
        if (e.message != null) exceptionMessage = e.message
    } catch (e: LexicalAnalyser.AnalisationException) {
        if (e.message != null) exceptionMessage = e.message
    }


    output1.printWriter().use { out ->
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

    exceptionMessage = ""
    val output2 = File("statement-output.txt")
    val statementFile = File("statement.txt")
    if (!statementFile.exists()) {
        exceptionMessage = "Statement file does not exist, should be statement.txt"
        return output2.writeText(exceptionMessage)
    }

    val text: String = statementFile.readText()

    analyser.clean()

    try {
        analyser.analyse(text)
    } catch (e: LexemeProcessor.IllegalLexemeCharacter) {
        if (e.message != null) exceptionMessage = e.message
    } catch (e: LexicalAnalyser.InvalidLexemeTypeException) {
        if (e.message != null) exceptionMessage = e.message
    } catch (e: LexicalAnalyser.AnalisationException) {
        if (e.message != null) exceptionMessage = e.message
    }

    output2.printWriter().use { out ->
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
//    }

        if (exceptionMessage.isNotEmpty()) {
            println(exceptionMessage)
            return out.println(exceptionMessage)
        }
        val semanticValidator = SyntaxValidator(analyser.identifiers)
        try {
            val list = analyser.results.map { x -> x.lexeme }
            semanticValidator.validate(list)
        } catch (e: SyntaxValidator.UnexpectedLexemeException) {
            if (e.message != null) exceptionMessage = e.message
        } catch (e: SyntaxValidator.UnrecognizedLexemeException) {
            if (e.message != null) exceptionMessage = e.message
        }

//    output2.printWriter().use { out ->
        if (exceptionMessage.isNotEmpty()) {
            println(exceptionMessage)
            return out.println(exceptionMessage)
        }

        println("Statement is correct\n")
        println("Tetrads: ")
        out.println("\nStatement is correct")
        out.println("Tetrads: ")
        printToFile(TetradCollector.tetrads, out)
    }



}

fun printToFile(set: Collection<Any>, out: PrintWriter) {
    for ((i, value) in set.withIndex()) {
        val formatted = String.format("%3d: " + value.toString(), i)
        println(formatted)
        out.println(formatted)
    }
}
