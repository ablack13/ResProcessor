
interface Line

data class CommentLine(val comment: String) : Line

data class StringLine(val key: String, val value: String) : Line

class EmptyLine : Line

class ImageLine(val name:String, val extension:String) : Line
