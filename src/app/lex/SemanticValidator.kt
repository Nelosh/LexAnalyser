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