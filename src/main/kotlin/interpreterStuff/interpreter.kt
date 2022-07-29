import com.sun.jdi.InvalidTypeException
import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.persistentHashMapOf
import interpreterStuff.Expr
import parserStuff.Lexer
import interpreterStuff.Operator
import parserStuff.ListOperation

typealias Env = PersistentMap<String, Value>

sealed class Value {
    data class Int(val num: kotlin.Int) : Value()
    data class Bool(val bool: Boolean) : Value()
    data class String(val string: kotlin.String) : Value()
    data class List(val contentType: Class<Value>, val list: MutableList<Value>) : Value()
    data class Closure(var env: Env, val binder: kotlin.String, val body: Expr) : Value()
}

fun eval(env: Env, expr: Expr): Value {
    return when (expr) {
        is Expr.IntLiteral -> Value.Int(expr.num)
        is Expr.BoolLiteral -> Value.Bool(expr.bool)
        is Expr.StringLiteral -> Value.String(expr.string)
        is Expr.ListLiteral -> {
            val valueList: MutableList<Value> = mutableListOf()
            for (expr in expr.list) {
                when (expr) {
                    is Expr.IntLiteral -> valueList.add(Value.Int(expr.num))
                    is Expr.BoolLiteral -> valueList.add(Value.Bool(expr.bool))
                    is Expr.StringLiteral -> valueList.add(Value.String(expr.string))
                    is Expr.ListLiteral -> valueList.add(eval(env, expr))
                    else -> throw Exception("Invalid data-type: '${expr.javaClass}'")
                }
            }
            Value.List(valueList[0].javaClass, valueList)
        }
        is Expr.SingleAttributeListOperation -> {
            return when (expr.operation) {
                is ListOperation.LIST_GET_SIZE -> {
                    val first = eval(env, expr.first)
                    if (first !is Value.List) throw InvalidTypeException()
                    return Value.Int(first.list.size)
                }
                is ListOperation.LIST_IS_EMPTY -> {
                    val first = eval(env, expr.first)
                    if (first !is Value.List) throw InvalidTypeException()
                    return Value.Bool(first.list.isEmpty())
                }
                else -> throw Exception("unreachable")
            }

        }
        is Expr.DoubleAttributeListOperation -> {
            return when (expr.operation) {
                is ListOperation.LIST_JOIN -> {
                    val first = eval(env, expr.first)
                    val second = eval(env, expr.second)
                    if (first !is Value.List || second !is Value.List) throw InvalidTypeException()
                    first.list.addAll(second.list)
                    return Value.List(first.contentType, first.list)
                }
                is ListOperation.LIST_APPEND -> {
                    val first = eval(env, expr.first)
                    val second = eval(env, expr.second)
                    if (first !is Value.List) throw InvalidTypeException()
                    if (second.javaClass != first.contentType) throw InvalidTypeException("'${second.javaClass}' does not match '${first.contentType}'")
                    first.list.add(second)
                    return Value.List(first.contentType, first.list)
                }
                is ListOperation.LIST_REMOVE_AT_POSITION -> {
                    val first = eval(env, expr.first)
                    val second = eval(env, expr.second)
                    if (first !is Value.List || second !is Value.Int) throw InvalidTypeException()
                    first.list.removeAt(second.num)
                    return Value.List(first.contentType, first.list)
                }
                is ListOperation.LIST_GET_VALUE -> {
                    val first = eval(env, expr.first)
                    val second = eval(env, expr.second)
                    if (first !is Value.List || second !is Value.Int) throw InvalidTypeException()
                    return first.list.get(second.num)
                }
                is ListOperation.LIST_MAP -> {
                    val first = eval(env, expr.first)
                    val second = eval(env, expr.second)

                    if (first !is Value.List || second !is Value.Closure) throw InvalidTypeException()
                    var returnListType : Class<Value>? = null
                    val returnList = first.list.map {
                        var tempEnv = env
                        tempEnv = tempEnv.put(second.binder, it)
                        val newValue = eval(tempEnv, second.body)
                        if (returnListType == null) {
                            returnListType = newValue.javaClass
                        }
                        if (newValue.javaClass != returnListType) throw InvalidTypeException("Return Type of given function (${newValue.javaClass.simpleName}) does not match ListContentType (${it.javaClass.simpleName})")
                        newValue
                    }
                    return Value.List(returnList.first().javaClass, returnList.toMutableList())
                }
                is ListOperation.LIST_FILTER -> {
                    val first = eval(env, expr.first)
                    val second = eval(env, expr.second)

                    if (first !is Value.List || second !is Value.Closure) throw InvalidTypeException()

                    first.list.removeAll {
                        var tempEnv = env
                        tempEnv = tempEnv.put(second.binder, it)
                        val newValue = eval(tempEnv, second.body)
                        if (newValue !is Value.Bool) throw InvalidTypeException("Return Type of given function (${newValue.javaClass.simpleName}) does not match Bool")
                        !newValue.bool
                    }
                    return first
                }
                is ListOperation.LIST_FOLD -> {
                    val first = eval(env, expr.first)
                    val second = eval(env, expr.second)

                    if (first !is Value.List || second !is Value.Closure) throw InvalidTypeException()
                    if (second.body !is Expr.Lambda) throw InvalidTypeException()
                    if (first.list.size < 2) throw IndexOutOfBoundsException("the given list has to contain at least 2 elements")

                    var returnValue : Value = first.list.first()
                    first.list.removeFirst()

                    first.list.forEach {
                        var tempEnv = env
                        tempEnv = tempEnv.put(second.binder, returnValue)
                        tempEnv = tempEnv.put(second.body.binder, it)
                        returnValue = eval(tempEnv, second.body.body)
                    }
                    return returnValue
                }
                else -> throw Exception("unreachable")
            }
        }
        is Expr.TrippleAttributeListOperation -> {
            return when (expr.operation) {
                is ListOperation.LIST_INSERT_AT -> {
                    val first = eval(env, expr.first)
                    val second = eval(env, expr.second)
                    val third = eval(env, expr.third)
                    if (first !is Value.List) throw InvalidTypeException()
                    if (second.javaClass != first.contentType) throw InvalidTypeException("'${second.javaClass}' does not match '${first.contentType}'")
                    if (third !is Value.Int) throw InvalidTypeException()
                    first.list.add(third.num, second)
                    return Value.List(first.contentType, first.list)
                }
                else -> throw Exception("unreachable")
            }
        }
        is Expr.Binary -> {
            val left = eval(env, expr.left)
            val right = eval(env, expr.right)
            return when (expr.op) {
                Operator.Equality -> if (left is Value.Int && right is Value.Int) {
                    Value.Bool(left.num == right.num)
                } else if (left is Value.Bool && right is Value.Bool) {
                    Value.Bool(left.bool == right.bool)
                } else if (left is Value.String && right is Value.String) {
                    Value.Bool(left.string == right.string)
                } else {
                    throw Error("Comparing incompatible values: $left and $right")
                }
                Operator.Concat -> if (left is Value.String && right is Value.String) {
                    Value.String(left.string + right.string)
                } else {
                    throw Error("Can't concatenate non-string values: $left and $right")

                }
                else -> numericBinary(left, right, nameForOp(expr.op)) { x, y -> applyOp(expr.op, x, y) }
            }

        }
        is Expr.If -> {
            val condition = eval(env, expr.condition)
            if (condition !is Value.Bool) {
                throw Exception("Expected a boolean condition, but got $condition")
            }
            return if (condition.bool) {
                eval(env, expr.thenBranch)
            } else {
                eval(env, expr.elseBranch)
            }
        }
        is Expr.Let -> {
            val evaledExpr = eval(env, expr.expr)
            if (expr.recursive && evaledExpr is Value.Closure) {
                evaledExpr.env = evaledExpr.env.put(expr.binder, evaledExpr)
            }
            val extendedEnv = env.put(expr.binder, evaledExpr)
            eval(extendedEnv, expr.body)
        }
        is Expr.Lambda -> Value.Closure(env, expr.binder, expr.body)
        is Expr.Var ->
            when (expr.name) {
                "#firstChar" -> {
                    val s = env["x"]!! as Value.String
                    Value.String(s.string.take(1))
                }
                "#remainingChars" -> {
                    val s = env["x"]!! as Value.String
                    Value.String(s.string.drop(1))
                }
                "#charCode" -> {
                    val s = env["x"]!! as Value.String
                    Value.Int(s.string[0].code)
                }
                "#codeChar" -> {
                    val x = env["x"]!! as Value.Int
                    Value.String(x.num.toChar().toString())
                }
                else -> env.get(expr.name) ?: throw Exception("Unbound variable ${expr.name}")
            }
        is Expr.App -> {
            val func = eval(env, expr.func)
            if (func !is Value.Closure) {
                throw Exception("$func is not a function")
            } else {
                val arg = eval(env, expr.arg)
                val newEnv = func.env.put(func.binder, arg)
                eval(newEnv, func.body)
            }
        }
    }
}

