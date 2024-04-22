package helpers

import java.io.File
import javax.swing.JFileChooser

fun getFileSelected(): File? {
    val fileChooser = JFileChooser("./")
    fileChooser.fileSelectionMode = JFileChooser.FILES_ONLY
    fileChooser.fileSelectionMode = JFileChooser.FILES_ONLY

    if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
        return fileChooser.selectedFile
    }

    return null
}