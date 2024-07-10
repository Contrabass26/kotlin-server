import java.awt.Dimension
import javax.swing.JTabbedPane

class MainTabbedPane(console: ConsolePanel) : JTabbedPane() {

    init {
        addTab("Console", console)
    }

    fun openFile(relativePath: String, server: Server) {
        val tabType = ServerConfigTabType.getForFile(relativePath)
        if (indexOfTab(relativePath) == -1) {
            if (tabType != null)
                addTab(relativePath, tabType.createTab(server))
        }
        if (tabType != null) {
            selectedIndex = indexOfTab(relativePath)
        }
    }

    fun closeFile(relativePath: String) {
        val index = indexOfTab(relativePath)
        if (index != -1) {
            val tab = getComponentAt(index) as ServerConfigTab
            removeTabAt(index)
            tab.onCloseTab()
        }
    }

    fun dispose() {
        // Close all tabs
        for (i in 1..<tabCount) {
            val tab = getComponentAt(1) as ServerConfigTab
            tab.onCloseTab()
            removeTabAt(1)
        }
    }

    fun updateFile(relativePath: String) {
        val index = indexOfTab(relativePath)
        if (index != -1) {
            val tab = getComponentAt(index) as ServerConfigTab
            tab.onFileUpdate()
        }
    }

    override fun getPreferredSize(): Dimension {
        return Dimension((MAIN_SCREEN!!.width * 0.8).toInt(), MAIN_SCREEN!!.height)
    }
}