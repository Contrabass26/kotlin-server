import java.io.File
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.MutableTreeNode
import javax.swing.tree.TreeNode

class FileTreeModel(rootDir: File) : DefaultTreeModel(DefaultMutableTreeNode(rootDir.absolutePath)) {

    private val rootPath = rootDir.absolutePath

    init {
        explore(rootDir, root as DefaultMutableTreeNode)
    }

    private fun explore(dir: File, node: DefaultMutableTreeNode) {
        dir.listFiles()?.forEach {
            val child = DefaultMutableTreeNode(it.name)
            if (it.isDirectory) {
                explore(it, child)
            }
            node.add(child)
        }
    }

    private fun TreeNode.getChild(child: String): TreeNode? = children().asSequence().find { (it as DefaultMutableTreeNode).userObject == child }

    private fun insertNodeInto(newChild: DefaultMutableTreeNode, parent: MutableTreeNode) {
        val childValue = newChild.userObject as String
        var index = parent.children()
            .asSequence()
            .map { it as DefaultMutableTreeNode }
            .map { it.userObject as String }
            .indexOfFirst { childValue < it }
        if (index == -1) index = parent.childCount
        insertNodeInto(newChild, parent, index)
    }

    fun addFile(path: String) {
        var current = this.root as MutableTreeNode
        val splits = path.split(File.separatorChar)
        for (split in splits) {
            val child = current.getChild(split) as MutableTreeNode?
            if (child == null) {
                // Add a child
                val newChild = DefaultMutableTreeNode(split)
                insertNodeInto(newChild, current)
                current = newChild
            } else {
                current = child
            }
        }
    }

    fun deleteFile(path: String) {
        var current = this.root as MutableTreeNode
        val splits = path.split(File.separatorChar)
        for (split in splits) {
            current = current.getChild(split) as MutableTreeNode
        }
        removeNodeFromParent(current)
    }
}