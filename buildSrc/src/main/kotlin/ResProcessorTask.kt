import java.io.File

class ResProcessorTask {
    lateinit var androidStringsXmlPath: String
    var androidStringXmlFileName = "strings.xml"
    var textResGenerate: Boolean = true

    lateinit var androidDrawablesPath: String
    lateinit var androidDrawablePrefixFilter: String
    var imageResGenerate: Boolean = true

    lateinit var darwinLocalizationFolderPath: String
    lateinit var darwinLocalizableSourceFileName: String
    var darwinExport: Boolean = false

    lateinit var generatedResClassesPath: String
    lateinit var generatedResClassesPackage: String

    var generatedTextResFileName: String = "TextRes"
    var generatedImageResFileName: String = "ImageRes"

    fun doTask() {
        if (textResGenerate) {
            val data = TextParser().parse(
                StringsXmlReader().readStringsXmlToList(
                    path = androidStringsXmlPath,
                    fileName = androidStringXmlFileName
                )
            )
            TextResProcessor(
                packageName = generatedResClassesPackage,
                className = generatedTextResFileName
            ).exec(
                data = data,
                directory = File(generatedResClassesPath)
            )
            if (darwinExport)
                DarwinDataExporter().process(
                    input = data,
                    path = darwinLocalizationFolderPath,
                    fileName = darwinLocalizableSourceFileName
                )
        }
        if (imageResGenerate) {
            val data = DrawableNamesListReader()
                .readFileNamesToList(
                    path = androidDrawablesPath,
                    prefix = androidDrawablePrefixFilter
                )
            val mData = ImageParser().parse(data = data)
            ImageResProcessor(
                packageName = generatedResClassesPackage,
                className = generatedImageResFileName,
                imagePrefix = androidDrawablePrefixFilter
            ).exec(
                data = mData,
                directory = File(generatedResClassesPath)
            )
        }
    }
}