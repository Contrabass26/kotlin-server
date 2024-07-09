import com.fasterxml.jackson.databind.ObjectMapper
import com.formdev.flatlaf.FlatDarculaLaf
import com.formdev.flatlaf.FlatLightLaf
import com.jthemedetecor.OsThemeDetector
import kotlinx.coroutines.*
import org.apache.logging.log4j.LogManager
import java.awt.*

private val LOGGER = LogManager.getLogger()

// Screen size
val SCREEN_SIZE = Toolkit.getDefaultToolkit().screenSize!!

// Font - Windows default and monospace
val FONT = Font("Segoe UI", Font.PLAIN, 12)
val MONOSPACED_FONT = Font("Monospaced", Font.PLAIN, 12)

// JSON object mapper
val JSON_MAPPER = ObjectMapper()

var START_SCREEN: StartScreen? = null
var CREATE_SERVER_SCREEN: CreateServerScreen? = null
var MAIN_SCREEN: MainScreen? = null
var STATUS_PANEL: StatusPanel? = null
var statusUpdateJob: Job? = null
var modLoaderInitJob: Job? = null
var DARK_THEME = true

fun cancelJob(job: Job?, name: String = "job"): Job? {
    if (job != null) {
        job.cancel()
        LOGGER.info("Cancelled $name")
    }
    return null
}

class Main {
    companion object {
        @JvmStatic
        fun main(args: Array<String>): Unit = runBlocking {
            modLoaderInitJob = launch { ModLoader.init() }
            // Initialise GUI appearance
            val themeDetector = OsThemeDetector.getDetector()
            DARK_THEME = themeDetector.isDark
            LOGGER.info("Setting dark theme = $DARK_THEME")
            if (DARK_THEME) {
                FlatDarculaLaf.setup()
            } else {
                FlatLightLaf.setup()
            }
            // Create GUIs
            CREATE_SERVER_SCREEN = CreateServerScreen()
            START_SCREEN = StartScreen()
            MAIN_SCREEN = MainScreen()
            STATUS_PANEL = MAIN_SCREEN!!.statusPanel
            // Load servers
            launch { loadServers() }
            statusUpdateJob = launch {
                LOGGER.info("Started listening for status updates")
                while (true) {
                    MAIN_SCREEN!!.statusPanel.updateJobs()
                    delay(50)
                }
            }
        }
    }
}