package app.lex

object TetradCollector {

    var tetrads: List<Tetrad> = emptyList()
    var variableCount: Int = 1
    var variable: String? = null

    fun nextVariable(): String {
        if (variable != null) {
            val result = variable
            variable = null
            return result!!
        }
        return "T" + ++variableCount
    }

    fun makeOperation(operator: String, operand1: String, operand2: String, destination: String) {
        tetrads += Tetrad(operator, operand1, operand2, destination)
    }

    data class Tetrad(val operator: String, val operand1: String, val operand2: String, val destination: String) {
        override fun toString(): String = "[$operator, $operand1, $operand2, $destination]"
    }

}