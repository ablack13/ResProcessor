class TextParser {
    private val stringResRegex =
        "(<string\\s+name=\")(?<key>[\\w]+)(\">)(?<value>[\\w\\s.,:\\-\\%s!&;<>#\"\'\\\'(?:)?:\\/|\\\\]*)(<\\/string>)".toRegex()
    private val commentRegex =
        "(<!--)(?<comment>[\\w\\s.,:\\-\\%s!@\\'(?:)?:\\/|]*)(-->)+".toRegex()
    private val addedCommentRegex = "[\\s]*(@a[\\w]*)(?<value>[\\w\\s.,:\\-\\%s!\\'(?:)?:\\/|\\\\]*)".toRegex()
    private val updateCommentRegex = "[\\s]*(@u[\\w]*)(?<value>[\\w\\s.,@><:\\-\\%s!\\'(?:)?:\\/|\\\\]*)".toRegex()
    private val deleteCommentRegex = "[\\s]*(@d[\\w]*)(?<value>[\\w\\s.,:\\-\\%s!\\'(?:)?:\\/|\\\\]*)".toRegex()

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

    private fun tryToParseComment(rowData: String): Line? {
        val match = commentRegex.find(rowData)
        if (match != null) {
            val comment = match.groups["comment"]?.value?.trim()

            if (comment != null) {
                if (comment.startsWith("@")) {
                    return parseMetadataComment(comment = comment)
                }
                return CommentLine(comment = comment)
            }
        }
        return null
    }

    private fun parseMetadataComment(comment: String): Line? =
        when {
            comment.startsWith("@a") -> {
                val match = addedCommentRegex.find(comment)
                if (match != null) {
                    val value = match.groups["value"]?.value?.trim()
                    ModifiedStringLine(
                        type = ModifiedStringLine.Type.ADDED,
                        comment = value
                    )
                } else
                    null
            }
            comment.startsWith("@u") -> {
                val match = updateCommentRegex.find(comment)
                if (match != null) {
                    val value = match.groups["value"]?.value?.trim()
                    ModifiedStringLine(
                        type = ModifiedStringLine.Type.UPDATED,
                        comment = value
                    )
                } else
                    null
            }
            comment.startsWith("@d") -> {
                val match = deleteCommentRegex.find(comment)
                if (match != null) {
                    val value = match.groups["value"]?.value?.trim()
                    DeletedStringLine(comment = value)
                } else
                    null
            }
            else -> null
        }


    private fun getEmptyLine(): EmptyLine =
        EmptyLine()
}
