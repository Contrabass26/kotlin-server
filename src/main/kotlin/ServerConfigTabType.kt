abstract class ServerConfigTabType {

    companion object {
        private val INSTANCES = mutableListOf<ServerConfigTabType>()

        init {
            createAndRegister(::ServerPropertiesTab) { it == "server.properties" }
        }

        fun getForFile(relativePath: String): ServerConfigTabType? {
            return INSTANCES.find { it.canOpenFile(relativePath) }
        }

        private fun createAndRegister(createTab: (Server) -> ServerConfigTab, canOpenFile: (String) -> Boolean): ServerConfigTabType {
            val type = object : ServerConfigTabType() {
                override fun canOpenFile(relativePath: String) = canOpenFile(relativePath)

                override fun createTab(server: Server) = createTab(server)
            }
            register(type)
            return type
        }

        private fun register(type: ServerConfigTabType) {
            INSTANCES.add(type)
        }
    }

    abstract fun canOpenFile(relativePath: String): Boolean

    abstract fun createTab(server: Server): ServerConfigTab
}