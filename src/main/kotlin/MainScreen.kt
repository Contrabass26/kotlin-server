import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import javax.swing.JFrame
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JTabbedPane

class MainScreen : JFrame("Minecraft wrapper") {

    private var server: Server? = null
    private val fileExplorer = FileExplorerPanel()
    private val console = ConsolePanel()

    init {
        extendedState = MAXIMIZED_BOTH
        defaultCloseOperation = EXIT_ON_CLOSE
        layout = GridBagLayout()
        // File explorer
        add(fileExplorer, getConstraints(1, 1, weightx = 0.2, fill = GridBagConstraints.BOTH))
        // Main tabbed pane
        val tabbedPane = JTabbedPane()
        tabbedPane.addTab("Console", console)
        add(tabbedPane, getConstraints(2, 1, weightx = 0.8, fill = GridBagConstraints.BOTH, insets = getInsets(left = 10)))
    }

    fun setServer(server: Server) {
        this.server = server
        fileExplorer.setServer(server)
        console.setServer(server)
        isVisible = true
    }
}