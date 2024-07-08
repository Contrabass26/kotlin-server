import java.awt.Color
import java.awt.Graphics
import javax.swing.JPanel

enum class FileHandler {

    SERVER_PROPERTIES {
        override fun supportsFile(relativeName: String): Boolean =
            relativeName == "server.properties"

        override fun getTab(): FileTab {
            return object : FileTab() {
                override fun paint(g: Graphics?) {
                    super.paint(g)
                    g!!.color = Color.RED
                    g.fillRect(100, 100, 200, 200)
                }
            }
        }
    };

    companion object {
        fun get(relativeName: String): FileHandler? =
            entries.find { it.supportsFile(relativeName) }
    }

    abstract fun supportsFile(relativeName: String): Boolean

    abstract fun getTab(): FileTab
}