import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javax.swing.JFrame

class MainScreen : JFrame("Minecraft wrapper") {

    private var server: Server? = null
    private val fileExplorer: FileExplorerPanel
    private val console = ConsolePanel()
    val statusPanel = StatusPanel()
    private val tabbedPane: MainTabbedPane

    init {
        extendedState = MAXIMIZED_BOTH
        defaultCloseOperation = DO_NOTHING_ON_CLOSE
        layout = GridBagLayout()
        // Tabbed pane
        tabbedPane = MainTabbedPane(console)
        add(tabbedPane, getConstraints(2, 1, weightx = 0.8, fill = GridBagConstraints.BOTH))
        // File explorer
        fileExplorer = FileExplorerPanel(tabbedPane)
        add(fileExplorer, getConstraints(1, 1, weightx = 0.2, fill = GridBagConstraints.BOTH, insets = getInsets(5, 5, 5, 5)))
        // Status bar
        add(statusPanel, getConstraints(1, 2, gridwidth = 2, weightx = 1.0))
        // Window listener
        addWindowListener(object : WindowAdapter() {
            override fun windowClosing(e: WindowEvent?) {
                START_SCREEN!!.isVisible = true
                isVisible = false
                fileExplorer.dispose()
                tabbedPane.dispose()
            }
        })
    }

    fun setServer(server: Server) {
        this.title = server.name
        this.server = server
        server.lastOpened = System.currentTimeMillis()
        fileExplorer.setServer(server)
        console.setServer(server)
        isVisible = true
    }
}