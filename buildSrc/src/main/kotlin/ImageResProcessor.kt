import com.squareup.kotlinpoet.*
import java.io.File

class ImageResProcessor(
    override val packageName: String,
    override val className: String,
    private val imagePrefix: List<String>
) : ResProcessor<Line> {

    companion object {
        private const val keyArg = "key"
    }

    override fun exec(data: List<Line>, directory: File) {
        FileSpec.builder(packageName, className)
            .addType(
                TypeSpec.enumBuilder(className)
                    .addKdoc(Constants.attentionBeforeUsageText)
                    .addProperty(
                        PropertySpec.builder(name = keyArg, type = String::class)
                            .initializer(keyArg)
                            .build()
                    )
                    .primaryConstructor(
                        FunSpec.constructorBuilder()
                            .addParameter(keyArg, String::class)
                            .build()
                    )
                    .apply {
                        data.forEach { line ->
                            if (line is ImageLine)
                                handleImageLine(line = line)
                        }
                    }
                    .build()
            )
            .addFileComment(Constants.classTopPictureText).build()
            .apply {
                if (Constants.isInDebug)
                    writeTo(System.out)
            }
            .writeTo(directory)
    }

    private fun TypeSpec.Builder.handleImageLine(line: ImageLine) {
        val key = prepareKey(line = line)

        addEnumConstant(
            name = key,
            typeSpec = TypeSpec.anonymousClassBuilder()
                .addSuperclassConstructorParameter("%S", line.name)
                .build()
        )
    }

    private fun prepareKey(line: ImageLine): String =
        retrievePrefixFromExtension(extension = line.extension) + formInnerClassName(key = line.name)

    private fun retrievePrefixFromExtension(extension: String): String =
        if (extension == "xml")
            "Ico"
        else
            "Img"

    private fun formInnerClassName(key: String): String {
        var name: String? = null
        imagePrefix.forEach {
                if (name == null && key.startsWith(it))
                    name = key.removePrefix(it)
                        .splitToSequence("_")
                        .map { it.capitalize() }
                        .joinToString("")
            }
        return name ?: throw NullPointerException("Not found registered prefix for key->$key")
    }
}