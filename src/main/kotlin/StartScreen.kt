import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javax.swing.JButton
import javax.swing.JFrame
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JSeparator
import javax.swing.event.ListDataEvent
import javax.swing.event.ListDataListener
import kotlin.system.exitProcess

fun dataAddedListener(consumer: (ListDataEvent?) -> Unit): ListDataListener {
    return object : ListDataListener {
        override fun intervalAdded(e: ListDataEvent?) {
            consumer(e)
        }

        override fun intervalRemoved(e: ListDataEvent?) {}

        override fun contentsChanged(e: ListDataEvent?) {}
    }
}

class StartScreen : JFrame("Welcome") {

    private var nextServerY = 4
    private var padding: JPanel

    init {
        defaultCloseOperation = DO_NOTHING_ON_CLOSE
        addWindowListener(object : WindowAdapter() {
            override fun windowClosing(e: WindowEvent?) {
                cancelStatusUpdate()
                dispose()
                exitProcess(0)
            }
        })
        setSize(0.3, 0.5)
        setLocationRelativeTo(null) // Centre window
        layout = GridBagLayout()
        // Select server label
        val selectServerLbl = JLabel("Select or create a server:")
        selectServerLbl.font = FONT
        add(selectServerLbl, getConstraints(1, 1, weightx = 1.0, insets = getInsets(top = 5, left = 5)))
        // New server button
        val newServerBtn = JButton("New")
        newServerBtn.addActionListener {
            CREATE_SERVER_SCREEN?.showScreen()
            isVisible = false
        }
        add(newServerBtn, getConstraints(2, 1, insets = getInsets(right = 5, top = 5)))
        // Divider
        add(JSeparator(JSeparator.HORIZONTAL), getConstraints(1, 2, gridwidth = 2, weightx = 1.0, insets = getInsets(top = 10, bottom = 10)))
        // Server list
        SERVERS.addListDataListener(dataAddedListener {
            val start = it!!.index0
            val end = it.index1
            for (i in start..end) {
                val server = SERVERS.getElementAt(i)
                addServer(server)
            }
        })
        // Padding
        padding = JPanel()
        add(padding, getConstraints(1, 3, gridwidth = 2, fill = GridBagConstraints.BOTH))
        // Finalise
        isVisible = true
    }

    private fun refreshPadding() {
        remove(padding)
        add(padding, getConstraints(1, nextServerY, gridwidth = 2, fill = GridBagConstraints.BOTH))
    }

    private fun addServer(server: Server) {
        // Name label
        val nameLbl = JLabel(server.name)
        add(nameLbl, getConstraints(1, nextServerY, weightx = 1.0, insets = getInsets(left = 5, bottom = 5)))
        // Open button
        val openBtn = JButton("Open")
        openBtn.addActionListener {
            MAIN_SCREEN?.setServer(server)
            isVisible = false
        }
        add(openBtn, getConstraints(2, nextServerY, insets = getInsets(right = 5, bottom = 5)))
        // Tell server what components to remove
        server.startScreenComponents = Server.StartScreenComponents(nameLbl, openBtn)
        nextServerY++
        refreshPadding()
        refreshGui()
    }
}