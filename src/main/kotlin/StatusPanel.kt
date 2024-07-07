import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JProgressBar
import javax.swing.SwingUtilities

private const val PROGRESS_BAR_RESOLUTION = 1000

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

    inner class TrackedJob(val getProgress: () -> Double, val getStatus: () -> String) {

        private val label = JLabel()
        private val progressBar = JProgressBar()

        init {
            SwingUtilities.invokeLater {
                progressBar.maximum = PROGRESS_BAR_RESOLUTION
                add(label, getConstraints(gridy = jobs.size * 2, weightx = 1.0, anchor = GridBagConstraints.WEST, insets = getInsets(5, 5, 5, 5)))
                add(progressBar, getConstraints(gridy = jobs.size * 2 + 1, weightx = 1.0))
            }
            synchronized(jobs) { jobs.add(this) }
        }

        fun complete() {
            SwingUtilities.invokeLater {
                remove(label)
                remove(progressBar)
                refreshGui()
            }
            synchronized(jobs) { jobs.remove(this) }
        }

        fun update() {
            label.text = getStatus()
            progressBar.value = (getProgress() * PROGRESS_BAR_RESOLUTION).toInt()
        }
    }
}