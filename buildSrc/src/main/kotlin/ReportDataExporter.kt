import org.apache.poi.hssf.usermodel.HSSFFont
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.*
import java.io.File
import java.io.FileOutputStream

class ReportDataExporter {
    fun process(input: List<Line>, path: String, fileName: String) {
        input.forEach {
            println("line -> $it")
        }

        val dst = File(path)
        if (!dst.exists())
            dst.mkdirs()

        val workbook = HSSFWorkbook()
        val sheet = workbook.createSheet()

        sheet.setColumnWidth(0, 12000)
        sheet.setColumnWidth(1, 15000)

        var lastRowIndex = 0
        writeServiceInfo(workbook = workbook, sheet = sheet, rowIndex = lastRowIndex)

        lastRowIndex += 1
        writeFormattedStringInfo(workbook = workbook, sheet = sheet, rowIndex = lastRowIndex)

        lastRowIndex += 2
        writeHeaderInfo(workbook = workbook, sheet = sheet, rowIndex = lastRowIndex)

        lastRowIndex += 2
        input.forEachIndexed { index, line ->
            val comment = input.getOrNull(index - 1).run { if (this is ModifiedStringLine) this else null }

            when (line) {
                is StringLine -> {
                    writeStringLine(
                        workbook = workbook,
                        sheet = sheet,
                        rowIndex = lastRowIndex,
                        line = line,
                        comment = comment
                    )
                    lastRowIndex += 1
                }
                is DeletedStringLine -> {
                    writeDeletedStringLine(
                        workbook = workbook,
                        sheet = sheet,
                        rowIndex = lastRowIndex,
                        line = line
                    )
                    lastRowIndex += 1
                }
                is CommentLine -> {
                    writeCommentLine(
                        workbook = workbook,
                        sheet = sheet,
                        rowIndex = lastRowIndex,
                        line = line
                    )
                    lastRowIndex += 1
                }
            }
        }
        val outputStream = FileOutputStream(path + fileName)
        workbook.write(outputStream)
        workbook.close()
    }

    private fun writeServiceInfo(workbook: HSSFWorkbook, sheet: Sheet, rowIndex: Int) {
        val row = sheet.createRow(rowIndex)
        val style: CellStyle = workbook.createCellStyle()
        val font: HSSFFont = workbook.createFont()
        font.setFontName("Arial")
        font.setFontHeightInPoints(10.toShort())
        font.italic = true
        style.setFont(font)

        with(row.createCell(0)) {
            setCellValue("Strings with arguments can contains %s (for Android) or %@  (for iOS) symbols")
            setCellStyle(style)
        }

        with(row.createCell(4)) {
            setCellValue(Constants.attentionBeforeUsageText)
            setCellStyle(style)
        }
    }

    private fun writeFormattedStringInfo(workbook: HSSFWorkbook, sheet: Sheet, rowIndex: Int) {
        val row = sheet.createRow(rowIndex)
        val style: CellStyle = workbook.createCellStyle()
        val font: HSSFFont = workbook.createFont()
        font.setFontName("Arial")
        font.setFontHeightInPoints(10.toShort())
        font.italic = true
        style.setFont(font)

        with(row.createCell(0)) {
            setCellValue("* - formated string")
            setCellStyle(style)
        }
    }

    private fun writeHeaderInfo(workbook: HSSFWorkbook, sheet: Sheet, rowIndex: Int) {
        val row = sheet.createRow(rowIndex)
        val style: CellStyle = workbook.createCellStyle()
        val font: HSSFFont = workbook.createFont()
        font.setFontName("Arial")
        font.setFontHeightInPoints(12.toShort())
        font.bold = true
        font.italic = true
        style.setFont(font)
        with(row.createCell(0)) {
            setCellValue("key")
            setCellStyle(style)
        }
        with(row.createCell(1)) {
            setCellValue("default value")
            setCellStyle(style)
        }
    }

    private fun writeStringLine(
        workbook: HSSFWorkbook,
        sheet: Sheet,
        rowIndex: Int,
        line: StringLine,
        comment: ModifiedStringLine?
    ) {
        val row = sheet.createRow(rowIndex)
        val style: CellStyle = workbook.createCellStyle()
        val font: HSSFFont = workbook.createFont()
        font.setFontName("Arial")
        font.setFontHeightInPoints(10.toShort())
        style.setFont(font)


        comment?.let {
            when (comment.type) {
                ModifiedStringLine.Type.ADDED -> {
                    style.setFillForegroundColor(IndexedColors.GREEN.getIndex());
                    style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                }
                ModifiedStringLine.Type.UPDATED -> {
                    style.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
                    style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                }
            }
            with(row.createCell(2)) {
                setCellValue(it.comment)
                setCellStyle(style)
            }
        }
        with(row.createCell(0)) {
            setCellValue(line.key)
            setCellStyle(style)
        }
        with(row.createCell(1)) {
            setCellValue(line.value)
            setCellStyle(style)
        }
    }

    private fun writeCommentLine(workbook: HSSFWorkbook, sheet: Sheet, rowIndex: Int, line: CommentLine) {
        val row = sheet.createRow(rowIndex)
        val style: CellStyle = workbook.createCellStyle()
        val font: HSSFFont = workbook.createFont()
        font.setFontName("Arial")
        font.setFontHeightInPoints(14.toShort())
        font.bold = true
        style.setFont(font)

        with(row.createCell(4)) {
            setCellValue(line.comment)
            setCellStyle(style)
        }
    }

    private fun writeDeletedStringLine(workbook: HSSFWorkbook, sheet: Sheet, rowIndex: Int, line: DeletedStringLine) {
        val row = sheet.createRow(rowIndex)
        val style: CellStyle = workbook.createCellStyle()
        val font: HSSFFont = workbook.createFont()
        font.setFontName("Arial")
        font.setFontHeightInPoints(10.toShort())
        style.setFont(font)
        style.setFillForegroundColor(IndexedColors.RED.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        with(row.createCell(0)) {
            setCellValue(line.comment)
            setCellStyle(style)
        }
    }
}