
class TextParser {
    private val stringResRegex =
        "(<string\\s+name=\")(?<key>[\\w]+)(\">)(?<value>[\\w\\s.,:\\-\\%s!\\\'(?:)?:\\/|\\\\]*)(<\\/string>)".toRegex()
    private val commentRegex =
        "(<!--)(?<comment>[\\w\\s.,:\\-\\%s!\\'(?:)?:\\/|]*)(-->)+".toRegex()

    fun parse(data: List<String>): List<Line> {
        val result = mutableListOf<Line>()

        data.forEach { rowData ->
            var line: Line?
            line = tryToParseString(rowData = rowData)
            if (line == null)
                line = tryToParseComment(rowData = rowData)
            if (line == null)
                line = getEmptyLine()

            result.add(line)
        }
        return result
    }

    private fun tryToParseString(rowData: String): StringLine? {
        val match = stringResRegex.find(rowData)
        if (match != null) {
            val key = match.groups["key"]?.value
            val value = match.groups["value"]?.value

            if (key != null && value != null)
                return StringLine(key = key, value = value)
        }
        return null
    }

    private fun tryToParseComment(rowData: String): CommentLine? {
        val match = commentRegex.find(rowData)
        if (match != null) {
            val comment = match.groups["comment"]?.value

            if (comment != null)
                return CommentLine(comment = comment)
        }
        return null
    }

    private fun getEmptyLine(): EmptyLine =
        EmptyLine()
}
