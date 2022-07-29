package parserStuff

sealed class Token {

    @Override
    override fun toString(): String {
        return this.javaClass.simpleName
    }

    // Keyword
    object IF : Token()
    object THEN : Token()
    object ELSE : Token()
    object LET : Token()
    object REC : Token()
    object IN : Token()

    object INT : Token()
    object BOOL : Token()
    object STRING : Token()

    object LIST : Token()



    // Symbols
    object LPAREN : Token()     // (
    object RPAREN : Token()     // )
    object SLPAREN : Token()    // [
    object SRPAREN : Token()    // ]
    object ARROW : Token()      // ->
    object EQ_ARROW : Token()   // =>
    object BACKSLASH : Token()  // \
    object EQUALS : Token()     // =
    object COLON : Token()      // :
    object COMMA : Token()      // ,

    // Literal
    data class BOOL_LIT(val bool: Boolean) : Token()
    data class INT_LIT(val int: Int) : Token()
    data class STRING_LIT(val string: String): Token()
    data class LIST_LIT(val listType: Class<Token>, val list: List<Token>): Token()

    data class IDENT(val ident: String) : Token()

    // Operator
    object PLUS : Token()
    object MINUS : Token()
    object MULTIPLY : Token()
    object DIVIDES : Token()
    object DOUBLE_EQUALS : Token()
    object HASH : Token()
    object MODULO : Token()

    data class LIST_OPERATION(val operation: ListOperation) : Token()

    // Control
    object EOF : Token()
}

sealed class ListOperation{
    object LIST_JOIN : ListOperation()
    object LIST_APPEND : ListOperation()
    object LIST_INSERT_AT : ListOperation()
    object LIST_REMOVE_AT_POSITION : ListOperation()
    object LIST_GET_VALUE : ListOperation()

    object LIST_GET_SIZE : ListOperation()
    object LIST_IS_EMPTY : ListOperation()

    object LIST_MAP : ListOperation()
    object LIST_FILTER : ListOperation()
    object LIST_FOLD : ListOperation()

}


