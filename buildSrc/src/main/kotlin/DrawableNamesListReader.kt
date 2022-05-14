import java.io.File

class DrawableNamesListReader {
    fun readFileNamesToList(path: String, prefix: String): List<String> =
        File(path)
            .listFiles { _, name -> name?.startsWith(prefix) ?: false }
            ?.map { it.name } ?: emptyList()
}