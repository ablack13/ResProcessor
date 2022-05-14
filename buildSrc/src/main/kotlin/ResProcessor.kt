import java.io.File

interface ResProcessor<T> {
    val packageName: String
    val className: String

    fun exec(data: List<T>, directory: File)
}