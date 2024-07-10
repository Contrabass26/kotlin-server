import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import org.apache.commons.lang3.StringUtils
import org.apache.logging.log4j.LogManager
import org.jglrxavpok.hephaistos.mca.RegionFile
import java.awt.image.BufferedImage
import javax.imageio.ImageIO
import javax.swing.JOptionPane
import javax.swing.JTextArea

private val LOGGER = LogManager.getLogger("Chunk")

class Chunk(
    private val x: Int,
    private val y: Int,
    private val region: RegionFile,
    private val server: Server
) {

    private var isInitialised = false
    val image: BufferedImage = BufferedImage(256, 256, BufferedImage.TYPE_INT_RGB)
        get() = if (isInitialised) field else BLANK_IMAGE

    companion object {
        private val BLANK_IMAGE = BufferedImage(256, 256, BufferedImage.TYPE_INT_RGB)

        private val modelToTextureCache = mutableMapOf<String, String>()
        private val textureToImageCache = mutableMapOf<String, BufferedImage>()
        private val blockStatesComplained = mutableListOf<String>()

        suspend fun getTextureUrl(modelUrl: String, server: Server): String? {
            modelToTextureCache[modelUrl]?.let { return it }
            val prettyJson = getJson(modelUrl).toPrettyString()
            val textArea = JTextArea(prettyJson)
            textArea.isEditable = false
            textArea.font = MONOSPACED_FONT
            val result = JOptionPane.showConfirmDialog(MAIN_SCREEN, textArea, "Select texture resource location", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE)
            if (result != JOptionPane.OK_OPTION) return null
            val path = textArea.selectedText.substring(16)
            val textureUrl = "https://raw.githubusercontent.com/misode/mcmeta/${server.mcVersion}-assets/assets/minecraft/textures/block/$path.png"
            modelToTextureCache[modelUrl] = textureUrl
            return textureUrl
        }

        suspend fun getTextureImage(textureUrl: String): BufferedImage {
            textureToImageCache[textureUrl]?.let { return it }
            val image = withContext(Dispatchers.IO) {
                async {
                    println("Actually reading texture image")
                    ImageIO.read(getUrl(textureUrl))
                }.await()
            }
            textureToImageCache[textureUrl] = image
            return image
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
                try {
                    val blockStatesUrl =
                        "https://raw.githubusercontent.com/misode/mcmeta/${server.mcVersion}-assets/assets/minecraft/blockstates/${
                            removeNamespace(blockState.name)
                        }.json"
                    val blockStatesJson = getJson(blockStatesUrl)
                    val blockStatesKey = StringUtils.join(blockState.properties.map { "${it.key}=${it.value}" }, ',')
                    val model = blockStatesJson["variants"][blockStatesKey]["model"].textValue()
                    val modelUrl =
                        "https://raw.githubusercontent.com/misode/mcmeta/${server.mcVersion}-assets/assets/minecraft/models/block/${
                            model.substring(16)
                        }.json"
                    val textureUrl = getTextureUrl(modelUrl, server) ?: continue
                    val image = getTextureImage(textureUrl)
                    g.drawImage(image, x * 16, z * 16, null)
                } catch (e: Exception) {
                    val key = "${blockState.name}${blockState.properties}"
                    if (!blockStatesComplained.contains(key)) {
                        blockStatesComplained.add(key)
                        LOGGER.error("Failed to load blockstate $key", e)
                    }
                }
            }
        }
    }
}