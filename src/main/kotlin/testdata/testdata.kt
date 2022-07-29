package testdata

import testInput

fun spacer(){
    println("\n -------------------------------------------- \n")
}

fun testAppend(){
    spacer()

    println("LIST_APPEND\n")

    println("Simply appending an Int to a List: 'ListAppend List[1,2,3] 4' -> Result: List[1,2,3,4]")
    testInput("""
        let testList = List[1,2,3] in
        ListAppend testList 4
    """.trimIndent())

    println()

    println("Appending nested Lists: 'ListAppend List[ List[1,2], List[3,4] ] List[5,6]' -> Result: List[ List[1,2], List[3,4], List[5,6] ]")
    testInput("""
        let testList = List[ List[1,2], List[3,4] ] in
        ListAppend testList List[5,6]
    """.trimIndent())

    spacer()
}

fun testJoin(){
    spacer()

    println("LIST_JOIN\n")

    println("Joining two Int-Lists: 'ListJoin List[1,2] List[3,4]' -> Result: List[1,2,3,4]")
    testInput("""
        let list1 = List[1,2] in
        let list2 = List[3,4] in
        ListJoin list1 list2
    """.trimIndent())

    spacer()
}

fun testGetValue(){
    spacer()

    println("LIST_GET_VALUE\n")

    println("Getting the Value at index 2 from a List: List[1,2,3,4] 2 -> Result: Int(3)")
    testInput("""
        ListGetValue List[1,2,3,4] 2
    """.trimIndent())

}


fun testGetSize() {
    spacer()


    println("LIST_GET_SIZE\n")

    println("Get Size of List: 'ListGetSize List[1,2,3,4]' -> Result: Int(4)")
    testInput(
        """
        ListGetSize List[1,2,3,4]
    """.trimIndent()
    )

    spacer()
}

fun testRemoveAtPosition() {
    spacer()

    println("REMOVE_AT_POSITION\n")

    println("Remove the Element at Position 1: 'ListRemoveAtPosition List[0,1,2]' -> Result: List[0,2]")
    testInput(
        """
        ListRemoveAtPosition List[0,1,2] 1
      """.trimIndent()
        )

    println()

    println("Remove last Element of List: 'ListRemoveAtPosition List[1,2,3,4] ((ListGetSize List[1,2,3,4]) - 1)' -> Result: List[1,2,3]")
    testInput(
        """
        let testList = List[1,2,3,4] in
        ListRemoveAtPosition testList ((ListGetSize testList) - 1 )
    """.trimIndent()
    )

    spacer()
}

fun testIsEmpty(){
    spacer()

    println("LIST_IS_EMPTY\n")

    println("List is not Empty: 'ListIsEmpty List[1]' -> Result: false")
    testInput("""
        ListIsEmpty List[1]
    """.trimIndent())

    println()

    println("List is Empty: 'ListIsEmpty ListRemoveAtPosition List[1] 0' -> Result: true")
    testInput("""
        ListIsEmpty ListRemoveAtPosition List[1] 0
    """.trimIndent())

    spacer()
}

fun testInsertAt() {
    spacer()

    println("LIST_INSERT_AT\n")

    println("Insert Value 2 at Index 1: ListInsertAt List[1,3] 2 1 -> Result: List[1,2,3]")
    testInput("""
        ListInsertAt List[1,3] 2 1
    """.trimIndent())
    spacer()
}


fun testMap(){
    spacer()

    println("LIST_MAP\n")

    println("""
        Double every Value in the List:
        #   let double = \x => x * 2 in
        #   ListMap List[1,2,3] double
        -> Result: List[2,4,6]
    """.trimIndent())
    testInput("""
        let double = \x => x * 2 in
        ListMap List[1,2,3] double
    """.trimIndent())

    println()

    println("""
        Get the Size of every nested List:
        #   let sizeOfList = \l => ListGetSize l in
        #   let testList = List[ List[1,2,3], List[1,2,3,4], List[1,2,3,4,5,6] ] in
        #   ListMap testList sizeOfList
        -> Result: List[3,4,6]
    """.trimIndent())
    testInput("""
        let sizeOfList = \l => ListGetSize l in
        let testList = List[ List[1,2,3], List[1,2,3,4], List[1,2,3,4,5,6] ] in
        ListMap testList sizeOfList
    """.trimIndent())

    println()

    println("""
        Map does also work with recursive functions (PoC):
        #   let rec recTest = \x => if x == 0 then 0
        #       else recTest (x-1) in
        #   ListMap List[3,4,5] recTest
        -> Result: List[0,0,0]
    """.trimIndent())
    testInput("""
        let rec recTest = \x => if x == 0 then 0
            else recTest (x-1) in
        
        ListMap List[3,4,5] recTest
    """.trimIndent())

    spacer()
}

fun testFold(){
    spacer()

    println("LIST_FOLD\n")

    println("""
        List-Folding:
        #   let doMath = \x => \y => (x * 7) - y in
        #   ListFold List[1,2,3] doMath
        -> Result: 32
    """.trimIndent())
    testInput("""
        let doMath = \x => \y => (x * 7) - y in
        ListFold List[1,2,3] doMath
    """.trimIndent())

    spacer()
}

fun testFilter(){
    spacer()

    println("""
        Filter a given List for Int(2)
        # let filterInt = \x => x == 2 in
        # ListFilter List[1,2,3,2] filterInt
        -> Result: List[2,2]
        """.trimIndent())
    testInput(
        """           
            let filterInt = \x => x == 2 in
            ListFilter List[1,2,3,2] filterInt
        """.trimIndent()
    )

    println()

    println("""
        Checks if Elements in List are even and returns them
        # let isEven = \x => if (x % 2) == 1 then false
        #   else true in
        # ListFilter List[1,2,3,4,5,6,7] istEven
        -> Result: List[2,4,6]
    """.trimIndent())
    testInput("""
        let isEven = \x => if (x % 2) == 1 then false
            else true in
        
        ListFilter List[1,2,3,4,5,6,7] isEven
    """.trimIndent())

    spacer()
}

fun main() {

    testAppend()
    testJoin()
    testGetValue()
    testGetSize()
    testRemoveAtPosition()
    testIsEmpty()
    testInsertAt()
    testMap()
    testFold()
    testFilter()

}