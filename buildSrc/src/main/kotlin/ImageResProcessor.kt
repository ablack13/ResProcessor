import com.squareup.kotlinpoet.*
import java.io.File

class ImageResProcessor(
    override val packageName: String,
    override val className: String,
    private val imagePrefix: String
) : ResProcessor<Line> {

    companion object {
        private const val keyArg = "key"
    }

    override fun exec(data: List<Line>, directory: File) {
        FileSpec.builder(packageName, className)
            .addType(
                TypeSpec.classBuilder(className)
                    .addKdoc(Constants.attentionBeforeUsageText)
                    .addModifiers(KModifier.SEALED)
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

        addType(
            TypeSpec.objectBuilder(formInnerClassName(key = key))
                .superclass(ClassName(packageName, className))
                .addSuperclassConstructorParameter("$keyArg = %S", line.name)
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

    private fun formInnerClassName(key: String) =
        key.removePrefix(imagePrefix)
            .splitToSequence("_")
            .map { it.capitalize() }
            .joinToString("")
}