import com.sun.jdi.InvalidTypeException
import interpreterStuff.Expr
import interpreterStuff.Operator
import kotlinx.collections.immutable.persistentHashMapOf
import parserStuff.Token
import parserStuff.Lexer
import parserStuff.ListOperation
import java.lang.NullPointerException

class Parser(val lexer: Lexer) {

  fun parseType(): MonoType {
    var ty = parseTypeAtom()
    while(lexer.lookahead() == Token.ARROW) {
      expect<Token.ARROW>("an arrow")
      ty = MonoType.FunType(ty, parseType())
    }
    return ty
  }

  fun parseTypeAtom(): MonoType {
    return when (val t = lexer.next()) {
      is Token.BOOL -> {
        MonoType.BoolTy
      }
      is Token.INT -> {
        MonoType.IntTy
      }
      is Token.STRING -> {
        MonoType.StringTy
      }
      is Token.LPAREN -> {
        val ty = parseType()
        expect<Token.RPAREN>("a closing paren")
        ty
      }
      else -> throw Error("Expected a type but got: $t")
    }
  }

  fun parseExpression(): Expr {
    return parseBinary(0)
  }

  fun parseBinary(minBindingPower: Int): Expr {
    var lhs = parseApplication()
    while (true) {
      val op = peekOperator() ?: break
      val (leftBp, rightBp) = bindingPowerForOp(op)
      if (minBindingPower > leftBp) break;
      lexer.next()
      val rhs = parseBinary(rightBp)
      lhs = Expr.Binary(lhs, op, rhs)
    }
    return lhs
  }

  private fun peekOperator(): Operator? {
    return when (lexer.lookahead()) {
      Token.DIVIDES -> Operator.Divide
      Token.DOUBLE_EQUALS -> Operator.Equality
      Token.MINUS -> Operator.Subtract
      Token.MULTIPLY -> Operator.Multiply
      Token.PLUS -> Operator.Add
      Token.HASH -> Operator.Concat
      Token.MODULO -> Operator.Modulo
      else -> null
    }
  }

  private fun bindingPowerForOp(op: Operator): Pair<Int, Int> {
    return when (op) {
      Operator.Equality -> 2 to 1
      Operator.Add, Operator.Subtract, Operator.Concat-> 3 to 4
      Operator.Multiply, Operator.Divide, Operator.Modulo -> 5 to 6
    }
  }

  fun parseApplication(): Expr {
    var expr = parseAtom() ?: throw Exception("Expected an expression")
    while (true) {
      val arg = parseAtom() ?: break
      expr = Expr.App(expr, arg)
    }
    return expr
  }

  fun parseAtom(): Expr? {
    return when (lexer.lookahead()) {
      is Token.INT_LIT -> parseInt()
      is Token.BOOL_LIT -> parseBool()
      is Token.STRING_LIT -> parseString()
      is Token.LIST -> parseList()
      is Token.LIST_OPERATION -> parseListOperation()
      is Token.BACKSLASH -> parseLambda()
      is Token.LET -> parseLet()
      is Token.IF -> parseIf()
      is Token.IDENT -> parseVar()
      is Token.LPAREN -> {
        expect<Token.LPAREN>("opening paren")
        val inner = parseExpression()
        expect<Token.RPAREN>("closing paren")
        inner
      }
      else -> null
    }
  }

  private fun parseString(): Expr {
    val t = expect<Token.STRING_LIT>("string")
    return Expr.StringLiteral(t.string)
  }

  private fun parseList(): Expr{
    expect<Token.LIST>("list")
    expect<Token.SLPAREN>("slparen")

    when (lexer.lookahead()) {
      is Token.SRPAREN -> throw Exception("List empty")
      is Token.COMMA -> throw Exception("List malformed")
    }

    val firstListElement = parseAtom() ?: throw NullPointerException("Parser-fuck-up")
    val listType = firstListElement.javaClass
    val list = mutableListOf(firstListElement)

    while (lexer.lookahead() != Token.SRPAREN){
      expect<Token.COMMA>("comma")

      val nextLex = lexer.lookahead()
      if (nextLex == Token.COMMA || nextLex == Token.SRPAREN){
        throw Error("List malformed")
      }
      val newListElement = parseAtom() ?: throw NullPointerException("Parser-fuck-up")
      if (newListElement.javaClass != listType) throw InvalidTypeException("Every value of a List has to be the same Type")
      list.add(newListElement)
    }
    expect<Token.SRPAREN>("srparen")
    return Expr.ListLiteral(listType, list)
  }

