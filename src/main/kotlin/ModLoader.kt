import com.fasterxml.jackson.databind.node.ArrayNode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.commons.lang3.StringUtils
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter

private suspend fun writeEula(serverRoot: File) = withContext(Dispatchers.IO) {
    BufferedWriter(FileWriter(serverRoot.absolutePath + "/eula.txt")).use {
        it.write("eula=true")
    }
}

enum class ModLoader {

    VANILLA {
        override suspend fun downloadFiles(server: Server) {
            super.downloadFiles(server)
            val url = getUrl("https://piston-meta.mojang.com/mc/game/version_manifest_v2.json")
            val data: ArrayNode = getJson(url).get("versions") as ArrayNode
            data.forEach {
                if (server.mcVersion == it.get("id").textValue()) {
                    val version = getJson(getUrl(it.get("url").textValue()))
                    val fileUrl = getUrl(version.get("downloads", "server", "url").textValue())
                    downloadFile(fileUrl, File(server.location.absolutePath + "/server.jar"))
                }
            }
        }

        override fun getStartCommand(server: Server): String =
            "${server.javaVersion} -Xmx${server.mbMemory}M -jar server.jar nogui"

        override fun supportsVersion(mcVersion: String): Boolean {
            return super.supportsVersion(mcVersion)
        }
    },
    FABRIC,
    FORGE,
    NEOFORGE,
    PUFFERFISH;

    open suspend fun downloadFiles(server: Server) = writeEula(server.location)

    open fun getCfModLoaderType(): Int = -1

    open fun getStartCommand(server: Server): String = TODO("Not implemented yet")

    open fun supportsVersion(mcVersion: String): Boolean = false

    override fun toString(): String = StringUtils.capitalize(super.toString().lowercase())
}