import org.apache.logging.log4j.LogManager
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.io.File
import java.nio.file.ClosedWatchServiceException
import java.nio.file.FileSystems
import java.nio.file.StandardWatchEventKinds
import java.nio.file.WatchEvent
import java.nio.file.WatchKey
import java.nio.file.WatchService
import javax.swing.JPanel
import javax.swing.JTabbedPane
import javax.swing.JTree
import kotlin.concurrent.thread

private val LOGGER = LogManager.getLogger("FileExplorerPanel")

class FileExplorerPanel(private val tabbedPane: JTabbedPane) : JPanel(), Disposable {

    private val tree = JTree()
    private var lastClick: Long? = null
    private var server: Server? = null
    private var watchService: WatchService? = null

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

    private fun register(watchService: WatchService, file: File) {

    }

    fun setServer(server: Server) {
        this.server = server
        tree.model = FileTreeModel(server.location)
        // Start listening for file updates
        dispose()
        thread {
            watchService = FileSystems.getDefault().newWatchService()
            val path = server.location.toPath()
            path.register(watchService!!,
                StandardWatchEventKinds.ENTRY_CREATE,
                StandardWatchEventKinds.ENTRY_DELETE)
            var key: WatchKey?
            while (true) {
                try {
                    key = watchService!!.take()
                    if (key == null) break
                    key.pollEvents().forEach {
                        val relativePath = it.context()
                        println("${it.kind()} $relativePath")
                    }
                    key.reset()
                } catch (e: ClosedWatchServiceException) {
                    LOGGER.info("Closed file watch service")
                }
            }
//            consumeEach {
//                val relativePath = it.file.toRelativeString(server.location)
//                when (it.kind) {
//                    KWatchEvent.Kind.Created -> {
//                        println("Adding file")
//                        (tree.model as FileTreeModel).addFile(relativePath)
//                    }
//
//                    KWatchEvent.Kind.Deleted -> {
//                        println("File deleted at $relativePath")
//                    }
//
//                    KWatchEvent.Kind.Initialized -> {
//
//                    }
//
//                    else -> {}
//                }
//            }
        }
    }

    override fun dispose() {
        watchService?.close()
    }
}