import java.awt.GridBagLayout
import javax.swing.JPanel

abstract class ValidatedStage : JPanel() {

    init {
        layout = GridBagLayout()
        initGui()
    }

    protected abstract fun initGui()

    protected abstract fun isStageValid(): Boolean
}