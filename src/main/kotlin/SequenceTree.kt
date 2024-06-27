class SequenceTree<T, S>(private val sequence: Sequence<T>) {

    private var branchChild: SequenceTree<S, Any>? = null
    private var copyChild: SequenceTree<T, S>? = null

    fun branch(function: (T) -> S) {
        if (branchChild == null) {
            branchChild = SequenceTree(sequence.map { function(it) })
        } else {
            if (copyChild == null) {
                copyChild = SequenceTree(sequence)
            }
            copyChild!!.branch(function)
        }
    }
}