import com.fasterxml.jackson.databind.ObjectMapper
import com.formdev.flatlaf.FlatDarculaLaf
import com.formdev.flatlaf.FlatLightLaf
import com.jthemedetecor.OsThemeDetector
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.awt.*

// Screen size
val SCREEN_SIZE = Toolkit.getDefaultToolkit().screenSize!!

// Font - Windows default and monospace
val FONT = Font("Segoe UI", Font.PLAIN, 12)
val MONOSPACED_FONT = Font("Monospaced", Font.PLAIN, 12)

// JSON object mapper
val JSON_MAPPER = ObjectMapper()

var START_SCREEN: StartScreen? = null
var MAIN_SCREEN: MainScreen? = null
var statusUpdateJob: Job? = null
var STATUS_PANEL: StatusPanel? = null

class Main {
    companion object {
        @JvmStatic
        fun main(args: Array<String>): Unit = runBlocking {
            launch { ModLoader.init() }
            // Initialise GUI appearance
            val themeDetector = OsThemeDetector.getDetector()
            if (themeDetector.isDark) {
                FlatDarculaLaf.setup()
            } else {
                FlatLightLaf.setup()
            }
            // Start screen
            START_SCREEN = StartScreen()
            MAIN_SCREEN = MainScreen()
            STATUS_PANEL = MAIN_SCREEN!!.statusPanel
            // Load servers
            launch { loadServers() }
            statusUpdateJob = launch {
                while (true) {
                    MAIN_SCREEN!!.statusPanel.updateJobs()
                    delay(50)
                }
            }
        }
    }
}