import org.apache.commons.lang3.StringUtils
import org.jglrxavpok.hephaistos.mca.RegionFile
import java.awt.image.BufferedImage
import javax.swing.JOptionPane
import javax.swing.JTextArea

class Chunk(
    private val x: Int,
    private val y: Int,
    private val region: RegionFile,
    private val server: Server
) {

    val image: BufferedImage = BufferedImage(256, 256, BufferedImage.TYPE_INT_RGB)

    companion object {
        private val modelToTextureCache = mutableMapOf<String, String>()
        private val textureToImageCache = mutableMapOf<String, BufferedImage>()

        suspend fun getTextureUrl(modelUrl: String): String? {
            modelToTextureCache[modelUrl]?.let { return it }
            val prettyJson = getJson(modelUrl).toPrettyString()
            val textArea = JTextArea(prettyJson)
            textArea.isEditable = false
            textArea.font = MONOSPACED_FONT
            val result = JOptionPane.showConfirmDialog(MAIN_SCREEN, "Select texture URL:", "Loading textures", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE)
            val textureUrl = textArea.selectedText
            if (result != JOptionPane.OK_OPTION) return null
            modelToTextureCache[modelUrl] = textureUrl
            return textureUrl
        }
    }

    suspend fun init() {
        val chunk = region.getChunk(x, y) ?: return
        val g = image.graphics
        val heightMap = chunk.motionBlockingHeightMap
        for (x in 0..<16) {
            for (z in 0..<16) {
                val y = heightMap[x, z] - 65
                if (y < -64) continue
                val blockState = chunk.getBlockState(x, y, z)
                // Try to do it properly
                val blockStatesUrl = "https://raw.githubusercontent.com/misode/mcmeta/${server.mcVersion}-assets/assets/minecraft/blockstates/${removeNamespace(blockState.name)}.json"
                val blockStatesJson = getJson(blockStatesUrl)
                val blockStatesKey = StringUtils.join(blockState.properties.map { "${it.key}=${it.value}" }, ',')
                val model = blockStatesJson["variants"][blockStatesKey]["model"].textValue()
                val modelUrl = "https://raw.githubusercontent.com/misode/mcmeta/${server.mcVersion}-assets/assets/minecraft/models/block/${model.substring(16)}.json"
                val textureUrl =
            }
        }
    }
}