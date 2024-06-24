package client

import constants.SLIDING_WINDOW_CONF
import constants.SOCKET_CONN
import java.io.*
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.SocketTimeoutException

fun sendFile(file: File) {
    val clientSocket = DatagramSocket()
    clientSocket.soTimeout = 5000
    val address = InetAddress.getByName(SOCKET_CONN.IP_ADDR)

    // Metadata from file
    val fileSize = file.length()
    val fileName = file.name
    file.readBytes().toList().toByteArray()

    val baos = ByteArrayOutputStream()
    val dos = DataOutputStream(baos)

    var fileBytes = file.readBytes().toList()
    var packetNumber = 1

    while (fileBytes.isNotEmpty()) {
        val fileChunk = fileBytes.take(SLIDING_WINDOW_CONF.PACKET_SIZE - 1023)
        try {
            sendChunk(
                dos,
                fileName,
                fileSize,
                packetNumber,
                fileChunk,
                baos,
                address,
                clientSocket,
                error = (0..4).random() == 2
            )

            val ackData = ByteArray(Int.SIZE_BYTES)
            val clientACKDatagram = DatagramPacket(ackData, ackData.size)
            clientSocket.receive(clientACKDatagram)
            val ackStream = DataInputStream(ByteArrayInputStream(clientACKDatagram.data))
            val confirmation = packetNumber == ackStream.readInt()

            if (!confirmation) {
                println("Acuse no recibida, reenviando paquete $packetNumber")
                // sendChunk(dos, fileName, fileSize, packetNumber, fileChunk, baos, address, clientSocket)
            }
            else {
                println("Acuse recibida, paquete $packetNumber enviado correctamente")
                fileBytes = fileBytes.drop(SLIDING_WINDOW_CONF.PACKET_SIZE - 1023)
                packetNumber++
            }
        } catch (e: SocketTimeoutException) {
            println("Acuse no recibida, reenviando paquete $packetNumber")
            // sendChunk(dos, fileName, fileSize, packetNumber, fileChunk, baos, address, clientSocket)
        }

    }
}

private fun sendChunk(
    dos: DataOutputStream,
    fileName: String,
    fileSize: Long,
    packetNumber: Int,
    fileChunk: List<Byte>,
    baos: ByteArrayOutputStream,
    address: InetAddress?,
    clientSocket: DatagramSocket,
    error: Boolean = false
) {
    dos.writeInt(fileName.length)
    dos.write(fileName.toByteArray())
    dos.writeLong(fileSize)
    dos.writeInt(packetNumber)
    dos.writeInt(fileChunk.size)
    dos.write(fileChunk.toByteArray())

    val packet = baos.toByteArray()
    val datagramPacket = DatagramPacket(
        packet,
        packet.size,
        address,
        SOCKET_CONN.PORT
    )
    if (error) {
        baos.reset()
        return
    }
    clientSocket.send(datagramPacket)
    baos.reset()
}