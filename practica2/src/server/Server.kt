package server

import constants.DIRECTORY
import java.io.File

fun main() {
    // Create server directory if not exists
    val serverDir = File(DIRECTORY.SERVER_DIR)
    if (!serverDir.exists()) {
        serverDir.mkdir()
    }

    receiveFile()
}