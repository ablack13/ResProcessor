import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import java.io.File

class TextResProcessor(
    override val packageName: String,
    override val className: String
) : ResProcessor<Line> {

    companion object {
        private const val pluralSuffix = "_plural"
        private const val keyArg = "key"
        private const val argsArg = "args"
        private const val pluralKeyArg = "pluralKey"
    }

    private val processedKeysSet: MutableSet<String> = mutableSetOf()

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
                    .addProperty(
                        PropertySpec.builder(name = argsArg, type = MutableList::class.parameterizedBy(String::class).copy(nullable = true))
                            .initializer(argsArg)
                            .build()
                    )
                    .addProperty(
                        PropertySpec.builder(name = pluralKeyArg, type = Int::class)
                            .initializer(pluralKeyArg)
                            .build()
                    )
                    .primaryConstructor(
                        FunSpec.constructorBuilder()
                            .addParameter(keyArg, String::class)
                            .addParameter(
                                ParameterSpec.builder(argsArg, MutableList::class.parameterizedBy(String::class).copy(nullable = true))
                                    .defaultValue("%L", null)
                                    .build()
                            )
                            .addParameter(
                                ParameterSpec.builder(pluralKeyArg, Int::class)
                                    .defaultValue("%L", -1)
                                    .build()
                            )
                            .build()
                    )
                    .apply {
                        data.forEachIndexed { index, line ->
                            val comment = data.getOrNull(index - 1)?.run { if (this is CommentLine) this else null }
                            if (line is StringLine)
                                handleStringLine(
                                    line = line,
                                    data = data,
                                    comment = comment
                                )
                        }
                    }
                    .build()
            )
            .addFileComment(Constants.classTopPictureText)
            .build()
            .apply {
                if (Constants.isInDebug)
                    writeTo(System.out)
            }
            .writeTo(directory)
        clearAllRegisteredExistingKeys()
    }

    private fun TypeSpec.Builder.handleStringLine(
        line: StringLine,
        data: List<Line>,
        comment: CommentLine?
    ) {
        val comment = comment?.comment
        val (key, isPluralType) = prepareKey(line = line)
        val value = line.value
        if (!isExistingKeyRegistered(key = key)) {
            registerExistingKey(key = key)

            when {
                isPluralType ->
                    processPluralString(
                        key = key,
                        keys = data,
                        comment = comment
                    )
                isFormatString(value = value) ->
                    processFormatString(
                        key = key,
                        value = value,
                        comment = comment
                    )
                else ->
                    processSimpleString(
                        key = key,
                        comment = comment
                    )
            }
        }
    }

    private fun isFormatString(value: String): Boolean =
        value.contains("%s")

    private fun getFormatStringArgsCount(value: String): Int =
        value.replace("%s", "~").count { it == '~' }

    private fun getPluralStringArgsCount(key: String, keys: List<Line>): Int =
        keys.filterIsInstance<StringLine>()
            .filter { it.key.startsWith(key) }
            .map { it.value.replace("%s", "~").count { it == '~' } }
            .maxOf { it }

    private fun prepareKey(line: StringLine): Pair<String, Boolean> {
        val isPluralType = line.key.contains(pluralSuffix)
        val key = line.key.run { if (isPluralType) substringBefore(pluralSuffix) else this }
        return key to isPluralType
    }

    private fun TypeSpec.Builder.processPluralString(
        key: String,
        keys: List<Line>,
        comment: String?
    ) {
        //get max argsCount from all plural strings this type
        val argsCount = getPluralStringArgsCount(key, keys)

        addType(
            TypeSpec.classBuilder(formInnerClassName(key = key))
                .apply {
                    if (comment != null)
                        addKdoc(comment)
                }
                .primaryConstructor(
                    FunSpec.constructorBuilder()
                        .apply {
                            addParameter(pluralKeyArg, Int::class)
                            if (argsCount > 1)
                                repeat(argsCount - 1) {
                                    addParameter(argArg(index = it), String::class)
                                }
                        }
                        .build()
                )
                .superclass(ClassName("", className))
                .addSuperclassConstructorParameter("$keyArg = %S", key)
                .apply {
                    if (argsCount == 1)
                        addSuperclassConstructorParameter("$argsArg = listOf(%L)", "$pluralKeyArg.toString()")
                    if (argsCount > 1) {
                        val args = mutableListOf<String>()

                        val argParam = buildString {
                            append("listOf(%L, ")
                            args.add("$pluralKeyArg.toString()")
                            repeat(argsCount - 1) {
                                append("%L")
                                if (it != argsCount - 2)
                                    append(", ")
                                args.add(argArg(index = it))
                            }
                            append(")")
                        }
                        addSuperclassConstructorParameter("$argsArg = $argParam", *args.toTypedArray())
                    }
                }
                .addSuperclassConstructorParameter("$pluralKeyArg = %L", pluralKeyArg)
                .build()
        )
    }

    private fun TypeSpec.Builder.processFormatString(
        key: String,
        value: String,
        comment: String?
    ) {
        val argsCount = getFormatStringArgsCount(value = value)

        addType(
            TypeSpec.classBuilder(formInnerClassName(key = key))
                .apply {
                    if (comment != null)
                        addKdoc(comment)
                }
                .primaryConstructor(
                    FunSpec.constructorBuilder()
                        .apply {
                            repeat(argsCount) {
                                addParameter(argArg(index = it), String::class)
                            }
                        }
                        .build()
                )
                .superclass(ClassName("", className))
                .addSuperclassConstructorParameter("$keyArg = %S", key)
                .apply {
                    if (argsCount > 0) {
                        val args = mutableListOf<String>()

                        val argParam = buildString {
                            append("listOf(")
                            repeat(argsCount) {
                                append("%L")
                                args.add(argArg(index = it))
                                if (it != argsCount - 1)
                                    append(", ")
                            }
                            append(")")
                        }
                        addSuperclassConstructorParameter("$argsArg = $argParam", *args.toTypedArray())
                    }
                }
                .build()
        )
    }

    private fun TypeSpec.Builder.processSimpleString(key: String, comment: String?) {
        addType(
            TypeSpec.objectBuilder(formInnerClassName(key = key))
                .apply {
                    if (comment != null)
                        addKdoc(comment)
                }
                .superclass(ClassName(packageName, className))
                .addSuperclassConstructorParameter("$keyArg = %S", key)
                .build()
        )
    }

    private fun isExistingKeyRegistered(key: String): Boolean =
        processedKeysSet.contains(element = key)

    private fun registerExistingKey(key: String) {
        processedKeysSet.add(element = key)
    }

    private fun clearAllRegisteredExistingKeys() {
        processedKeysSet.clear()
    }

    private fun argArg(index: Int) = "arg${index + 1}"

    private fun formInnerClassName(key: String) =
        key.splitToSequence("_")
            .map { it.capitalize() }
            .joinToString("")
}