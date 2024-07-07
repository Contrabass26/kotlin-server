import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.fasterxml.jackson.databind.node.ArrayNode
import org.apache.logging.log4j.LogManager
import java.io.File
import java.io.IOException
import javax.swing.*

private val LOGGER = LogManager.getLogger("Server")

val SERVERS = DefaultListModel<Server>()

val DATA_PATH = "$APP_DATA_LOCATION/minecraft-wrapper-kt"
val SERVERS_PATH = "$DATA_PATH/servers.json"

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

fun <T> ListModel<T>.asSequence(): Sequence<T> = (0..<size)
    .asSequence()
    .map { getElementAt(it) }

fun loadServers() {
    SERVERS.clear()
    try {
        val servers: ArrayNode = JSON_MAPPER.readTree(File(SERVERS_PATH)) as ArrayNode
        for (serverData in servers) {
            SERVERS.insertSorted(Server(serverData))
        }
    } catch (e: IOException) {
        LOGGER.warn("Failed to load servers", e)
    }
}

fun saveServers() {
    try {
        File(DATA_PATH).mkdirs()
        JSON_MAPPER.writeValue(File(SERVERS_PATH), SERVERS.asSequence().toList())
    } catch (e: IOException) {
        LOGGER.error("Failed to save servers", e)
    }
}

@JsonSerialize(using = ServerSerializer::class)
class Server(
    val name: String,
    val location: File,
    val mcVersion: String,
    val modLoader: ModLoader,
    val mbMemory: Int,
    val javaVersion: String,
    var lastOpened: Long
) : Comparable<Server> {

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