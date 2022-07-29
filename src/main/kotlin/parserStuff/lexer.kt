package parserStuff

class Lexer(input: String) {

    private val iter = PeekableIterator(input.iterator())
    var lh: Token? = null

    public fun lookahead(): Token {
        lh = next()
        return lh ?: Token.EOF
    }

    fun next(): Token {
        chompWhitespace()
        lh?.let { it -> lh = null; return it }
        return when (val c = iter.next()) {
            null -> Token.EOF
            ',' -> Token.COMMA
            '[' -> Token.SLPAREN
            ']' -> Token.SRPAREN
            '(' -> Token.LPAREN
            ')' -> Token.RPAREN
            '\\' -> Token.BACKSLASH
            ':' -> Token.COLON
            '+' -> Token.PLUS
            '/' -> Token.DIVIDES
            '*' -> Token.MULTIPLY
            '#' -> Token.HASH
            '%' -> Token.MODULO
            '-' -> if (iter.peek() == '>') {
                iter.next()
                Token.ARROW
            } else {
                Token.MINUS
            }
            '=' -> if (iter.peek() == '>') {
                iter.next()
                Token.EQ_ARROW
            } else if (iter.peek() == '=') {
                iter.next()
                Token.DOUBLE_EQUALS
            } else {
                Token.EQUALS
            }
            '"' -> lexString()
            else -> when {
                c.isJavaIdentifierStart() -> lexIdentifier(c)
                c.isDigit() -> lexInt(c)
                else -> throw Exception("Unexpected $c")
            }
        }
    }

    private fun lexInt(first: Char): Token {
        var res = first.toString()
        while (iter.peek()?.isDigit() == true) {
            res += iter.next()
        }
        return Token.INT_LIT(res.toInt())
    }

    private fun lexIdentifier(first: Char): Token {
        var res = first.toString()
        while (iter.peek()?.isJavaIdentifierPart() == true) {
            res += iter.next()
        }
        return when (res) {
            "if" -> Token.IF
            "then" -> Token.THEN
            "else" -> Token.ELSE
            "let" -> Token.LET
            "rec" -> Token.REC
            "in" -> Token.IN
            "true" -> Token.BOOL_LIT(true)
            "false" -> Token.BOOL_LIT(false)
            "Int" -> Token.INT
            "Bool" -> Token.BOOL
            "String" -> Token.STRING
            "List" -> Token.LIST
            "ListJoin" -> Token.LIST_OPERATION(ListOperation.LIST_JOIN)
            "ListAppend" -> Token.LIST_OPERATION(ListOperation.LIST_APPEND)
            "ListRemoveAtPosition" -> Token.LIST_OPERATION(ListOperation.LIST_REMOVE_AT_POSITION)
            "ListGetValue" -> Token.LIST_OPERATION(ListOperation.LIST_GET_VALUE)

            "ListInsertAt" -> Token.LIST_OPERATION(ListOperation.LIST_INSERT_AT)
            "ListGetSize" -> Token.LIST_OPERATION(ListOperation.LIST_GET_SIZE)
            "ListIsEmpty" -> Token.LIST_OPERATION(ListOperation.LIST_IS_EMPTY)
            "ListMap" -> Token.LIST_OPERATION(ListOperation.LIST_MAP)
            "ListFilter" -> Token.LIST_OPERATION(ListOperation.LIST_FILTER)
            "ListFold" -> Token.LIST_OPERATION(ListOperation.LIST_FOLD)
            else -> Token.IDENT(res)
        }
    }

    private fun lexString(): Token.STRING_LIT {
        var result = ""
        while (iter.peek() != '"') {
            val next = iter.next() ?: throw Error("Unterminated String Literal")
            result += next
        }
        iter.next()
        return Token.STRING_LIT(result)
    }

    private fun chompWhitespace() {
        while (iter.peek()?.isWhitespace() == true) {
            iter.next()
        }
    }
}
