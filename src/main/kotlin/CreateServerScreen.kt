import java.awt.GridBagLayout
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javax.swing.JFrame
import javax.swing.JLabel

class CreateServerScreen : JFrame("Create server") {

    init {
        setSize(0.5, 0.7)
        setLocationRelativeTo(null) // Centre window
        defaultCloseOperation = DO_NOTHING_ON_CLOSE
        addWindowListener(object : WindowAdapter() {
            override fun windowClosing(e: WindowEvent?) {
                START_SCREEN!!.isVisible = true
                isVisible = false
            }
        })
        layout = GridBagLayout()
        add(JLabel("Cheese"), getConstraints(1, 1))
    }

    fun showScreen() {
        isVisible = true
    }
}