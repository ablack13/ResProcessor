import java.io.File

class ResProcessorTask {
    lateinit var androidStringsXmlPath: String
    var androidStringXmlFileName = "strings.xml"
    var textResGenerate: Boolean = true

    lateinit var androidDrawablesPath: String
    lateinit var androidDrawablePrefixFilter: List<String>
    var imageResGenerate: Boolean = true

    lateinit var darwinLocalizationFolderPath: String
    lateinit var darwinLocalizableSourceFileName: String
    var darwinExport: Boolean = false

    lateinit var generatedResClassesPath: String
    lateinit var generatedResClassesPackage: String

    lateinit var generatedReportPath: String
    lateinit var generatedReportFileName: String
    var reportExport: Boolean = false

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
            if (reportExport)
                ReportDataExporter().process(
                    input = data,
                    path = generatedReportPath,
                    fileName = generatedReportFileName
                )
            if (darwinExport)
                DarwinDataExporter().process(
                    input = data,
                    path = darwinLocalizationFolderPath,
                    fileName = darwinLocalizableSourceFileName
                )
        }
        if (imageResGenerate) {
            val prefixFilter = androidDrawablePrefixFilter.sortedDescending()
            val data = DrawableNamesListReader()
                .readFileNamesToList(
                    path = androidDrawablesPath,
                    prefix = prefixFilter
                )
            val mData = ImageParser().parse(data = data)
            ImageResProcessor(
                packageName = generatedResClassesPackage,
                className = generatedImageResFileName,
                imagePrefix = prefixFilter
            ).exec(
                data = mData,
                directory = File(generatedResClassesPath)
            )
        }
    }
}