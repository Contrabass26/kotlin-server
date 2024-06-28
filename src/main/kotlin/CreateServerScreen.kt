import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javax.swing.*


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
        // Server name
        val nameLbl = JLabel("Server name:")
        add(nameLbl, getConstraints(1, 1, anchor = GridBagConstraints.WEST, insets = getInsets(left = 5, top = 5, right = 5)))
        val nameField = JTextField()
        add(nameField, getConstraints(2, 1, weightx = 1.0, gridwidth = 2, insets = getInsets(top = 5, right = 5)))
        // Server location
        val locationLbl = JLabel("Server location:")
        add(locationLbl, getConstraints(1, 2, anchor = GridBagConstraints.WEST, insets = getInsets(top = 5, left = 5, right = 5)))
        val locationField = JTextField()
        add(locationField, getConstraints(2, 2, weightx = 1.0, insets = getInsets(top = 5, right = 5)))
        val locationBtn = JButton("Choose")
        locationBtn.addActionListener {
            val fileChooser = JFileChooser(USER_HOME)
            fileChooser.fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
            fileChooser.showOpenDialog(CREATE_SERVER_SCREEN)
            fileChooser.selectedFile?.absolutePath?.let { locationField.text = it }
        }
        add(locationBtn, getConstraints(3, 2, insets = getInsets(top = 5, right = 5)))
        // Padding
        add(JPanel(), getConstraints(1, GridBagConstraints.RELATIVE, gridwidth = 3, fill = GridBagConstraints.BOTH))
    }

    fun showScreen() {
        isVisible = true
    }
}