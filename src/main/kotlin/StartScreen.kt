import java.awt.GridBagLayout
import javax.swing.JButton
import javax.swing.JFrame
import javax.swing.JLabel
import javax.swing.JPanel

class StartScreen : JFrame("Welcome") {

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
        // Filler panel
        val filler = JPanel()
        add(filler, getConstraints(1, 2, gridwidth = 2, weightx = 1.0, weighty = 1.0))
        // Finalise
        isVisible = true
    }
}