import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.ser.std.StdSerializer

class ServerSerializer : StdSerializer<Server>(null as Class<Server>?) {
    override fun serialize(server: Server?, generator: JsonGenerator?, provider: SerializerProvider?) {
        if (generator == null) throw IllegalArgumentException("Generator is null")
        if (server == null) throw IllegalArgumentException("Server is null")
        with(generator) {
            writeStartObject()
            writeStringField("name", server.name)
            writeStringField("location", server.location.absolutePath)
            writeStringField("modLoader", server.modLoader.toString().uppercase())
            writeStringField("mcVersion", server.mcVersion)
            writeNumberField("mbMemory", server.mbMemory)
            writeStringField("javaVersion", server.javaVersion)
            writeNumberField("lastOpened", server.lastOpened)
            writeEndObject()
        }
    }
}