import kotlinx.coroutines.cancel
import org.jglrxavpok.hephaistos.mca.ChunkColumn
import org.jglrxavpok.hephaistos.mca.RegionFile
import java.awt.Graphics
import java.awt.image.BufferedImage
import java.io.RandomAccessFile

class RegionTab(private val server: Server, private val relativePath: String) : ServerConfigTab() {

    private val mainScope = ServerConfigTabType.REGION.mainScope
    private val region: RegionFile

    companion object {
        private val BLANK_IMAGE = BufferedImage(256, 256, BufferedImage.TYPE_INT_RGB)
    }

    init {
        val regex = Regex("r\\.([-0-9]+)\\.([-0-9]+)\\.mca")
        val file = server.relativeFile(relativePath)
        val matchResult = regex.matchEntire(file.name)!!
        val x = matchResult.groups[1]!!.value.toInt()
        val y = matchResult.groups[2]!!.value.toInt()
        region = RegionFile(RandomAccessFile(file.absolutePath, "r"), x, y, 1, 1)
    }

    override fun paint(g: Graphics?) {
        if (g == null) throw IllegalArgumentException("Graphics is null")
    }

    private fun getChunkImage(x: Int, y: Int): BufferedImage {
        val chunk = region.getChunk(x, y) ?: return BLANK_IMAGE
        ch
    }

    override fun onCloseTab() {
        mainScope.cancel()
    }

    override fun onFileUpdate() {

    }
}