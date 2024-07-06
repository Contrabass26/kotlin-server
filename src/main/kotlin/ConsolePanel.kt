import kotlinx.coroutines.*
import java.awt.Color
import java.awt.Dimension
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import javax.swing.*

private fun ConsoleWrapper.sendCommand(command: String) {
    send("$command\n")
}

@OptIn(DelicateCoroutinesApi::class)
class ConsolePanel : JPanel() {

    private var consoleWrapper: ConsoleWrapper? = null
    private var server: Server? = null
    private val outputBox = object : JTextArea() {
        // Size shouldn't change depending on contents
        override fun getPreferredSize(): Dimension {
            return Dimension(400, 400)
        }
    }
    private val startBtn = JButton()

    init {
        layout = GridBagLayout()
        // Output box
        outputBox.font = MONOSPACED_FONT
        outputBox.isEditable = false
        outputBox.alignmentY = BOTTOM_ALIGNMENT
        val scrollPane = JScrollPane(outputBox, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED)
        add(scrollPane, getConstraints(1, 2, gridwidth = 2, fill = GridBagConstraints.BOTH, insets = getInsets(left = 5, right = 5)))
        // Start button
        startBtn.addActionListener {
            if (consoleWrapper == null) {
                updateStartBtn(true)
                GlobalScope.launch {
                    consoleWrapper = ConsoleWrapper(server!!.location, server!!.getStartCommand()) {
                        outputBox.append("$it\n")
                        scrollPane.verticalScrollBar.value = Int.MAX_VALUE
                    }
                    consoleWrapper!!.start()
                    consoleWrapper = null
                    updateStartBtn()
                }
            } else consoleWrapper!!.sendCommand("stop")
        }
        updateStartBtn()
        add(startBtn, getConstraints(2, 1, insets = getInsets(right = 5, top = 5, bottom = 5)))
        // Input box
        val textField = JTextField()
        textField.addKeyListener(object : KeyAdapter() {
            override fun keyReleased(e: KeyEvent?) {
                if (e?.keyCode == KeyEvent.VK_ENTER) {
                    if (consoleWrapper != null) {
                        outputBox.append(textField.text + "\n")
                        consoleWrapper!!.sendCommand(textField.text)
                    }
                    textField.text = ""
                }
            }
        })
        textField.font = MONOSPACED_FONT
        add(textField, getConstraints(1, 1, weightx = 1.0, insets = getInsets(5, 5, 5, 5)))
    }

    fun setServer(server: Server) {
        this.server = server
    }

    private fun updateStartBtn(running: Boolean = consoleWrapper != null) {
        startBtn.text = if (running) "Stop" else "Start"
        startBtn.background = if (running) Color.RED else Color.GREEN
    }
}