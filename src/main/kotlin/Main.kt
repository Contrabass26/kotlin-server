import com.formdev.flatlaf.FlatDarculaLaf
import com.formdev.flatlaf.FlatLightLaf
import com.jthemedetecor.OsThemeDetector
import java.awt.Font
import java.awt.GridBagConstraints
import java.awt.Insets
import java.awt.Toolkit
import javax.swing.JFrame

// Screen size
val SCREEN_SIZE = Toolkit.getDefaultToolkit().screenSize!!

// Font - Windows default
val FONT = Font("Segoe UI", Font.PLAIN, 12)

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
    weightx: Double = 0.0,
    weighty: Double = 0.0,
    anchor: Int = GridBagConstraints.NORTH,
    insets: Insets = Insets(0, 0, 0, 0),
    ipadx: Int = 0,
    ipady: Int = 0
): GridBagConstraints {
    var fill = GridBagConstraints.NONE
    if (weightx > 0 && weighty > 0) {
        fill = GridBagConstraints.BOTH
    } else if (weightx > 0) {
        fill = GridBagConstraints.HORIZONTAL
    } else if (weighty > 0) {
        fill = GridBagConstraints.VERTICAL
    }
    return GridBagConstraints(gridx, gridy, gridwidth, gridheight, weightx, weighty, anchor, fill, insets, ipadx, ipady)
}

// Utility to create Insets
fun getInsets(top: Int = 0, left: Int = 0, bottom: Int = 0, right: Int = 0): Insets {
    return Insets(top, left, bottom, right)
}

fun main() {
    // Initialise GUI appearance
    val themeDetector = OsThemeDetector.getDetector()
    if (themeDetector.isDark) {
        FlatDarculaLaf.setup()
    } else {
        FlatLightLaf.setup()
    }
    // Start screen
    val startScreen = StartScreen()
}