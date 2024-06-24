import java.io.File
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel

class FileTreeModel(rootDir: File) : DefaultTreeModel(DefaultMutableTreeNode(rootDir.name)) {

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
}