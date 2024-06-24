import java.awt.GridBagLayout
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JProgressBar

class StatusPanel : JPanel() {

    private val jobs: MutableList<TrackedJob> = mutableListOf()

    init {
        layout = GridBagLayout()
    }

    fun updateJobs() {
        jobs.forEach { it.update() }
    }

    inner class TrackedJob(val getProgress: () -> Int, val getStatus: () -> String) {

        private val label = JLabel()
        private val progressBar = JProgressBar()

        init {
            add(label, getConstraints(1, jobs.size * 2))
            add(progressBar, getConstraints(1, jobs.size * 2 + 1))
            jobs.add(this)
        }

        fun complete() {
            remove(label)
            remove(progressBar)
            jobs.remove(this)
        }

        fun update() {
            label.text = getStatus()
            progressBar.value = getProgress()
        }
    }
}