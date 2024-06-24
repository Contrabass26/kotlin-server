import java.awt.Dimension
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import javax.swing.JFrame
import javax.swing.JLabel
import javax.swing.JPanel

class MainScreen : JFrame("Minecraft wrapper") {

    private var server: Server? = null
    private val fileExplorer: FileExplorerPanel = FileExplorerPanel()

    init {
        extendedState = MAXIMIZED_BOTH
        defaultCloseOperation = EXIT_ON_CLOSE
        layout = GridBagLayout()
        add(fileExplorer, getConstraints(1, 1, weightx = 0.2, fill = GridBagConstraints.BOTH))
        // Padding
        val panel = JPanel()
        add(panel, getConstraints(2, 1, fill = GridBagConstraints.BOTH))
    }

    fun setServer(server: Server) {
        this.server = server
        fileExplorer.setServer(server)
        isVisible = true
    }
}