package server

import constants.DIRECTORY
import constants.SLIDING_WINDOW_CONF
import constants.SOCKET_CONN
import java.io.*
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress

fun receiveFile() {
    val serverSocket = DatagramSocket(SOCKET_CONN.PORT)
    serverSocket.reuseAddress = true

    // Datos del archivo
    var fileName: String
    var fileSize: Int

    // OutputStream para escribir el archivo
    var fos: FileOutputStream? = null

    // InputStream para leer la información del paquete recibido
    var dis: DataInputStream

    while (true) {
        val b = ByteArray(SLIDING_WINDOW_CONF.PACKET_SIZE)
        val packet = DatagramPacket(b, b.size)
        serverSocket.receive(packet)

        dis = DataInputStream(ByteArrayInputStream(packet.data))

        // Leyendo el nombre del archivo
        val fileNameLength = dis.readInt()
        val fileNameBytes = ByteArray(fileNameLength)
        dis.read(fileNameBytes)
        fileName = String(fileNameBytes)

        // Leyendo el tamaño del archivo
        fileSize = dis.readLong().toInt()

        // Leyendo el número de paquete
        val packetNumber = dis.readInt()
        println("Recibiendo paquete $packetNumber")

        // Leyendo los bytes del archivo
        val fileChunkSize = dis.readInt()
        val fileBytes = ByteArray(fileChunkSize)
        dis.read(fileBytes)
        fos = fos ?: FileOutputStream(File("${DIRECTORY.SERVER_DIR}/$fileName"))
        fos.write(fileBytes)

        sendACK(serverSocket, packetNumber, packet.address, packet.port)
    }
}

private fun sendACK(socket: DatagramSocket, packetNumber: Int, addr: InetAddress, port: Int) {
    val baos = ByteArrayOutputStream()


    val dos = DataOutputStream(baos)

    dos.writeInt(packetNumber)

    val packet = baos.toByteArray()
    val datagramPacket = DatagramPacket(packet, packet.size, addr, port)
    socket.send(datagramPacket)
}