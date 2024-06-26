import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javax.swing.JFrame
import javax.swing.JTabbedPane

class MainScreen : JFrame("Minecraft wrapper") {

    private var server: Server? = null
    private val fileExplorer = FileExplorerPanel()
    private val console = ConsolePanel()
    val statusPanel = StatusPanel()

    init {
        extendedState = MAXIMIZED_BOTH
        defaultCloseOperation = DO_NOTHING_ON_CLOSE
        addWindowListener(object : WindowAdapter() {
            override fun windowClosing(e: WindowEvent?) {
                START_SCREEN!!.isVisible = true
                isVisible = false
            }
        })
        layout = GridBagLayout()
        // File explorer
        add(fileExplorer, getConstraints(1, 1, weightx = 0.2, fill = GridBagConstraints.BOTH))
        // Main tabbed pane
        val tabbedPane = JTabbedPane()
        tabbedPane.addTab("Console", console)
        add(tabbedPane, getConstraints(2, 1, weightx = 0.8, fill = GridBagConstraints.BOTH, insets = getInsets(left = 10)))
        // Status bar
        add(statusPanel, getConstraints(1, 2, gridwidth = 2, weightx = 1.0))
    }

    fun setServer(server: Server) {
        this.server = server
        fileExplorer.setServer(server)
        console.setServer(server)
        isVisible = true
    }
}