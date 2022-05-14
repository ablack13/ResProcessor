import java.io.File

class DarwinDataExporter {
    fun process(input: List<Line>, path: String, fileName: String) {
        val dst = File(path, fileName)
        if (!dst.exists())
            dst.createNewFile()
        dst.writeText("//" + Constants.attentionBeforeUsageText)
        input.forEach {
            when (it) {
                is CommentLine -> dst.appendText("\n//${it.comment}\n")
                is StringLine -> dst.appendText("\"${it.key}\" = \"${it.value.replace("%s", "%@")}\";\n")
            }
        }
    }
}