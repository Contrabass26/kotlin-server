import dev.vishna.watchservice.KWatchChannel
import dev.vishna.watchservice.KWatchEvent
import dev.vishna.watchservice.asWatchChannel
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.consumeEach
import java.awt.*
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.JPanel
import javax.swing.JTabbedPane
import javax.swing.JTree
import javax.swing.SwingUtilities

private const val UPDATE_INTERVAL = 5000L

class FileExplorerPanel(private val tabbedPane: JTabbedPane) : JPanel(), Disposable {

    private val tree = JTree()
    private var lastClick: Long? = null
    private var server: Server? = null
    private val mainScope = MainScope()
    private var watchChannel: KWatchChannel? = null
    private var updateJob: Job? = null

    init {
        START_SCREEN!!.registerDisposable(this)
        layout = GridBagLayout()
        tree.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent?) {

            }
        })
        add(tree, getConstraints(1, 1, fill = GridBagConstraints.BOTH, ipadx = 10, ipady = 10))
        background = tree.background
    }

    fun setServer(server: Server?) {
        this.server = server
        // Start listening for file updates
        updateJob?.cancel()
        watchChannel?.close()
        val watchChannel = server!!.location.asWatchChannel()
        updateJob = mainScope.launch {
            watchChannel.consumeEach {
                val relativePath = it.file.toRelativeString(server.location)
                when (it.kind) {
                    KWatchEvent.Kind.Created -> {
                        println("File created at $relativePath")
                    }
                    KWatchEvent.Kind.Deleted -> {
                        println("File deleted at $relativePath")
                    }
                    KWatchEvent.Kind.Initialized -> {
                        tree.model = FileTreeModel(server.location)
                    }
                    else -> {}
                }
            }
        }
    }

    override fun dispose() {
        watchChannel?.close()
        watchChannel = null
        mainScope.cancel()
    }
}