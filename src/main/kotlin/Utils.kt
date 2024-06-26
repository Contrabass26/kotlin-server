import com.fasterxml.jackson.databind.JsonNode
import kotlinx.coroutines.*
import org.apache.commons.io.input.CountingInputStream
import org.apache.commons.io.output.CountingOutputStream
import org.apache.commons.lang3.SystemUtils
import org.jsoup.Jsoup
import java.awt.Component
import java.awt.GridBagConstraints
import java.awt.Insets
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URI
import java.net.URL
import javax.swing.JFrame
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener
import javax.swing.text.Document
import kotlin.reflect.KProperty

val USERNAME: String = System.getProperty("user.name")

class ManualLazy<T>(val getter: suspend () -> T) {

    private var value: T? = null

    operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
        if (value == null) {
            runBlocking { loadManual() }
        }
        return value!!
    }

    suspend fun loadManual() {
        val newValue = getter()
        synchronized(this) {
            println("Set manual lazy value")
            value = newValue
        }
    }
}

// Get app data directory
val APP_DATA_LOCATION by lazy {
    String.format(when {
        SystemUtils.IS_OS_WINDOWS -> "C:/Users/%s/AppData/Roaming/"
        SystemUtils.IS_OS_MAC -> "/Users/%s/Library/Application Support/"
        SystemUtils.IS_OS_LINUX -> "/home/%s/."
        else -> throw IllegalStateException("Operating system not supported: " + System.getProperty("os.name"))
    }, USERNAME)
}

// Utility extension to set frame size based on screen resolution
fun JFrame.setSize(widthProp: Double, heightProp: Double) {
    setSize(
        (SCREEN_SIZE.width * widthProp).toInt(),
        (SCREEN_SIZE.height * heightProp).toInt()
    )
}

// Utility to create GridBagConstraints
fun getConstraints(
    gridx: Int,
    gridy: Int,
    gridwidth: Int = 1,
    gridheight: Int = 1,
    weightx: Double? = null,
    weighty: Double? = null,
    anchor: Int = GridBagConstraints.NORTH,
    fill: Int? = null,
    insets: Insets = Insets(0, 0, 0, 0),
    ipadx: Int = 0,
    ipady: Int = 0
): GridBagConstraints {
    val weightx1: Double; val weighty1: Double; val fill1: Int
    if (fill != null) {
        weightx1 = weightx ?: if (fill == GridBagConstraints.HORIZONTAL || fill == GridBagConstraints.BOTH) 1.0 else 0.0;
        weighty1 = weighty ?: if (fill == GridBagConstraints.VERTICAL || fill == GridBagConstraints.BOTH) 1.0 else 0.0;
        fill1 = fill
    } else {
        weightx1 = weightx ?: 0.0
        weighty1 = weighty ?: 0.0
        fill1 = when {
            weightx1 > 0 && weighty1 > 0 -> GridBagConstraints.BOTH
            weightx1 > 0 -> GridBagConstraints.HORIZONTAL
            weighty1 > 0 -> GridBagConstraints.VERTICAL
            else -> GridBagConstraints.NONE
        }
    }
    return GridBagConstraints(
        gridx,
        gridy,
        gridwidth,
        gridheight,
        weightx1,
        weighty1,
        anchor,
        fill1,
        insets,
        ipadx,
        ipady
    )
}

// Utility to create Insets
fun getInsets(top: Int = 0, left: Int = 0, bottom: Int = 0, right: Int = 0): Insets {
    return Insets(top, left, bottom, right)
}

// Utility to repaint and revalidate
fun Component.refreshGui() {
    repaint()
    revalidate()
}

// Utility for easy document listeners
fun Document.addDocumentListener(consumer: (DocumentEvent?) -> Unit) {
    addDocumentListener(object : DocumentListener {
        override fun insertUpdate(e: DocumentEvent?) { consumer(e) }

        override fun removeUpdate(e: DocumentEvent?) { consumer(e) }

        override fun changedUpdate(e: DocumentEvent?) { consumer(e) }
    })
}

fun getUrl(path: String): URL {
    return URI.create(path).toURL()
}

// Chained get() calls on JsonNode
fun JsonNode.get(vararg path: String): JsonNode {
    var current = this
    path.forEach {
        current = current.get(it)
    }
    return current
}

fun String.looksLikeMcVersion(): Boolean {
    return matches("[0-9]+\\.[0-9]+(?:\\.[0-9]+)?".toRegex())
}

fun consoleWrapper(vararg command: String) {

}

// Download file
suspend fun downloadFile(url: URL, destination: File) = withContext(Dispatchers.IO) {
    val status = "Downloading $url"
    val connection: HttpURLConnection = url.openConnection() as HttpURLConnection
    connection.requestMethod = "GET"
    val length = connection.contentLength
    val inputStream = connection.inputStream
    CountingOutputStream(FileOutputStream(destination)).use {
        val job = STATUS_PANEL!!.TrackedJob({ it.count / length }, { status })
        inputStream.transferTo(it)
        job.complete()
    }
    inputStream.close()
}

suspend fun getJson(url: URL): JsonNode = withContext(Dispatchers.IO) {
    val result: Deferred<JsonNode?> = async {
        val status = "Downloading $url"
        val connection: HttpURLConnection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        val length = connection.contentLength
        val stream = connection.inputStream
        var node: JsonNode? = null
        CountingInputStream(stream).use {
            val job = STATUS_PANEL!!.TrackedJob({ it.count / length }, { status })
            node = JSON_MAPPER.readTree(it)
            job.complete()
        }
        node
    }
    result.await()!!
}

suspend fun getJsoup(url: String): org.jsoup.nodes.Document = withContext(Dispatchers.IO) {
    val result: Deferred<org.jsoup.nodes.Document> = async {
        Jsoup.connect(url).userAgent("Mozilla").get()
    }
    result.await()
}