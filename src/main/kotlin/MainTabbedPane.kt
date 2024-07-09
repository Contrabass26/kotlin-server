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
            val tab = getTabComponentAt(index) as ServerConfigTab
            removeTabAt(index)
            tab.onCloseTab()
        }
    }

    fun updateFile(relativePath: String) {
        if (indexOfTab(relativePath) != -1) {
            println("Updating $relativePath")
        }
    }

    override fun getPreferredSize(): Dimension {
        return Dimension((MAIN_SCREEN!!.width * 0.8).toInt(), MAIN_SCREEN!!.height)
    }
}