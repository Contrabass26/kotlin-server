import java.awt.*
import javax.swing.JPanel
import javax.swing.JTree

class FileExplorerPanel : JPanel() {

    private val tree = JTree()

    init {
        layout = GridBagLayout()
        val constraints = getConstraints(1, 1, fill = GridBagConstraints.BOTH, ipadx = 10, ipady = 10)
        add(tree, constraints)
        background = tree.background
    }

    fun setServer(server: Server) {
        val model = FileTreeModel(server.location)
        tree.model = model
    }
}