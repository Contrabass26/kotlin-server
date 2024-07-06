import kotlinx.coroutines.*
import kotlinx.coroutines.swing.Swing
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.io.File
import java.util.Hashtable
import javax.swing.*
import kotlin.math.roundToInt

class CreateServerScreen : JFrame("Create server") {

    private val nameField: JTextField
    private val locationField: JTextField
    private val modLoaderRadios: ButtonGroup
    private val memorySlider: JSlider

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
        nameField = JTextField()
        createInnerPanel {
            it.add(nameLbl, getConstraints(anchor = GridBagConstraints.WEST, insets = getInsets(right = 5)))
            it.add(nameField, getConstraints(gridx = 2, weightx = 1.0, gridwidth = 2))
        }
        // Server location
        val locationLbl = JLabel("Server location:")
        locationField = JTextField()
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
        modLoaderRadios = ButtonGroup()
        ModLoader.entries
            .map { JRadioButton(it.toString(), it == ModLoader.VANILLA) }
            .forEach { modLoaderRadios.add(it) }
        createInnerPanel {
            it.add(modLoaderLbl, getConstraints(anchor = GridBagConstraints.WEST, insets = getInsets(right = 5)))
            modLoaderRadios.elements.iterator().forEach { radio ->
                it.add(radio, getConstraints(gridx = GridBagConstraints.RELATIVE, insets = getInsets(left = 5)))
            }
            it.add(JPanel(), getConstraints(gridx = GridBagConstraints.RELATIVE, weightx = 1.0))
        }
        // Minecraft version
        val mcVersionLbl = JLabel("Minecraft version:")
        val mcVersionComboModel = DefaultComboBoxModel<String>()
        val mcVersionCombo = JComboBox(mcVersionComboModel)
        @OptIn(DelicateCoroutinesApi::class)
        GlobalScope.launch(Dispatchers.Swing) {
            modLoaderInitJob!!.join()
            mcVersionComboModel.addAll(ModLoader.getMcVersions())
        }
        createInnerPanel {
            it.add(mcVersionLbl, getConstraints(anchor = GridBagConstraints.WEST, insets = getInsets(right = 5)))
            it.add(mcVersionCombo, getConstraints(gridx = GridBagConstraints.RELATIVE, weightx = 1.0))
        }
        // Memory allocation
        val memoryLbl = JLabel("Memory allocation:")
        memorySlider = JSlider(0, SYSTEM_MEMORY_GB * 1024).apply {
            majorTickSpacing = 1024
            snapToTicks = true
            labelTable = Hashtable<Int, JLabel>().apply {
                for (i in 0..SYSTEM_MEMORY_GB) {
                    this[i * 1024] = JLabel(i.toString())
                }
            }
            paintLabels = true
        }
        val memoryCheckBox = JCheckBox("Snap to GB", true).apply {
            addChangeListener {
                memorySlider.snapToTicks = isSelected
                if (isSelected) {
                    // Round to nearest 1024
                    memorySlider.value = (memorySlider.value / 1024f).roundToInt() * 1024
                }
            }
        }
        createInnerPanel {
            it.add(memoryLbl, getConstraints())
            it.add(memorySlider, getConstraints(gridx = 2, gridheight = 2, weightx = 1.0, insets = getInsets(left = 5)))
            it.add(memoryCheckBox, getConstraints(gridy = 2, anchor = GridBagConstraints.WEST, insets = getInsets(top = 5)))
        }
        // "Create" button
        val createBtn = JButton("Create")
        createBtn.addActionListener {
            if (validateOptions()) {

            }
        }
        add(createBtn, getConstraints(gridy = GridBagConstraints.RELATIVE, weightx = 1.0, insets = getInsets(top = 5, left = 5, right = 5)))
        // Padding
        add(JPanel(), getConstraints(gridy = GridBagConstraints.RELATIVE, fill = GridBagConstraints.BOTH))
    }

    private fun validateOptions(): Boolean {
        // Server name
        if (SERVERS.elements().asSequence().any { it.name == nameField.text }) {
            complain("There is already a server called \"${nameField.text}\". Server names must be unique.")
            return false
        }
        // Server location
        val location = File(locationField.text)
        if (!location.exists()) {
            complain("Selected server location does not exist: \"${locationField.text}\"")
            return false
        }
        val children: Array<File>? = location.listFiles()
        if (children == null) {
            complain("Selected server location is not a directory: \"${locationField.text}\"")
            return false
        }
        if (children.isNotEmpty()) {
            if (!warn("Selected server location is not empty - files may be overwritten."))
                return false
        }
        // Minecraft version and mod loader
        val modLoader = ModLoader.valueOf((modLoaderRadios.selection as JRadioButton).text.uppercase())

        return true
    }

    private fun complain(message: String) {
        JOptionPane.showMessageDialog(
            this,
            message,
            "Invalid options",
            JOptionPane.ERROR_MESSAGE
        )
    }

    private fun warn(message: String): Boolean { // Returns whether the user chose to continue
        return JOptionPane.showConfirmDialog(
            this,
            message,
            "Suspicious options",
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.WARNING_MESSAGE
        ) == JOptionPane.OK_OPTION
    }

//    private fun updateMemoryFeedback() {
//        when {
//            (memorySlider.value < 1024) ->
//                memoryFeedbackLbl.setFeedback("You probably need more memory than that", Color.ORANGE)
//
//            (memorySlider.value > SYSTEM_MEMORY_MB - 2048) ->
//                memoryFeedbackLbl.setFeedback("Other programs probably need more memory than that", Color.ORANGE)
//
//            else ->
//                memoryFeedbackLbl.setFeedback("Valid memory allocation", Color.GREEN)
//        }
//    }

    fun showScreen() {
        isVisible = true
    }

    private fun createInnerPanel(addComponents: (JPanel) -> Unit) {
        val panel = JPanel()
        panel.layout = GridBagLayout()
        addComponents(panel)
        add(panel, getConstraints(gridy = GridBagConstraints.RELATIVE, weightx = 1.0, insets = getInsets(5, 5, 5, 5)))
        add(JSeparator(), getConstraints(gridy = GridBagConstraints.RELATIVE, weightx = 1.0))
    }
}