import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel

abstract class ServerConfigTabType : Disposable {

    val mainScope = MainScope()

    companion object {
        private val INSTANCES = mutableListOf<ServerConfigTabType>()
        val SERVER_PROPERTIES = register(object : ServerConfigTabType() {
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