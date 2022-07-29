package parserStuff

class PeekableIterator<T>(val iter: Iterator<T>) {
    var lh: T? = null
    fun peek(): T? {
        lh = next()
        return lh
    }

    fun next(): T? {
        lh?.let { lh = null; return it }
        return if (iter.hasNext()) {
            iter.next()
        } else {
            null
        }
    }
}