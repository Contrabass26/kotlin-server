import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import java.io.File
import java.io.IOException
import javax.swing.DefaultListModel
import javax.swing.JButton
import javax.swing.JLabel
import javax.swing.SwingUtilities

val SERVERS = DefaultListModel<Server>()

// Utility to add items to DefaultListModel
fun DefaultListModel<Server>.insertSorted(item: Server) {
    SwingUtilities.invokeLater {
        var toInsert = size
        for (i in 0..<size) {
            if (item < getElementAt(i)) {
                toInsert = i
            }
        }
        add(toInsert, item)
    }
}

fun loadServers() {
    SERVERS.clear()
    val path = "$APP_DATA_LOCATION/minecraft-wrapper-kt/servers.json"
    try {
        val servers: ArrayNode = JSON_MAPPER.readTree(File(path)) as ArrayNode
        for (serverData in servers) {
            SERVERS.insertSorted(Server(serverData))
        }
    } catch (e: IOException) {
        println("Failed to load servers")
    }
}

class Server(val name: String, val location: File, val mcVersion: String, val modLoader: ModLoader, val mbMemory: Int, val javaVersion: String, val lastOpened: Long) : Comparable<Server> {

    data class StartScreenComponents(val label: JLabel, val openBtn: JButton)

    var startScreenComponents: StartScreenComponents? = null
    val majorMcVersion by lazy { getMajorVersion(mcVersion) }

    constructor(data: JsonNode) : this(
        data.get("name").textValue(),
        File(data.get("location").textValue()),
        data.get("mcVersion").textValue(),
        ModLoader.valueOf(data.get("modLoader").textValue()),
        data.get("mbMemory").intValue(),
        data.get("javaVersion").textValue(),
        data.get("lastOpened").longValue()
    )

    fun getStartCommand(): String {
        return modLoader.getStartCommand(this)
    }

    fun delete() {
        // Remove from start screen
        if (startScreenComponents != null) {
            START_SCREEN!!.remove(startScreenComponents!!.label)
            START_SCREEN!!.remove(startScreenComponents!!.openBtn)
        }
    }

    fun relativeFile(path: String): File {
        return File("${location.absolutePath}/$path")
    }

    suspend fun downloadFiles() {
        modLoader.downloadFiles(this)
    }

    override fun compareTo(other: Server): Int {
        return other.lastOpened.compareTo(lastOpened)
    }

    override fun toString(): String {
        return name
    }
}