import com.fasterxml.jackson.databind.JsonNode
import kotlinx.coroutines.*
import org.apache.commons.io.input.CountingInputStream
import org.apache.commons.io.output.CountingOutputStream
import org.apache.commons.lang3.StringUtils
import org.apache.commons.lang3.SystemUtils
import org.apache.logging.log4j.LogManager
import org.jsoup.Jsoup
import java.awt.Component
import java.awt.GridBagConstraints
import java.awt.Insets
import java.io.BufferedWriter
import java.io.File
import java.io.FileOutputStream
import java.lang.management.ManagementFactory
import java.net.HttpURLConnection
import java.net.URI
import java.net.URL
import javax.management.*
import javax.swing.JFrame
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener
import javax.swing.text.Document

private val LOGGER = LogManager.getLogger()
val USERNAME: String = System.getProperty("user.name")

// Get app data directory
val APP_DATA_LOCATION by lazy {
    String.format(when {
        SystemUtils.IS_OS_WINDOWS -> "C:/Users/%s/AppData/Roaming/"
        SystemUtils.IS_OS_MAC -> "/Users/%s/Library/Application Support/"
        SystemUtils.IS_OS_LINUX -> "/home/%s/."
        else -> throw IllegalStateException("Operating system not supported: " + System.getProperty("os.name"))
    }, USERNAME)
}

val USER_HOME by lazy {
    String.format(when {
        SystemUtils.IS_OS_WINDOWS -> "C:/Users/%s"
        SystemUtils.IS_OS_MAC -> "/Users/%s"
        SystemUtils.IS_OS_LINUX -> "/home/%s"
        else -> throw IllegalStateException("Operating system not supported: " + System.getProperty("os.name"))
    }, USERNAME)
}

val SYSTEM_MEMORY_BYTES: Long by lazy {
    val mBeanServer = ManagementFactory.getPlatformMBeanServer()
    try {
        mBeanServer.getAttribute(
            ObjectName("java.lang", "type", "OperatingSystem"),
            "TotalPhysicalMemorySize"
        ) as Long
    } catch (e: Throwable) {
        LOGGER.error("Failed to read system memory", e)
    }
    8589934592L
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
    gridx: Int = 1,
    gridy: Int = 1,
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

class ConsoleWrapper private constructor(location: File, vararg command: String) {

    private val process: Process = ProcessBuilder(*command).directory(location).start()
    private val writer: BufferedWriter = process.outputWriter();

    private fun stop() {
        process.destroyForcibly()
        process.inputStream.close()
        process.outputStream.close()
        process.errorStream.close()
        writer.close()
    }

    companion object {
        suspend fun create(location: File, vararg command: String, outputConsumer: (String) -> Unit) = withContext(Dispatchers.IO) {
            val consoleWrapper = ConsoleWrapper(location, *command)
            val reader = consoleWrapper.process.inputReader()
            launch {
                reader.use { reader ->
                    reader!!.lines().forEach { outputConsumer(it) }
                }
                reader.close()
                consoleWrapper.stop()
            }
            return@withContext consoleWrapper
        }

        suspend fun create(location: File, command: String, outputConsumer: (String) -> Unit) = withContext(Dispatchers.IO) {
            return@withContext create(location, *command.split(' ').toTypedArray()) { outputConsumer(it) }
        }
    }

    fun send(s: String) {
        writer.write(s)
        writer.flush()
    }
}

fun convertVersion(version: String): Sequence<Int> {
    val split = version.split("\\.".toRegex())
    return split
        .asSequence()
        .map { it.toInt() }
        .plus(generateSequence { 0 })
        .take(3)
}

fun getMajorVersion(mcVersion: String): String {
    return StringUtils.join(convertVersion(mcVersion).take(2), '.')
}

val MC_VERSION_COMPARATOR = Comparator<String> { v1, v2 ->
    val nums1 = convertVersion(v1)
    val nums2 = convertVersion(v2)
    nums1.zip(nums2)
        .map { (i1, i2) -> i1.compareTo(i2) }
        .find { it != 0 } ?: 0
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
            val job = STATUS_PANEL?.TrackedJob({ it.count / length }, { status })
            node = JSON_MAPPER.readTree(it)
            job?.complete()
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