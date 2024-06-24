import com.fasterxml.jackson.databind.ObjectMapper
import com.formdev.flatlaf.FlatDarculaLaf
import com.formdev.flatlaf.FlatLightLaf
import com.jthemedetecor.OsThemeDetector
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.awt.*
import javax.swing.JFrame
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener
import javax.swing.text.Document

// Screen size
val SCREEN_SIZE = Toolkit.getDefaultToolkit().screenSize!!

// Font - Windows default and monospace
val FONT = Font("Segoe UI", Font.PLAIN, 12)
val MONOSPACED_FONT = Font("Monospaced", Font.PLAIN, 12)

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
    return GridBagConstraints(gridx, gridy, gridwidth, gridheight, weightx1, weighty1, anchor, fill1, insets, ipadx, ipady)
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

// JSON object mapper
val JSON_MAPPER = ObjectMapper()

// Start screen instance
var START_SCREEN: StartScreen? = null
var MAIN_SCREEN: MainScreen? = null

fun main(): Unit = runBlocking {
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
    // Load servers
    launch { loadServers() }
}