import kotlinx.coroutines.*
import kotlinx.coroutines.swing.Swing
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.io.File
import java.util.Hashtable
import javax.swing.*
import javax.swing.JToggleButton.ToggleButtonModel
import kotlin.math.roundToInt

class CreateServerScreen : JFrame("Create server") {

    private val nameField: JTextField
    private val locationField: JTextField
    private val modLoaderRadios: List<JRadioButton>
    private val mcVersionCombo: JComboBox<String>
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
        modLoaderRadios = ModLoader.entries
            .map { JRadioButton(it.toString(), it == ModLoader.VANILLA) }
            .toList()
        val modLoaderBtnGroup = ButtonGroup()
        modLoaderRadios.forEach(modLoaderBtnGroup::add)
        createInnerPanel {
            it.add(modLoaderLbl, getConstraints(anchor = GridBagConstraints.WEST, insets = getInsets(right = 5)))
            modLoaderRadios.forEach { radio ->
                it.add(radio, getConstraints(gridx = GridBagConstraints.RELATIVE, insets = getInsets(left = 5)))
            }
            it.add(JPanel(), getConstraints(gridx = GridBagConstraints.RELATIVE, weightx = 1.0))
        }
        // Minecraft version
        val mcVersionLbl = JLabel("Minecraft version:")
        val mcVersionComboModel = DefaultComboBoxModel<String>()
        mcVersionCombo = JComboBox(mcVersionComboModel)
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
                // Add server
                val server = Server(
                    nameField.text,
                    File(locationField.text),
                    mcVersionCombo.selectedItem as String,
                    ModLoader.valueOf(modLoaderRadios.getSelected().text.uppercase()),
                    memorySlider.value,
                    "java",
                    System.currentTimeMillis()
                )
                SERVERS.insertSorted(server)
                // Download files
                @OptIn(DelicateCoroutinesApi::class)
                GlobalScope.launch(Dispatchers.IO) { server.downloadFiles() }
            }
        }
        add(createBtn, getConstraints(gridy = GridBagConstraints.RELATIVE, weightx = 1.0, insets = getInsets(top = 5, left = 5, right = 5)))
        // Padding
        add(JPanel(), getConstraints(gridy = GridBagConstraints.RELATIVE, fill = GridBagConstraints.BOTH))
    }

    private fun List<JRadioButton>.getSelected(): JRadioButton = find { it.isSelected }!!

    private fun validateOptions(): Boolean {
        // Server name
        if (nameField.text.isEmpty()) {
            complain("No server name provided.")
            return false
        }
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
            warn("Selected server location is not empty - files may be overwritten.")?.let { return false }
        }
        // Minecraft version and mod loader
        val modLoader = ModLoader.valueOf(modLoaderRadios.getSelected().text.uppercase())
        val mcVersion = mcVersionCombo.selectedItem as String?
        if (mcVersion == null) {
            complain("No Minecraft version selected.")
            return false
        }
        runBlocking {
            if (!modLoader.supportsVersion(mcVersion)) {
                complain("$modLoader does not support $mcVersion.")
                return@runBlocking Unit
            }
            null
        }?.let { return false }
        // Memory allocation
        val memory = memorySlider.value
        when {
            (memory < 1024) ->
                warn("You probably need more memory than that.")

            (memory > SYSTEM_MEMORY_MB - 2048) ->
                warn("Other programs probably need more memory than that.")

            else -> null
        }?.let { return false }
        // Everything is fine
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

    private fun warn(message: String): Unit? { // Returns Unit if the user chose to stop
        return if (JOptionPane.showConfirmDialog(
            this,
            message,
            "Suspicious options",
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.WARNING_MESSAGE
        ) == JOptionPane.OK_OPTION) null else Unit
    }

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