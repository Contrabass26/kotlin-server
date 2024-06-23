import java.awt.GridBagLayout
import javax.swing.*
import javax.swing.event.ListDataEvent
import javax.swing.event.ListDataListener

class StartScreen : JFrame("Welcome") {

    private var nextServerY = 3

    init {
        defaultCloseOperation = EXIT_ON_CLOSE
        setSize(0.3, 0.5)
        setLocationRelativeTo(null) // Centre window
        layout = GridBagLayout()
        // Select server label
        val selectServerLbl = JLabel("Select a server:")
        selectServerLbl.font = FONT
        add(selectServerLbl, getConstraints(1, 1, weightx = 1.0, insets = getInsets(top = 5, left = 5)))
        // New server button
        val newServerBtn = JButton("New")
        add(newServerBtn, getConstraints(2, 1, insets = getInsets(left = 20, right = 5, top = 5)))
        // Server list
        SERVERS.addListDataListener(object : ListDataListener {
            override fun intervalAdded(e: ListDataEvent?) {
                TODO("Not yet implemented")
            }

            override fun intervalRemoved(e: ListDataEvent?) {
                TODO("Not yet implemented")
            }

            override fun contentsChanged(e: ListDataEvent?) {
                TODO("Not yet implemented")
            }

        })
        // Finalise
        isVisible = true
    }

    private fun addServer(server: Server) {
        // Name label
        val nameLbl = JLabel(server.name)
        add(nameLbl, getConstraints(1, nextServerY, weightx = 1.0))
        // Open button
        val openBtn = JButton("Open")
        add(openBtn, getConstraints(2, nextServerY))
        // Tell server what components to remove
        server.startScreenComponents = Server.StartScreenComponents(nameLbl, openBtn)
    }
}