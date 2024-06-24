import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter

private suspend fun writeEula(serverRoot: File) = withContext(Dispatchers.IO) {
    BufferedWriter(FileWriter(serverRoot.absolutePath + "/eula.txt")).use {
        it.write("eula=true")
    }
}

enum class ModLoader {

    ;

    suspend fun downloadFiles(server: Server) {
        writeEula(server.location)
    }
}