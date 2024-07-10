import org.apache.logging.log4j.LogManager
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.io.File
import java.nio.file.*
import java.util.concurrent.ConcurrentHashMap
import javax.swing.JPanel
import javax.swing.JTree
import javax.swing.SwingUtilities
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.TreeSelectionModel
import kotlin.concurrent.thread
import kotlin.io.path.pathString
import kotlin.io.path.relativeTo

private val LOGGER = LogManager.getLogger("FileExplorerPanel")

class FileExplorerPanel(private val tabbedPane: MainTabbedPane) : JPanel() {

    private val tree = JTree()
    private var server: Server? = null
    private var watchService: WatchService? = null
    private val watchKeyPaths = ConcurrentHashMap<WatchKey, Path>()

    init {
        layout = GridBagLayout()
        tree.selectionModel.selectionMode = TreeSelectionModel.SINGLE_TREE_SELECTION
        tree.addTreeSelectionListener {
            if (tree.selectionCount > 0) {
                val path = tree.getPathForRow(tree.selectionRows!![0]).path
                val relativePath = path.asSequence()
                    .map { it as DefaultMutableTreeNode }
                    .drop(1) // Get rid of root
                    .joinToString(separator = File.separator) { it.userObject as String }
                tabbedPane.openFile(relativePath, server!!)
            }
        }
        add(tree, getConstraints(1, 1, fill = GridBagConstraints.BOTH, ipadx = 10, ipady = 10))
        background = tree.background
    }

    private fun register(watchService: WatchService, file: File) {
        val path = file.toPath()
        val key = path.register(watchService,
            StandardWatchEventKinds.ENTRY_CREATE,
            StandardWatchEventKinds.ENTRY_DELETE,
            StandardWatchEventKinds.ENTRY_MODIFY)
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
        thread {
            dispose()
            watchService = FileSystems.getDefault().newWatchService()
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
                        val model = tree.model as FileTreeModel
                        when(it.kind()) {
                            StandardWatchEventKinds.ENTRY_CREATE -> {
                                SwingUtilities.invokeLater { model.addFile(relativePath) }
                                val file = fullPath.toFile()
                                if (file.isDirectory) register(watchService!!, file)
                            }

                            StandardWatchEventKinds.ENTRY_DELETE -> {
                                SwingUtilities.invokeLater {
                                    model.deleteFile(relativePath)
                                    tabbedPane.closeFile(relativePath)
                                }
                            }

                            StandardWatchEventKinds.ENTRY_MODIFY -> {
                                tabbedPane.updateFile(relativePath)
                            }
                        }
                        key.reset()
                    }
                } catch (e: ClosedWatchServiceException) {
                    break
                }
            }
            LOGGER.info("Closed file watch service")
        }
    }

    fun dispose() {
        watchService?.close()
        watchKeyPaths.clear()
    }
}