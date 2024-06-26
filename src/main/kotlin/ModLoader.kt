import com.fasterxml.jackson.databind.node.ArrayNode
import kotlinx.coroutines.*
import org.apache.commons.lang3.StringUtils
import org.apache.commons.lang3.SystemUtils
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference
import javax.xml.XMLConstants
import javax.xml.parsers.DocumentBuilderFactory

private suspend fun writeEula(serverRoot: File) = withContext(Dispatchers.IO) {
    BufferedWriter(FileWriter(serverRoot.absolutePath + "/eula.txt")).use {
        it.write("eula=true")
    }
}

enum class ModLoader {

    VANILLA {
        private lateinit var mcVersions: Deferred<List<String>>

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

        override suspend fun supportsVersion(mcVersion: String): Boolean = mcVersions.await().contains(mcVersion)

        override suspend fun init() {
            coroutineScope {
                mcVersions = async {
                    getVersionsJson().asSequence()
                        .filter { it.get("type").textValue() == "release" }
                        .map { it.get("id").textValue() }
                        .toList()
                }
            }
        }
    },
    FABRIC {
        private lateinit var mcVersions: Deferred<List<String>>
        private var loaderVersion: String? = null
        private var installerVersion: String? = null
        override val cfModLoaderType = 4

        override suspend fun init() {
            coroutineScope {
                // Supported versions
                mcVersions = async {
                    getJson(getUrl("https://meta.fabricmc.net/v2/versions/game"))
                        .asSequence()
                        .filter { it.get("stable").booleanValue() }
                        .map { it.get("version").textValue() }
                        .toList()
                }
                // Loader
                loaderVersion = getJson(getUrl("https://meta.fabricmc.net/v2/versions/loader"))
                    .find { it.get("stable").booleanValue() }!!
                    .textValue()
                // Installer
                installerVersion = getJson(getUrl("https://meta.fabricmc.net/v2/versions/installer"))
                    .find { it.get("stable").booleanValue() }!!
                    .textValue()
            }
        }

        override fun getStartCommand(server: Server): String =
            "${server.javaVersion} -Xmx${server.mbMemory}M -jar fabric-server-launch.jar nogui"

        override suspend fun supportsVersion(mcVersion: String): Boolean = mcVersions.await().contains(mcVersion)

        override suspend fun downloadFiles(server: Server) {
            downloadFile(getUrl("https://meta.fabricmc.net/v2/versions/loader/${server.mcVersion}/$loaderVersion/$installerVersion/server/jar"), server.relativeFile("/fabric-server-launch.jar"))
        }
    },
    FORGE {
        private lateinit var mcVersions: Deferred<List<String>>
        override val cfModLoaderType = 1


        override suspend fun init() {
            coroutineScope {
                mcVersions = async {
                    getJsoup("https://files.minecraftforge.net/net/minecraftforge/forge").select("a")
                        .asSequence()
                        .map { it.text() }
                        .filter { it.looksLikeMcVersion() }
                        .toList()
                }
            }
        }

        override fun getStartCommand(server: Server): String {
            val os = if (SystemUtils.IS_OS_WINDOWS) "win" else "unix"
            return server.location.listFiles()!!
                .asSequence()
                .map { it.name }
                .filter { it.contains(server.mcVersion) }
                .find { it.matches("minecraft_server.*\\.jar".toRegex()) }
                ?.let { "${server.javaVersion} -Xmx${server.mbMemory}M -jar $it nogui" }
                ?: server.relativeFile("/libraries/net/minecraftforge/forge").listFiles()!!
                    .map { it.name }
                    .find { it.contains(server.mcVersion) }!!
                    .let { "${server.javaVersion} -Xmx${server.mbMemory}M @libraries/net/minecraftforge/forge/$it/${os}_args.txt nogui %*" }
        }

        override suspend fun supportsVersion(mcVersion: String): Boolean = mcVersions.await().contains(mcVersion)

        override suspend fun downloadFiles(server: Server) {
            val trackedUrl = getJsoup("https://files.minecraftforge.net/net/minecraftforge/forge/index_${server.mcVersion}.html")
                .select("div.link.link-boosted")
                .first()!!
                .child(0)
                .attr("href")
            val url = Regex("url=(https://maven\\.minecraftforge\\.net/.*)")
                .matchEntire(trackedUrl)!!
                .groups[1]!!
                .value
            downloadFile(getUrl(url), server.relativeFile("/installer.jar"))
            // Run installer
            val lineCount = AtomicInteger()
            val lastOutput = AtomicReference("Running Forge installer")
            val job = STATUS_PANEL!!.TrackedJob({ lineCount.get() / 22507 }, { lastOutput.get() })
            ConsoleWrapper.create(server.location, "${server.javaVersion} -jar installer.jar -installServer") {
                lineCount.incrementAndGet()
                lastOutput.set(it)
            }
            job.complete()
        }
    },
    NEOFORGE {
        private lateinit var mcVersions: Deferred<List<String>>
        private lateinit var neoVersions: Deferred<List<String>>

        private fun NodeList.asSequence(): Sequence<Node> {
            return generateSequence(0) {

            }
        }

        override suspend fun init() {
            val builderFactory = DocumentBuilderFactory.newInstance()
            builderFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true)
            val builder = builderFactory.newDocumentBuilder()
            val document = withContext(Dispatchers.IO) {
                builder.parse("https://maven.neoforged.net/releases/net/neoforged/neoforge/maven-metadata.xml")
            }.documentElement
            document.normalize()
            document.getElementsByTagName("version").forEach {

            }
        }
    },
    PUFFERFISH;

    open val cfModLoaderType = -1

    companion object {
        suspend fun init() = entries.forEach { it.init() }
    }

    open suspend fun downloadFiles(server: Server) = writeEula(server.location)

    open fun getStartCommand(server: Server): String = TODO("Not implemented yet")

    open suspend fun supportsVersion(mcVersion: String): Boolean = false

    // This will be started in a coroutine before anything else is loaded
    protected open suspend fun init() = Unit

    override fun toString(): String = StringUtils.capitalize(super.toString().lowercase())
}