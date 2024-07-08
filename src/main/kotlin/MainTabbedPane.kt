import java.awt.Dimension
import javax.swing.JTabbedPane

class MainTabbedPane(console: ConsolePanel) : JTabbedPane() {

    init {
        addTab("Console", console)
    }

    fun openFile(relativePath: String, handler: FileHandler) {
        if (indexOfTab(relativePath) == -1)
            addTab(relativePath, handler.getTab())
        selectedIndex = indexOfTab(relativePath)
    }

    fun closeFile(relativePath: String) {
        val index = indexOfTab(relativePath)
        if (index != -1)
            removeTabAt(index)
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