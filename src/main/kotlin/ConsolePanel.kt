import java.awt.Color
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import java.io.BufferedReader
import java.io.BufferedWriter
import javax.swing.*
import kotlin.concurrent.thread

class ConsolePanel : JPanel() {

    private var process: Process? = null
    private var writer: BufferedWriter? = null
    private var reader: BufferedReader? = null
    private var server: Server? = null
    private val outputBox = JTextArea()
    private val startBtn: JButton

    init {
        layout = GridBagLayout()
        // Start button
        startBtn = JButton("Start")
        startBtn.addActionListener {
            if (process == null) run() else sendToProcess("stop")
        }
        startBtn.background = Color.GREEN
        add(startBtn, getConstraints(2, 1, insets = getInsets(right = 5, top = 5, bottom = 5)))
        // Output box
        outputBox.font = MONOSPACED_FONT
        val scrollPane = JScrollPane(outputBox, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED)
        add(scrollPane, getConstraints(1, 2, gridwidth = 2, fill = GridBagConstraints.BOTH, insets = getInsets(left = 5, bottom = 5, right = 5)))
        // Input box
        val textField = JTextField()
        textField.addKeyListener(object : KeyAdapter() {
            override fun keyReleased(e: KeyEvent?) {
                if (e?.keyCode == KeyEvent.VK_ENTER) {
                    sendToProcess(textField.text)
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

    private fun sendToProcess(command: String) {
        val withNewline = command + "\n"
        outputBox.append(withNewline)
        writer?.write(withNewline)
        writer?.flush()
    }

    private fun run() {
        outputBox.text = ""
        process = ProcessBuilder("java", "-jar", "server.jar", "nogui").directory(server!!.location).start()
        startBtn.background = Color.RED
        startBtn.text = "Stop"
        reader = process!!.inputReader()
        writer = process!!.outputWriter()
        thread {
            reader.use { reader ->
                reader!!.lines().forEach {
                    outputBox.append(it + "\n")
                    outputBox.selectionStart = outputBox.text.length
                }
            }
            // Clean up all the process stuff
            process!!.destroyForcibly()
            process!!.inputStream.close()
            process!!.outputStream.close()
            process!!.errorStream.close()
            writer?.close()
            reader?.close()
            writer = null
            reader = null
            process = null
            SwingUtilities.invokeLater {
                startBtn.background = Color.GREEN
                startBtn.text = "Start"
            }
        }
    }
}