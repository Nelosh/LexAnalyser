package app.lex;

class RuleBuilder {
    var rule: Rule = Rule()
    var ids: Collection<String> = emptyList()

    companion object Factory {
        fun using(ids: Collection<String>): RuleBuilder = RuleBuilder().using(ids)
    }

    fun using(ids: Collection<String>): RuleBuilder {
        this.ids = ids
        return this
    }

    fun hasWord(word: String): RuleBuilder {
        rule.add(checkWord(word))
        return this
    }

    fun hasRule(rule: Rule): RuleBuilder {
        this.rule.add(checkRule(rule))
        return this
    }

    fun hasID(): RuleBuilder {
        rule.add(checkID())
        return this
    }

    fun hasRuleChain(rule1: Rule): RuleBuilder {
        rule.add(checkRuleChain(rule1))
        return this
    }

    fun hasEitherRule(rule1: Rule, rule2: Rule): RuleBuilder {
        rule.add(checkEitherRule(rule1, rule2))
        return this
    }

    fun build() = rule

    private fun checkWord(expected: String) = {
        sentence: List<String> -> run {
            val head = sentence.first()
            checkLexeme(head, expected)
            sentence.drop(1)
        }
    }

    private fun checkRuleChain(rule: Rule) = {
        sentence: List<String> -> run {
            var sentencePart = sentence
            while (rule.canStartWith(sentencePart.first())) {
                sentencePart = rule.check(sentencePart)
            }
            sentencePart
        }
    }

    private fun checkRule(rule: Rule) = {
        sentence: List<String> -> run {
            rule.check(sentence)
        }
    }

    private fun checkEitherRule(rule1: Rule, rule2: Rule) = {
        sentence: List<String> ->
            try {
                rule1.check(sentence)
            } catch (e: UnexpectedLexemeException){
                rule2.check(sentence)
            }
    }

    private fun checkID() = {
        sentence: List<String> -> run {
            val head = sentence.first()
            isID(head)
            sentence.drop(1)
        }
    }

    private fun checkLexeme(toCheck: String, expected: String) {
        if (toCheck != expected) throw UnexpectedLexemeException(toCheck, expected)
    }

    private fun isID(lexeme: String) {
        if (!ids.contains(lexeme)) throw UnrecognizedLexemeException(lexeme)
    }

    class Rule {
        var rules: List<(List<String>) -> List<String>> = emptyList()
        fun check(sentence: List<String>): List<String> {
            var sentencePart = sentence
            for (rule in rules) {
                sentencePart = rule(sentencePart)
            }
            return sentencePart
        }

        fun add(rule: (List<String>) -> List<String>) {
            rules += (rule)
        }

        fun canStartWith(lexeme: String): Boolean {
            return try {
                rules.first()(listOf(lexeme))
                true
            } catch (e: UnexpectedLexemeException) {
                false
            } catch (e: UnrecognizedLexemeException) {
                false
            }
        }
    }

    class UnexpectedLexemeException(lexeme: String, expected: String) : Throwable("Unexpected lemexe: found $lexeme when $expected was expected")
    class UnrecognizedLexemeException(lexeme: String) : Throwable(lexeme + " is not an identifier")
}
