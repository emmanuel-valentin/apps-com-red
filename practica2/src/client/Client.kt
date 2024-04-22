package client

import helpers.getFileSelected

fun main() {
    val fileSelected = getFileSelected()
    fileSelected?.let {
        sendFile(it)
    }
}