import kotlinx.coroutines.runBlocking
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
        val nameField = JTextField()
        createInnerPanel {
            it.add(nameLbl, getConstraints(anchor = GridBagConstraints.WEST, insets = getInsets(right = 5)))
            it.add(nameField, getConstraints(gridx = 2, weightx = 1.0, gridwidth = 2))
        }
        // Server location
        val locationLbl = JLabel("Server location:")
        val locationField = JTextField()
        val locationBtn = JButton("Choose")
        locationBtn.addActionListener {
            val fileChooser = JFileChooser(USER_HOME)
            fileChooser.fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
            fileChooser.showOpenDialog(CREATE_SERVER_SCREEN)
            fileChooser.selectedFile?.absolutePath?.let { locationField.text = it }
        }
        createInnerPanel {
            it.add(locationLbl, getConstraints(anchor = GridBagConstraints.WEST, insets = getInsets(right = 5)))
            it.add(locationField, getConstraints(gridx = 2, weightx = 1.0, insets = getInsets(right = 5)))
            it.add(locationBtn, getConstraints(gridx = 3))
        }
        // Mod loader
        val modLoaderLbl = JLabel("Mod loader:")
        val modLoaderRadios = ButtonGroup()
        ModLoader.entries
            .map { JRadioButton(it.toString()) }
            .forEach(modLoaderRadios::add)
        createInnerPanel {
            it.add(modLoaderLbl, getConstraints(anchor = GridBagConstraints.WEST, insets = getInsets(right = 5)))
            modLoaderRadios.elements.iterator().forEach { radio ->
                it.add(radio, getConstraints(gridx = GridBagConstraints.RELATIVE, insets = getInsets(left = 5)))
            }
            it.add(JPanel(), getConstraints(gridx = GridBagConstraints.RELATIVE, weightx = 1.0))
        }
        // Minecraft version
        val mcVersionLbl = JLabel("Minecraft version:")
        val mcVersions = runBlocking { ModLoader.getMcVersions() }
        val mcVersionCombo = JComboBox(mcVersions.toTypedArray())
        createInnerPanel {
            it.add(mcVersionLbl, getConstraints(anchor = GridBagConstraints.WEST, insets = getInsets(right = 5)))
            it.add(mcVersionCombo, getConstraints(gridx = GridBagConstraints.RELATIVE, weightx = 1.0))
        }
        // Memory allocation
        val memoryLbl = JLabel("Memory allocation:")
        val memorySlider = JSlider(0, )
        // Padding
        add(JPanel(), getConstraints(gridy = GridBagConstraints.RELATIVE, fill = GridBagConstraints.BOTH))
    }

    fun showScreen() {
        isVisible = true
    }

    private fun createInnerPanel(addComponents: (JPanel) -> Unit) {
        val panel = JPanel()
        panel.layout = GridBagLayout()
        addComponents(panel)
        add(panel, getConstraints(gridy = GridBagConstraints.RELATIVE, weightx = 1.0, insets = getInsets(5, 5, 5, 5)))
    }
}