  private fun parseListOperation(): Expr {
    val operator = expect<Token.LIST_OPERATION>("listOP")

    return when (operator.operation) {
      ListOperation.LIST_IS_EMPTY, ListOperation.LIST_GET_SIZE
        -> parseSingleAttributeListOperation(operator)

      ListOperation.LIST_APPEND, ListOperation.LIST_JOIN, ListOperation.LIST_GET_VALUE, ListOperation.LIST_REMOVE_AT_POSITION,
      ListOperation.LIST_MAP, ListOperation.LIST_FILTER, ListOperation.LIST_FOLD
        -> parseDoubleAttributeListOperation(operator)

      ListOperation.LIST_INSERT_AT
        -> parseTrippleAttributeListOperation(operator)
    }
  }

  private fun parseSingleAttributeListOperation(operator: Token.LIST_OPERATION):Expr {
    val first = parseAtom() ?: throw NullPointerException("Missing argument")

    return Expr.SingleAttributeListOperation(operator.operation, first)
  }

  private fun parseDoubleAttributeListOperation(operator: Token.LIST_OPERATION): Expr {
    val first = parseAtom() ?: throw NullPointerException("Missing argument")
    val second = parseAtom() ?: throw NullPointerException("Missing argument")

    return Expr.DoubleAttributeListOperation(operator.operation, first, second)
  }

  private fun parseTrippleAttributeListOperation(operator: Token.LIST_OPERATION): Expr {
    val first = parseAtom() ?: throw NullPointerException("Missing argument")
    val second = parseAtom() ?: throw NullPointerException("Missing argument")
    val third = parseAtom() ?: throw NullPointerException("Missing argument")

    return Expr.TrippleAttributeListOperation(operator.operation, first, second, third)
  }

  private fun parseLet(): Expr {
    expect<Token.LET>("let")
    val recursive = lexer.lookahead() == Token.REC
    if (recursive) {
      expect<Token.REC>("rec")
    }
    val binder = expect<Token.IDENT>("binder").ident
    expect<Token.EQUALS>("equals")
    val expr = parseExpression()
    expect<Token.IN>("in")
    val body = parseExpression()
    return Expr.Let(recursive, binder, expr, body)
  }

  private fun parseVar(): Expr.Var {
    val ident = expect<Token.IDENT>("identifier")
    return Expr.Var(ident.ident)
  }

  private fun parseIf(): Expr.If {
    expect<Token.IF>("if")
    val condition = parseExpression()
    expect<Token.THEN>("then")
    val thenBranch = parseExpression()
    expect<Token.ELSE>("else")
    val elseBranch = parseExpression()
    return Expr.If(condition, thenBranch, elseBranch)
  }

  private fun parseLambda(): Expr.Lambda {
    expect<Token.BACKSLASH>("lambda")
    val binder = expect<Token.IDENT>("binder")
    var tyBinder: MonoType? = null
    if (lexer.lookahead() == Token.COLON) {
      expect<Token.COLON>("colon")
      tyBinder = parseType()
    }
    expect<Token.EQ_ARROW>("arrow")
    val body = parseExpression()
    return Expr.Lambda(binder.ident, tyBinder, body)
  }

  private fun parseInt(): Expr.IntLiteral {
    val t = expect<Token.INT_LIT>("integer")
    return Expr.IntLiteral(t.int)
  }

  private fun parseBool(): Expr.BoolLiteral {
    val t = expect<Token.BOOL_LIT>("boolean")
    return Expr.BoolLiteral(t.bool)
  }

  private inline fun <reified T> expect(msg: String): T {
    val tkn = lexer.next()
    return tkn as? T ?: throw Exception("Expected $msg but saw $tkn")
  }
}

fun monoTy(input: String): MonoType {
  return Parser(Lexer(input)).parseType()
}

fun testLex(input: String) {
  val lexer = Lexer(input)
  do {
    println(lexer.next())
  } while (lexer.lookahead() != Token.EOF)
}

fun testParse(input: String) {
  val parser = Parser(Lexer(input))
  val expr = parser.parseExpression()
  print(expr)
}

fun test(input: String) {
  val parser = Parser(Lexer(input))
  val expr = parser.parseExpression()
  print(
    eval(
      persistentHashMapOf(), expr
    )
  )
}


fun main() {
  testLex("""-> => == / * + - %""")

  testParse(
    """
      int4 4
      ListAppend List[1, 2, 3] int4
   """.trimMargin()
  )
}