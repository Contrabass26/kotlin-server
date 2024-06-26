import com.fasterxml.jackson.databind.node.ArrayNode
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
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
        private val mcVersions: List<String> by ManualLazy {
            getVersionsJson().asSequence()
                .filter { it.get("type").textValue() == "release" }
                .map { it.get("id").textValue() }
                .toList()
        }

        override suspend fun downloadFiles(server: Server) {
            super.downloadFiles(server)
            val versions: ArrayNode = getVersionsJson()
            versions.forEach {
                if (server.mcVersion == it.get("id").textValue()) {
                    val version = getJson(getUrl(it.get("url").textValue()))
                    val fileUrl = getUrl(version.get("downloads", "server", "url").textValue())
                    downloadFile(fileUrl, File(server.location.absolutePath + "/server.jar"))
                }
            }
        }

        private suspend fun getVersionsJson(): ArrayNode {
            val url = getUrl("https://piston-meta.mojang.com/mc/game/version_manifest_v2.json")
            return getJson(url).get("versions") as ArrayNode
        }

        override fun getStartCommand(server: Server): String =
            "${server.javaVersion} -Xmx${server.mbMemory}M -jar server.jar nogui"

        override fun supportsVersion(mcVersion: String): Boolean = mcVersions.contains(mcVersion)

        override suspend fun init() {
            mcVersions.
        }
    },
    FABRIC {
        private val mcVersions: MutableList<String> = mutableListOf()

        override val cfModLoaderType = 4

        override suspend fun init() {
            val versions = getJson(getUrl("https://meta.fabricmc.net/v2/versions/game"))
            versions.forEach {
                if (it.get("stable").booleanValue()) {
                    mcVersions.add(it.get("version").textValue())
                }
            }
        }

        override fun getStartCommand(server: Server): String =
            "${server.javaVersion} -Xmx${server.mbMemory}M -jar fabric-server-launch.jar nogui"

        override fun supportsVersion(mcVersion: String): Boolean = mcVersions.contains(mcVersion)

        override suspend fun downloadFiles(server: Server) {
            TODO("Requires Fabric installer and loader versions")
        }
    },
    FORGE,
    NEOFORGE,
    PUFFERFISH;

    open val cfModLoaderType = -1

    companion object {
        suspend fun init() = entries.forEach { it.init() }
    }

    open suspend fun downloadFiles(server: Server) = writeEula(server.location)

    open fun getStartCommand(server: Server): String = TODO("Not implemented yet")

    open fun supportsVersion(mcVersion: String): Boolean = false

    // This will be started in a coroutine before anything else is loaded
    protected open suspend fun init() = Unit

    override fun toString(): String = StringUtils.capitalize(super.toString().lowercase())
}