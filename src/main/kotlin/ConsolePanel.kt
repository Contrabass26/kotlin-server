import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.lang.ProcessBuilder.Redirect
import javax.swing.JButton
import javax.swing.JPanel
import javax.swing.JTextArea
import javax.swing.SwingUtilities
import kotlin.concurrent.thread

class ConsolePanel : JPanel() {

    private var process: Process? = null
    private var server: Server? = null
    private val outputBox = JTextArea()

    init {
        layout = GridBagLayout()
        val startBtn = JButton("Start")
        startBtn.addActionListener { run() }
        add(startBtn, getConstraints(1, 1, weightx = 1.0))
        add(outputBox, getConstraints(1, 2, fill = GridBagConstraints.BOTH))
    }

    fun setServer(server: Server) {
        this.server = server
    }

    fun run() {
        if (process != null) {
            println("Sending stop command")
            process!!.outputWriter().write("stop\n")
        } else {
            outputBox.text = ""
            thread {
                process = ProcessBuilder("java", "-jar", "server.jar", "nogui").directory(server!!.location).start()
                process!!.inputReader().use { reader ->
                    reader.lines().forEach { SwingUtilities.invokeLater { outputBox.append(it + "\n") } }
                }
                println("No more lines to output")
                process!!.waitFor()
                process!!.destroy()
                println("Server process thread finished")
            }
            println("Launched ALL the threads")
        }
    }
}