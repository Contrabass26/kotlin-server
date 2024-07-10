import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.jglrxavpok.hephaistos.mca.RegionFile
import java.awt.Graphics
import java.io.RandomAccessFile
import javax.swing.SwingUtilities

class RegionTab(private val server: Server, private val relativePath: String) : ServerConfigTab() {

    private val mainScope = ServerConfigTabType.REGION.mainScope
    private val region: RegionFile
    private val chunk: Chunk

    init {
        val regex = Regex("r\\.([-0-9]+)\\.([-0-9]+)\\.mca")
        val file = server.relativeFile(relativePath)
        val matchResult = regex.matchEntire(file.name)!!
        val x = matchResult.groups[1]!!.value.toInt()
        val y = matchResult.groups[2]!!.value.toInt()
        region = RegionFile(RandomAccessFile(file.absolutePath, "r"), x, y, 1, 1)
        // Test image
        chunk = Chunk(0, 0, region, server)
        mainScope.launch {
            chunk.init()
            println("DONE")
            SwingUtilities.invokeLater { repaint() }
        }
    }

    override fun paint(g: Graphics?) {
        if (g == null) throw IllegalArgumentException("Graphics is null")
        g.drawImage(chunk.image, 0, 0, null)
    }

    override fun onCloseTab() {
        mainScope.cancel()
    }

    override fun onFileUpdate() {

    }
}