fun applyOp(op: Operator, x: Int, y: Int): Value {
    return when (op) {
        Operator.Add -> Value.Int(x + y)
        Operator.Subtract -> Value.Int(x - y)
        Operator.Multiply -> Value.Int(x * y)
        Operator.Divide -> Value.Int(x / y)
        Operator.Equality -> Value.Bool(x == y)
        Operator.Modulo -> Value.Int (x.mod(y))
        else -> throw Error("operator not suitable for ints")
    }
}

fun nameForOp(op: Operator): String {
    return when (op) {
        Operator.Add -> "add"
        Operator.Subtract -> "subtract"
        Operator.Multiply -> "multiply"
        Operator.Divide -> "divide"
        Operator.Equality -> "compare"
        Operator.Concat -> "concat"
        Operator.Modulo -> "modulo"
    }
}

fun numericBinary(left: Value, right: Value, operation: String, combine: (Int, Int) -> Value): Value {
    if (left is Value.Int && right is Value.Int) {
        return combine(left.num, right.num)
    } else {
        throw (Exception("Can't $operation non-numbers, $left, $right"))
    }
}

val emptyEnv: Env = persistentHashMapOf()
val initialEnv: Env = persistentHashMapOf(
    "firstChar" to Value.Closure(
        emptyEnv, "x",
        Expr.Var("#firstChar")
    ),
    "remainingChars" to Value.Closure(
        emptyEnv, "x",
        Expr.Var("#remainingChars")
    ),
    "charCode" to Value.Closure(
        emptyEnv, "x",
        Expr.Var("#charCode")
    ),
    "codeChar" to Value.Closure(
        emptyEnv, "x",
        Expr.Var("#codeChar")
    )
)

fun testInput(input: String) {
    val expr = Parser(Lexer(input)).parseExpression()
//  val ty = infer(initialContext, expr)

    println("${eval(initialEnv, expr)}") // : ${prettyPoly(generalize(initialContext, applySolution(ty)))}")
}
