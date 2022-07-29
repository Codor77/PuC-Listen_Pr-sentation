package interpreterStuff

import MonoType

sealed class Expr {
    data class Var(val name: String) : Expr()
    data class Lambda(val binder: String, val tyBinder: MonoType?, val body: Expr) : Expr()
    data class App(val func: Expr, val arg: Expr) : Expr()

    data class If(val condition: Expr, val thenBranch: Expr, val elseBranch: Expr) : Expr()

    data class Binary(val left: Expr, val op: Operator, val right: Expr) : Expr()
    data class Let(val recursive: Boolean, val binder: String, val expr: Expr, val body: Expr) : Expr()

    data class IntLiteral(val num: Int) : Expr()
    data class BoolLiteral(val bool: Boolean) : Expr()
    data class StringLiteral(val string: String) : Expr()
    data class ListLiteral(val contentType: Class<Expr>, val list: MutableList<Expr>) : Expr()

    data class SingleAttributeListOperation(val operation: parserStuff.ListOperation, val first: Expr) : Expr()
    data class DoubleAttributeListOperation(val operation: parserStuff.ListOperation, val first: Expr, val second: Expr) : Expr()
    data class TrippleAttributeListOperation(val operation: parserStuff.ListOperation, val first: Expr, val second: Expr, val third: Expr) : Expr()

}
