import kotlinx.coroutines.runBlocking
import java.awt.Color
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.util.Dictionary
import java.util.Hashtable
import javax.swing.*
import kotlin.math.roundToInt


class CreateServerScreen : JFrame("Create server") {

    private val memoryFeedbackLbl: JLabel
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
        memoryFeedbackLbl = JLabel()
        memorySlider = JSlider(0, SYSTEM_MEMORY_MB).apply {
            majorTickSpacing = 1024
            snapToTicks = true
            labelTable = Hashtable<Int, JLabel>().apply {
                for (i in 0..SYSTEM_MEMORY_MB step 1024) {
                    this[i] = JLabel(i.toString())
                }
            }
            paintLabels = true
            addChangeListener { updateMemoryFeedback() }
        }
        updateMemoryFeedback()
        val memoryCheckBox = JCheckBox("Snap to GB", true).apply {
            horizontalAlignment = SwingConstants.RIGHT
            addChangeListener {
                memorySlider.snapToTicks = isSelected
                if (isSelected) {
                    // Round to nearest 1024
                    memorySlider.value = (memorySlider.value / 1024f).roundToInt() * 1024
                }
            }
        }
        createInnerPanel(true) {
            it.add(memoryLbl, getConstraints())
            it.add(memorySlider, getConstraints(gridx = 2, weightx = 1.0, insets = getInsets(left = 5, right = 5)))
            it.add(memoryCheckBox, getConstraints(gridx = 3))
            it.add(memoryFeedbackLbl, getConstraints(gridy = 2, gridwidth = 3, insets = getInsets(top = 5), weightx = 1.0))
        }
        // Padding
        add(JPanel(), getConstraints(gridy = GridBagConstraints.RELATIVE, fill = GridBagConstraints.BOTH))
    }

    private fun updateMemoryFeedback() {
        when {
            (memorySlider.value < 1024) ->
                memoryFeedbackLbl.setFeedback("You probably need more memory than that", Color.ORANGE)

            (memorySlider.value > SYSTEM_MEMORY_MB - 2048) ->
                memoryFeedbackLbl.setFeedback("Other programs probably need more memory than that", Color.ORANGE)

            else ->
                memoryFeedbackLbl.setFeedback("Valid memory allocation", Color.GREEN)
        }
    }

    private fun JLabel.setFeedback(text: String, color: Color) {
        this.text = text
        this.foreground = color
    }

    fun showScreen() {
        isVisible = true
    }

    private fun createInnerPanel(last: Boolean = false, addComponents: (JPanel) -> Unit) {
        val panel = JPanel()
        panel.layout = GridBagLayout()
        addComponents(panel)
        add(panel, getConstraints(gridy = GridBagConstraints.RELATIVE, weightx = 1.0, insets = getInsets(5, 5, 5, 5)))
        if (!last) {
            add(JSeparator(), getConstraints(gridy = GridBagConstraints.RELATIVE, weightx = 1.0))
        }
    }
}