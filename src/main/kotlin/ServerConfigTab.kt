import javax.swing.JPanel

abstract class ServerConfigTab : JPanel() {

    abstract fun onCloseTab()

    abstract fun onFileUpdate()
}