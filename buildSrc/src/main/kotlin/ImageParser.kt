class ImageParser {
    private val fileNameRegex = "(?<name>[_\\w]+).(?<extension>[\\w]+)".toRegex()

    fun parse(data: List<String>): List<Line> {
        val result = mutableListOf<Line>()
        data.forEach {
            result.add(parseLine(input = it) ?: EmptyLine())
        }
        return result
    }

    private fun parseLine(input: String): ImageLine? {
        val match = fileNameRegex.find(input)
        if (match != null) {
            val name = match.groups["name"]?.value
            val extension = match.groups["extension"]?.value
            if (name != null && extension != null)
                return ImageLine(name, extension)
        }
        return null
    }
}