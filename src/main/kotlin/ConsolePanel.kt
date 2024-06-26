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
        // Start button
        startBtn.addActionListener {
            if (consoleWrapper == null) {
                updateStartBtn(true)
                GlobalScope.launch {
                    consoleWrapper = ConsoleWrapper.create(server!!.location, server!!.getStartCommand()) {
                        outputBox.append("$it\n")
                        outputBox.selectionStart = outputBox.text.length
                    }
                    consoleWrapper = null
                    updateStartBtn()
                }
            } else consoleWrapper!!.sendCommand("stop")
        }
        updateStartBtn()
        add(startBtn, getConstraints(2, 1, insets = getInsets(right = 5, top = 5, bottom = 5)))
        // Output box
        outputBox.font = MONOSPACED_FONT
        val scrollPane = JScrollPane(outputBox, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED)
        add(scrollPane, getConstraints(1, 2, gridwidth = 2, fill = GridBagConstraints.BOTH, insets = getInsets(left = 5, right = 5)))
        // Input box
        val textField = JTextField()
        textField.addKeyListener(object : KeyAdapter() {
            override fun keyReleased(e: KeyEvent?) {
                if (e?.keyCode == KeyEvent.VK_ENTER) {
                    consoleWrapper?.sendCommand(textField.text)
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