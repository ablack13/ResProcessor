import java.io.File

class DrawableNamesListReader {
    fun readFileNamesToList(path: String, prefix: List<String>): List<String> =
        File(path)
            .listFiles { _, name ->
                var find = false
                prefix.forEach {
                    if (!find)
                        find = name?.startsWith(prefix = it) ?: false
                }
                find
            }
            ?.map { it.name } ?: emptyList()
}