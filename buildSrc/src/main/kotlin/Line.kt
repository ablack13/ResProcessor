interface Line

data class CommentLine(val comment: String) : Line

data class StringLine(val key: String, val value: String) : Line

class EmptyLine : Line

data class ImageLine(val name: String, val extension: String) : Line

data class ModifiedStringLine(val type: Type, val comment: String?) : Line {
    enum class Type { ADDED, UPDATED }
}

data class DeletedStringLine(val comment: String?) : Line