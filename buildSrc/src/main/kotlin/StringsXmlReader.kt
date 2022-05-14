import java.io.File

class StringsXmlReader {
    fun readStringsXmlToList(path: String, fileName: String): List<String> {
        val file = File(path, fileName)
        return file.readLines()
    }
}
