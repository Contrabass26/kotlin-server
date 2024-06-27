import java.awt.GridBagLayout
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JProgressBar
import javax.swing.SwingUtilities

class StatusPanel : JPanel() {

    private val jobs: MutableList<TrackedJob> = mutableListOf()

    init {
        layout = GridBagLayout()
    }

    fun updateJobs() {
        SwingUtilities.invokeLater {
            synchronized(jobs) {
                jobs.forEach { it.update() }
            }
        }
    }

    inner class TrackedJob(val getProgress: () -> Int, val getStatus: () -> String) {

        private val label = JLabel()
        private val progressBar = JProgressBar()

        init {
            SwingUtilities.invokeLater {
                add(label, getConstraints(1, jobs.size * 2))
                add(progressBar, getConstraints(1, jobs.size * 2 + 1))
            }
            synchronized(jobs) { jobs.add(this) }
        }

        fun complete() {
            SwingUtilities.invokeLater {
                remove(label)
                remove(progressBar)
            }
            synchronized(jobs) { jobs.remove(this) }
        }

        fun update() {
            label.text = getStatus()
            progressBar.value = getProgress()
        }
    }
}