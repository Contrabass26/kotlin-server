import com.fasterxml.jackson.databind.node.ArrayNode
import kotlinx.coroutines.*
import org.apache.commons.lang3.StringUtils
import org.apache.commons.lang3.SystemUtils
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import org.w3c.dom.Text
import java.awt.Desktop
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.net.URI
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference
import javax.swing.JOptionPane
import javax.swing.JTextPane
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
            server.run { "$javaVersion -Xmx${mbMemory}M -jar server.jar nogui" }

        override suspend fun supportsVersion(mcVersion: String): Boolean = mcVersions.await().contains(mcVersion)

        override suspend fun init() {
            coroutineScope {
                mcVersions = async {
                    getVersionsJson().asSequence()
                        .filter { it.get("type").textValue() == "release" }
                        .map { it.get("id").textValue() }
                        .toList()
                }
                logger.info("Loaded ${mcVersions.await().size} supported versions")
            }
        }
    },
    FABRIC {
        private lateinit var mcVersions: Deferred<List<String>>
        private lateinit var loaderVersion: Deferred<String>
        private lateinit var installerVersion: Deferred<String>
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
                loaderVersion = async {
                    getJson(getUrl("https://meta.fabricmc.net/v2/versions/loader"))
                        .find { it.get("stable").booleanValue() }!!
                        .get("version").textValue()
                }
                // Installer
                installerVersion = async {
                    getJson(getUrl("https://meta.fabricmc.net/v2/versions/installer"))
                        .find { it.get("stable").booleanValue() }!!
                        .get("version").textValue()
                }
                // Log messages
                logger.info("Loaded ${mcVersions.await().size} supported versions")
                logger.info("Loader version = ${loaderVersion.await()}")
                logger.info("Installer version = ${installerVersion.await()}")
            }
        }

        override fun getStartCommand(server: Server): String =
            server.run { "$javaVersion -Xmx${mbMemory}M -jar fabric-server-launch.jar nogui" }

        override suspend fun supportsVersion(mcVersion: String): Boolean = mcVersions.await().contains(mcVersion)

        override suspend fun downloadFiles(server: Server) {
            super.downloadFiles(server)
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
                logger.info("Loaded ${mcVersions.await().size} supported versions")
            }
        }

        override fun getStartCommand(server: Server): String =
            getForgeStartCommand("minecraftforge", "forge", server)

        override suspend fun supportsVersion(mcVersion: String): Boolean = mcVersions.await().contains(mcVersion)

        override suspend fun downloadFiles(server: Server) {
            super.downloadFiles(server)
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
            runForgeInstaller(server)
        }
    },
    NEOFORGE {
        private lateinit var mcVersions: Deferred<List<String>>
        private lateinit var neoVersions: Deferred<List<String>>
        override val cfModLoaderType = 6

        private fun NodeList.asSequence(): Sequence<Node> = (0..<length)
            .asSequence()
            .map { item(it) }

        private fun normaliseVersion(version: String): String {
            val mcVersion = "1.${StringUtils.substringBeforeLast(version, ".")}"
            if (mcVersion.endsWith(".0")) return mcVersion.substring(0, mcVersion.length - 2)
            return mcVersion
        }

        override suspend fun init() {
            coroutineScope {
                neoVersions = async {
                    val builderFactory = DocumentBuilderFactory.newInstance()
                    builderFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true)
                    val builder = builderFactory.newDocumentBuilder()
                    val document = withContext(Dispatchers.IO) {
                        builder.parse("https://maven.neoforged.net/releases/net/neoforged/neoforge/maven-metadata.xml")
                    }.documentElement
                    document.normalize()
                    document.getElementsByTagName("version")
                        .asSequence()
                        .map { (it.firstChild as Text).wholeText }
                        .toList()
                }
                mcVersions = async {
                    neoVersions.await()
                        .asSequence()
                        .map { normaliseVersion(it) }
                        .distinct()
                        .toList()
                }
                // Log messages
                logger.info("Loaded ${neoVersions.await().size} NeoForge versions")
                logger.info("Loaded ${mcVersions.await().size} supported Minecraft versions")
            }
        }

        override fun getStartCommand(server: Server): String =
            getForgeStartCommand("neoforged", "neoforge", server)

        override suspend fun supportsVersion(mcVersion: String): Boolean = mcVersions.await().contains(mcVersion)

        override suspend fun downloadFiles(server: Server) {
            super.downloadFiles(server)
            val neoVersion = neoVersions.await()
                .find { MC_VERSION_COMPARATOR.compare(normaliseVersion(it), server.mcVersion) == 0 }!!
            val url = getUrl("https://maven.neoforged.net/releases/net/neoforged/neoforge/%s/neoforge-$neoVersion-installer.jar")
            downloadFile(url, server.relativeFile("/installer.jar"))
            runForgeInstaller(server)
        }
    },
    PUFFERFISH {
        private lateinit var ghBranches: Deferred<Set<String>>

        override suspend fun init() {
            coroutineScope {
                ghBranches = async {
                    (getJson(getUrl("https://api.github.com/repos/pufferfish-gg/Pufferfish/branches")) as ArrayNode)
                        .map { it.get("name").textValue() }
                        .toSet()
                }
                logger.info("Loaded ${ghBranches.await().size} GitHub branches")
            }
        }

        override suspend fun supportsVersion(mcVersion: String): Boolean = ghBranches.await().contains("ver/${getMajorVersion(mcVersion)}")

        override fun getStartCommand(server: Server): String = 
            server.run { "$javaVersion -Xmx${mbMemory}M -jar pufferfish.jar nogui" }

        override suspend fun downloadFiles(server: Server) {
            super.downloadFiles(server)
            // Get initial build
            val changelogUrl = "https://ci.pufferfish.host/job/Pufferfish-${server.majorMcVersion}/changes"
            val initialBuild = getInitialBuild(server, changelogUrl)
            // Confirm initial choice with the user
            val message = if (initialBuild == null)
                "No relevant build was detected on <a href=$changelogUrl>changelog</a>"
            else
                "Detected build %s in <a href=$changelogUrl>changelog</a>"
            val messageLabel = JTextPane()
            messageLabel.contentType = "text/html"
            messageLabel.text = "<html>$message - enter the build to use:</html>"
            messageLabel.isEditable = false
            messageLabel.addMouseListener(object : MouseAdapter() {
                override fun mouseReleased(e: MouseEvent) {
                    Desktop.getDesktop().browse(URI(changelogUrl))
                }
            })
            val chosenBuild = JOptionPane.showInputDialog(
                MAIN_SCREEN,
                messageLabel,
                "Enter build to use",
                JOptionPane.QUESTION_MESSAGE,
                null,
                null,
                initialBuild ?: ""
            ) as String
            // Download file with chosen build
            val document1 = getJsoup("https://ci.pufferfish.host/job/Pufferfish-${server.majorMcVersion}/$chosenBuild")
            val relativeJarPath = document1.select(".fileList")[0]
                .child(0)
                .child(0)
                .child(1)
                .child(0)
                .attr("href")
            val url = getUrl("https://ci.pufferfish.host/job/Pufferfish-${server.majorMcVersion}/$chosenBuild/$relativeJarPath")
            downloadFile(url, server.relativeFile("/pufferfish.jar"))
        }

        private suspend fun getInitialBuild(server: Server, url: String): String? {
            val elements = getJsoup(url)
                .select("#main-panel")
                .first()
                ?.children()
            if (elements == null) return null
            return (0..<elements.size)
                .asSequence()
                .filter { elements[it].`is`("h2") }
                .filter { elements[it + 1].`is`("ol") }
                .find { elements[it + 1].text().contains(server.mcVersion) }
                ?.let { StringUtils.substringBetween(elements[it].text(), "#", " ") }
        }
    };

    open val cfModLoaderType = -1
    protected val logger: Logger = LogManager.getLogger(this.name)

    companion object {
        suspend fun init() = coroutineScope {
            entries.forEach {
                launch { it.init() }
            }
        }

        protected fun getForgeStartCommand(organisation: String, name: String, server: Server): String {
            val os = if (SystemUtils.IS_OS_WINDOWS) "win" else "unix"
            return server.run {
                location.listFiles()!!
                    .asSequence()
                    .map { it.name }
                    .filter { it.contains(mcVersion) }
                    .find { it.matches("minecraft_server.*\\.jar".toRegex()) }
                    ?.let { "$javaVersion -Xmx${mbMemory}M -jar $it nogui" }
                    ?: relativeFile("/libraries/net/$organisation/$name").listFiles()!!
                        .map { it.name }
                        .find { it.contains(mcVersion) }!!
                        .let { "$javaVersion -Xmx${mbMemory}M @libraries/net/$organisation/$name/$it/${os}_args.txt nogui %*" }
            }
        }

        protected suspend fun runForgeInstaller(server: Server) {
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
    }

    open suspend fun downloadFiles(server: Server) = writeEula(server.location)

    open fun getStartCommand(server: Server): String = TODO("Not implemented yet")

    open suspend fun supportsVersion(mcVersion: String): Boolean = false

    // This will be started in a coroutine before anything else is loaded
    protected open suspend fun init() = Unit

    override fun toString(): String = StringUtils.capitalize(super.toString().lowercase())
}