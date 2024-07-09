import kotlinx.coroutines.Deferred
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel

abstract class ServerConfigTabType : Disposable {

    protected val mainScope = MainScope()

    companion object {
        private val INSTANCES = mutableListOf<ServerConfigTabType>()
        private val SERVER_PROPERTIES = register(object : ServerConfigTabType() {
            private val descriptions: Deferred<Map<String, String>>
            private val dataTypes: Deferred<Map<String, String>>
            private val defaults: Deferred<Map<String, String>>

            init {
                val rows = mainScope.async {
                    val document = getJsoup("https://minecraft.wiki/w/Server.properties")
                    val table = document.select("table[data-description=Server properties]").first()
                    table!!.select("tr")
                }
                descriptions = mainScope.async {
                    rows.await().associate {
                        val cells = it.select("td")
                        Pair(cells[0].text(), cells[3].html())
                    }
                }
                dataTypes = mainScope.async {
                    rows.await().associate {
                        val cells = it.select("td")
                        Pair(cells[0].text(), cells[1].text())
                    }
                }
                defaults = mainScope.async {
                    rows.await().associate {
                        val cells = it.select("td")
                        Pair(cells[0].text(), cells[2].text())
                    }
                }
            }

            suspend fun getDescription(key: String): String {
                return descriptions.await()[key] ?: "Not found"
            }

            suspend fun getDataType(key: String): PropertyType {
                val dataType = dataTypes.await()[key]
                val defaultValue = defaults.await()[key]
                return PropertyType.get(dataType, defaultValue)
            }

            suspend fun getDefaultValue(key: String): String {
                return defaults.await()[key] ?: "Not found"
            }

            override fun canOpenFile(relativePath: String) = relativePath == "server.properties"

            override fun createTab(server: Server) = ServerPropertiesTab(server)
        })

        fun getForFile(relativePath: String): ServerConfigTabType? {
            return INSTANCES.find { it.canOpenFile(relativePath) }
        }

        private fun register(type: ServerConfigTabType): ServerConfigTabType {
            INSTANCES.add(type)
            return type
        }
    }

    abstract fun canOpenFile(relativePath: String): Boolean

    abstract fun createTab(server: Server): ServerConfigTab

    override fun dispose() {
        mainScope.cancel()
    }
}