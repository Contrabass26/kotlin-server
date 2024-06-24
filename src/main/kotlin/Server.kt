import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import kotlinx.coroutines.delay
import java.io.File
import java.io.IOException
import javax.swing.DefaultListModel
import javax.swing.JButton
import javax.swing.JLabel
import javax.swing.SwingUtilities

val SERVERS = DefaultListModel<Server>()

// Utility to add items to DefaultListModel
private fun DefaultListModel<Server>.insertSorted(item: Server) {
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

class Server(val name: String, val location: File, val lastOpened: Int) : Comparable<Server> {

    data class StartScreenComponents(val label: JLabel, val openBtn: JButton)

    var startScreenComponents: StartScreenComponents? = null

    constructor(data: JsonNode) : this(
        data.get("name").textValue(),
        File(data.get("location").textValue()),
        data.get("lastOpened").intValue()
    )

    fun delete() {
        // Remove from start screen
        if (startScreenComponents != null) {
            START_SCREEN!!.remove(startScreenComponents!!.label)
            START_SCREEN!!.remove(startScreenComponents!!.openBtn)
        }
    }

    override fun compareTo(other: Server): Int {
        return other.lastOpened.compareTo(lastOpened)
    }

    override fun toString(): String {
        return name
    }
}