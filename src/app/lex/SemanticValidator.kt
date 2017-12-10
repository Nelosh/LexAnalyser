package app.lex

class SemanticValidator(val knownIDs: Set<String>) {

    fun validate(sentence: List<String>): Boolean = ifStatementRule.check(sentence).isEmpty()

    private val idRule: RuleBuilder.Rule = rule().hasID().build()
    private val parameterTailRule = rule().hasWord(",").hasID().build()
    private val parametersRule = rule().hasID().hasRuleChain(parameterTailRule).build()
    private val functionCallRule = rule().hasID().hasWord("(").hasRule(parametersRule).hasWord(")").build()
    private val valueRule = rule().hasEitherRule(functionCallRule, idRule).build()
    private val statementRule = rule().hasID().hasWord(":=").hasRule(valueRule).build()
    private val neqConditionRule = rule().hasRule(valueRule).hasWord("<>").hasRule(valueRule).build()
    private val logicalTermRule = rule().hasWord("(").hasRule(neqConditionRule).hasWord(")").build()
    private val logicalTailRule = rule().hasWord("or").hasRule(logicalTermRule).build()
    private val logicalConditionRule = rule().hasRule(logicalTermRule).hasRuleChain(logicalTailRule).build()
    private val ifStatementRule = rule().hasWord("if").hasRule(logicalConditionRule).hasWord("then").hasRule(statementRule).hasWord(";").build()

    private fun rule(): RuleBuilder = RuleBuilder.using(knownIDs)
}

@Deprecated("Cause it's ugly, but it works for lab 3")
class OldSemanticValidator(val knownIDs: Set<String>) {

    fun validate(sentence: List<String>): Boolean = ifStatement(sentence).isEmpty()

    fun ifStatement(sentence: List<String>): List<String> {
        var tail = checkNext("if", sentence)
        tail = logicalCondition(tail)
        TetradCollector.makeOperation("BRIFNOT", TetradCollector.nextVariable(), "", "")
        tail = checkNext("then", tail)
        tail = statement(tail)
        return checkNext(";", tail)
    }

    fun logicalCondition(sentence: List<String>): List<String> {
        var tail = sentence
        tail = checkNext("(", tail)
        tail = neqCondition(tail)
        var varName = TetradCollector.nextVariable()
        var resvar = ""
        tail = checkNext(")", tail)
        while (tail.first() == "or") {
            tail = tail.drop(1)
            tail = checkNext("(", tail)
            tail = neqCondition(tail)
            val varName2 = TetradCollector.nextVariable()
            resvar = TetradCollector.nextVariable()
            TetradCollector.makeOperation("OR", varName, varName2, resvar)
            varName = resvar
            tail = checkNext(")", tail)
        }
        TetradCollector.variable = resvar
        return tail
    }

    fun neqCondition(sentence: List<String>): List<String> {
        var tail = value(sentence)
        val varName = TetradCollector.nextVariable()
        tail = checkNext("<>", tail)
        val result = value(tail)
        var resvar = ""
        TetradCollector.makeOperation("EQUALS", varName, TetradCollector.nextVariable(), {resvar = TetradCollector.nextVariable(); resvar}())
        TetradCollector.makeOperation("NOT", resvar, "", {resvar = TetradCollector.nextVariable(); resvar}())
        TetradCollector.variable = resvar
        return result
    }

    fun statement(sentence: List<String>): List<String> {
        var tail = checkNextIsID(sentence)
        val varName = TetradCollector.nextVariable()
        tail = checkNext(":=", tail)
        val result = value(tail)
        TetradCollector.makeOperation("ASSIGN", TetradCollector.nextVariable(), "", varName)
        return result
    }

    fun value(sentence: List<String>): List<String> {
        try {
            return functionCall(sentence)
        } catch (e: UnexpectedLexemException) {
            return checkNextIsID(sentence)
        }
    }

    fun functionCall(sentence: List<String>): List<String> {
        var tail = checkNextIsID(sentence)
        val varName = TetradCollector.nextVariable()
        tail = checkNext("(", tail)
        tail = parameters(tail)
        val secvar = TetradCollector.nextVariable()
        val resvar = TetradCollector.nextVariable()
        TetradCollector.makeOperation("CALL", varName, secvar, resvar)
        TetradCollector.variable = resvar
        return checkNext(")", tail)
    }

    fun parameters(sentence: List<String>): List<String> {
        var tail = sentence
        val varName = TetradCollector.nextVariable()
        do {
            tail = checkNextIsID(tail)
            TetradCollector.makeOperation("SET", TetradCollector.nextVariable(), "", varName)
        } while (tail.first() == ",")
        TetradCollector.variable = varName
        return tail
    }

    fun checkNextIsID(sentence: List<String>): List<String> {
        val head = sentence.first()
        isID(head)
        TetradCollector.variable = head
        return sentence.drop(1)
    }

    fun isID(lexeme: String) {
        if (!knownIDs.contains(lexeme)) throw UnrecognizedLexemException(lexeme)
    }

    fun checkNext(expected: String, sentence: List<String>): List<String> {
        val head = sentence.first()
        checkLexeme(head, expected)
        return sentence.drop(1)
    }

    fun checkLexeme(toCheck: String, expected: String) {
        if (toCheck != expected) throw UnexpectedLexemException(toCheck, expected)
    }

    class UnexpectedLexemException(lexeme: String, expected: String) : Throwable("Unexpected lemexe: " + lexeme + " found when " + expected + " was expected")
    class UnrecognizedLexemException(lexeme: String) : Throwable(lexeme + " is not an identifier")
}