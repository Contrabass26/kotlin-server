import org.apache.logging.log4j.LogManager
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.io.File
import java.nio.file.*
import java.util.concurrent.ConcurrentHashMap
import javax.swing.JPanel
import javax.swing.JTabbedPane
import javax.swing.JTree
import javax.swing.SwingUtilities
import kotlin.concurrent.thread
import kotlin.io.path.pathString
import kotlin.io.path.relativeTo

private val LOGGER = LogManager.getLogger("FileExplorerPanel")

class FileExplorerPanel(private val tabbedPane: JTabbedPane) : JPanel(), Disposable {

    private val tree = JTree()
    private var lastClick: Long? = null
    private var server: Server? = null
    private var watchService: WatchService? = null
    private val watchKeyPaths = ConcurrentHashMap<WatchKey, Path>()

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
        val path = file.toPath()
        val key = path.register(watchService, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE)
        watchKeyPaths[key] = path
        file.listFiles()?.forEach {
            if (it.isDirectory) {
                register(watchService, it)
            }
        }
    }

    fun setServer(server: Server) {
        this.server = server
        tree.model = FileTreeModel(server.location)
        // Start listening for file updates
        dispose()
        thread {
            watchService = FileSystems.getDefault().newWatchService()
            watchKeyPaths.clear()
            register(watchService!!, server.location)
            var key: WatchKey?
            while (true) {
                try {
                    key = watchService!!.take()
                    if (key == null) break
                    key.pollEvents().forEach {
                        val parent = watchKeyPaths[key]!!
                        val fullPath = parent.resolve((it.context() as Path))
                        val relativePath = fullPath.relativeTo(server.location.toPath()).pathString
                        println("${it.kind()} $relativePath")
                        val model = tree.model as FileTreeModel
                        when(it.kind()) {
                            StandardWatchEventKinds.ENTRY_CREATE -> {
                                SwingUtilities.invokeLater { model.addFile(relativePath) }
                                val file = fullPath.toFile()
                                if (file.isDirectory) register(watchService!!, file)
                                key.reset()
                            }

                            StandardWatchEventKinds.ENTRY_DELETE -> {
                                SwingUtilities.invokeLater { model.deleteFile(relativePath) }
                                watchKeyPaths.remove(key)
                            }
                        }
                    }
                } catch (e: ClosedWatchServiceException) {
                    break
                }
            }
            LOGGER.info("Closed file watch service")
        }
    }

    override fun dispose() {
        watchService?.close()
    }
}