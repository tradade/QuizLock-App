package com.quizlock.core.data

import org.w3c.dom.Element
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.text.Normalizer
import java.util.Locale
import java.util.zip.ZipInputStream
import javax.xml.parsers.DocumentBuilderFactory

/**
 * Imports the Quiz Gate question spreadsheet template.
 *
 * Supported columns (header row optional):
 * STT | Question | A | B | C | D | Correct | Description
 * Optional: Topic, Age Band, Difficulty, Hint, Lang.
 *
 * A .xlsx file is parsed directly from its ZIP/XML parts; CSV is also accepted.
 */
object ExcelQuestionImporter {

    private const val MAX_FILE_BYTES = 20 * 1024 * 1024

    data class Defaults(
        val ageBand: String = "7-9",
        val lang: String = "vi",
        val topic: String = "Tổng hợp",
        val difficulty: Int = 2
    )

    sealed class Result {
        data class Success(val questions: List<QuestionEntity>, val skippedRows: Int) : Result()
        data class Failure(val error: Error, val detail: String? = null) : Result()
    }

    enum class Error {
        EMPTY_FILE,
        UNSUPPORTED_OLD_XLS,
        UNREADABLE,
        NO_VALID_QUESTIONS
    }

    fun parse(input: InputStream, defaults: Defaults = Defaults()): Result {
        val bytes = try {
            input.use { readBounded(it) }
        } catch (e: Exception) {
            return Result.Failure(Error.UNREADABLE, e.message)
        }
        if (bytes.isEmpty()) return Result.Failure(Error.EMPTY_FILE)

        val rows = try {
            when {
                isZip(bytes) -> parseXlsx(bytes)
                isOleFormat(bytes) -> return Result.Failure(Error.UNSUPPORTED_OLD_XLS)
                else -> parseCsv(bytes.toString(Charsets.UTF_8))
            }
        } catch (e: Exception) {
            return Result.Failure(Error.UNREADABLE, e.message)
        }

        if (rows.isEmpty()) return Result.Failure(Error.NO_VALID_QUESTIONS)
        return buildQuestions(rows, defaults)
    }

    private fun readBounded(input: InputStream): ByteArray {
        val output = ByteArrayOutputStream()
        val buffer = ByteArray(8 * 1024)
        var total = 0
        while (true) {
            val read = input.read(buffer)
            if (read < 0) break
            total += read
            if (total > MAX_FILE_BYTES) throw IllegalArgumentException("File is too large")
            output.write(buffer, 0, read)
        }
        return output.toByteArray()
    }

    private fun isZip(bytes: ByteArray): Boolean =
        bytes.size >= 2 && bytes[0] == 'P'.code.toByte() && bytes[1] == 'K'.code.toByte()

    private fun isOleFormat(bytes: ByteArray): Boolean =
        bytes.size >= 4 &&
            bytes[0] == 0xD0.toByte() &&
            bytes[1] == 0xCF.toByte() &&
            bytes[2] == 0x11.toByte() &&
            bytes[3] == 0xE0.toByte()

    // --- XLSX ---

    private fun parseXlsx(bytes: ByteArray): List<List<String>> {
        var sharedStrings: List<String> = emptyList()
        var sheetBytes: ByteArray? = null

        ZipInputStream(ByteArrayInputStream(bytes)).use { zip ->
            var entry = zip.nextEntry
            while (entry != null) {
                when {
                    entry.name == "xl/sharedStrings.xml" -> sharedStrings = parseSharedStrings(zip.readBytes())
                    entry.name.startsWith("xl/worksheets/") &&
                        entry.name.endsWith(".xml") &&
                        sheetBytes == null -> sheetBytes = zip.readBytes()
                }
                zip.closeEntry()
                entry = zip.nextEntry
            }
        }

        val sheet = sheetBytes ?: throw IllegalArgumentException("No worksheet found")
        return parseSheet(sheet, sharedStrings)
    }

    private fun parseSharedStrings(bytes: ByteArray): List<String> {
        val document = buildDocument(bytes)
        val nodes = document.getElementsByTagName("si")
        val strings = ArrayList<String>(nodes.length)
        for (i in 0 until nodes.length) {
            val si = nodes.item(i) as Element
            val textNodes = si.getElementsByTagName("t")
            val text = StringBuilder()
            for (j in 0 until textNodes.length) {
                text.append(textNodes.item(j).textContent.orEmpty())
            }
            strings.add(cleanCellText(text.toString()))
        }
        return strings
    }

    private fun parseSheet(bytes: ByteArray, sharedStrings: List<String>): List<List<String>> {
        val document = buildDocument(bytes)
        val rowNodes = document.getElementsByTagName("row")
        val rows = mutableListOf<List<String>>()

        for (i in 0 until rowNodes.length) {
            val rowElement = rowNodes.item(i) as Element
            val cellNodes = rowElement.getElementsByTagName("c")
            val cells = mutableMapOf<Int, String>()
            var maxColumn = -1

            for (j in 0 until cellNodes.length) {
                val cell = cellNodes.item(j) as Element
                val column = columnIndex(cell.getAttribute("r"))
                if (column < 0) continue
                cells[column] = readCellValue(cell, sharedStrings)
                if (column > maxColumn) maxColumn = column
            }

            if (maxColumn >= 0) {
                val row = MutableList(maxColumn + 1) { "" }
                for ((column, value) in cells) row[column] = value
                if (row.any { it.isNotBlank() }) rows.add(row)
            }
        }
        return rows
    }

    private fun buildDocument(bytes: ByteArray) =
        DocumentBuilderFactory.newInstance().apply {
            isNamespaceAware = false
            runCatching {
                setFeature("http://apache.org/xml/features/disallow-doctype-decl", true)
                setFeature("http://xml.org/sax/features/external-general-entities", false)
                setFeature("http://xml.org/sax/features/external-parameter-entities", false)
            }
        }.newDocumentBuilder().parse(ByteArrayInputStream(bytes))

    private fun readCellValue(cell: Element, sharedStrings: List<String>): String {
        val type = cell.getAttribute("t")
        return when (type) {
            "s" -> {
                val index = cell.getElementsByTagName("v").item(0)?.textContent?.trim()?.toIntOrNull()
                sharedStrings.getOrNull(index ?: -1).orEmpty()
            }
            "inlineStr" -> {
                val inline = cell.getElementsByTagName("is").item(0) as? Element
                val textNodes = inline?.getElementsByTagName("t")
                if (textNodes == null) {
                    ""
                } else {
                    val text = StringBuilder()
                    for (i in 0 until textNodes.length) {
                        text.append(textNodes.item(i).textContent.orEmpty())
                    }
                    cleanCellText(text.toString())
                }
            }
            "b" -> if (cell.getElementsByTagName("v").item(0)?.textContent == "1") "TRUE" else "FALSE"
            "e" -> ""
            else -> cleanCellText(cell.getElementsByTagName("v").item(0)?.textContent.orEmpty())
        }
    }

    private fun columnIndex(reference: String): Int {
        var column = 0
        var i = 0
        while (i < reference.length && reference[i].isLetter()) {
            column = column * 26 + (reference[i].uppercaseChar() - 'A' + 1)
            i++
        }
        return if (column > 0) column - 1 else -1
    }

    private fun cleanCellText(value: String): String =
        value.replace("_x000D_", "\n").replace("\r\n", "\n")

    // --- CSV ---

    private fun parseCsv(text: String): List<List<String>> {
        val clean = text.removePrefix("\uFEFF")
        val firstLine = clean.lineSequence().firstOrNull().orEmpty()
        val delimiter = if (firstLine.count { it == ';' } > firstLine.count { it == ',' }) ';' else ','

        val rows = mutableListOf<List<String>>()
        val row = mutableListOf<String>()
        val cell = StringBuilder()
        var inQuotes = false
        var i = 0

        while (i < clean.length) {
            val char = clean[i]
            when {
                inQuotes && char == '"' && i + 1 < clean.length && clean[i + 1] == '"' -> {
                    cell.append('"')
                    i++
                }
                char == '"' -> inQuotes = !inQuotes
                !inQuotes && char == delimiter -> {
                    row.add(cell.toString())
                    cell.clear()
                }
                !inQuotes && (char == '\n' || char == '\r') -> {
                    if (char == '\r' && i + 1 < clean.length && clean[i + 1] == '\n') i++
                    row.add(cell.toString())
                    cell.clear()
                    if (row.any { it.isNotBlank() }) rows.add(row.toList())
                    row.clear()
                }
                else -> cell.append(char)
            }
            i++
        }

        if (cell.isNotEmpty() || row.isNotEmpty()) {
            row.add(cell.toString())
            if (row.any { it.isNotBlank() }) rows.add(row.toList())
        }
        return rows
    }

    // --- Mapping ---

    private fun buildQuestions(rows: List<List<String>>, defaults: Defaults): Result {
        val headerRow = rows.first()
        val headers = headerRow.map(::normalizeHeader)
        val hasHeader = headers.any {
            it in setOf("question", "cauhoi", "correct", "answer", "dapan", "description", "giaithich")
        }

        val columnMap = defaultColumnMap()
        if (hasHeader) columnMap.putAll(mapHeaders(headers))

        val questions = mutableListOf<QuestionEntity>()
        var skipped = 0

        for (row in if (hasHeader) rows.drop(1) else rows) {
            val questionText = columnMap["question"]
                ?.let { row.getOrNull(it) }
                ?.trim()
                .orEmpty()
            if (questionText.isEmpty() || normalizeHeader(questionText) in HEADER_WORDS) {
                skipped++
                continue
            }

            val options = linkedMapOf<String, String>()
            for (letter in OPTION_LETTERS) {
                val index = columnMap["option_$letter"] ?: continue
                val text = row.getOrNull(index)?.trim().orEmpty()
                if (text.isNotEmpty()) options[letter] = text
            }

            val correctRaw = columnMap["correct"]
                ?.let { row.getOrNull(it) }
                ?.trim()
                .orEmpty()
            val correctLetter = resolveCorrectLetter(correctRaw, options)
            if (options.size < 2 || correctLetter == null) {
                skipped++
                continue
            }
            val answer = options[correctLetter] ?: run {
                skipped++
                continue
            }

            val explanation = columnMap["explanation"]
                ?.let { row.getOrNull(it) }
                ?.trim()
                ?.takeIf { it.isNotEmpty() }
            val hint = columnMap["hint"]
                ?.let { row.getOrNull(it) }
                ?.trim()
                ?.takeIf { it.isNotEmpty() }
            val topic = columnMap["topic"]
                ?.let { row.getOrNull(it) }
                ?.trim()
                ?.takeIf { it.isNotEmpty() }
                ?: defaults.topic
            val ageBand = normalizeAgeBand(columnMap["age_band"]?.let { row.getOrNull(it) })
                ?: defaults.ageBand
            val lang = normalizeLang(columnMap["lang"]?.let { row.getOrNull(it) })
                ?: defaults.lang
            val difficulty = columnMap["difficulty"]
                ?.let { row.getOrNull(it)?.trim()?.toIntOrNull() }
                ?.coerceIn(1, 5)
                ?: defaults.difficulty

            val optionsJson = optionsToJson(options.values.toList())
            questions.add(
                QuestionEntity(
                    id = QuestionIdentity.stableId(questionText, optionsJson, answer, ageBand, lang),
                    type = "choice",
                    topic = topic,
                    ageBand = ageBand,
                    difficulty = difficulty,
                    lang = lang,
                    question = questionText,
                    optionsJson = optionsJson,
                    answer = answer,
                    hint = hint,
                    explanation = explanation,
                    active = 1
                )
            )
        }

        if (questions.isEmpty()) return Result.Failure(Error.NO_VALID_QUESTIONS)
        return Result.Success(questions, skipped)
    }

    private fun defaultColumnMap(): MutableMap<String, Int> = mutableMapOf(
        "question" to 1,
        "option_A" to 2,
        "option_B" to 3,
        "option_C" to 4,
        "option_D" to 5,
        "correct" to 6,
        "explanation" to 7
    )

    private fun mapHeaders(headers: List<String>): Map<String, Int> {
        val result = mutableMapOf<String, Int>()
        headers.forEachIndexed { index, header ->
            val key = when (header) {
                "question", "cauhoi", "noidung" -> "question"
                "a", "optiona", "dapana" -> "option_A"
                "b", "optionb", "dapanb" -> "option_B"
                "c", "optionc", "dapanc" -> "option_C"
                "d", "optiond", "dapand" -> "option_D"
                "correct", "answer", "dapan", "dapandung", "dung" -> "correct"
                "description", "explanation", "giaithich", "loigiai" -> "explanation"
                "hint", "goiy" -> "hint"
                "topic", "chude", "theloai" -> "topic"
                "ageband", "nhomtuoi", "doituoi" -> "age_band"
                "difficulty", "dokho" -> "difficulty"
                "lang", "language", "ngonngu" -> "lang"
                else -> null
            }
            if (key != null && !result.containsKey(key)) result[key] = index
        }
        return result
    }

    private fun resolveCorrectLetter(raw: String, options: Map<String, String>): String? {
        if (raw.isBlank()) return null

        options.entries.firstOrNull { it.value.equals(raw, ignoreCase = true) }?.let {
            return it.key
        }

        val upper = raw.uppercase(Locale.ROOT)
        if (upper.length == 1 && upper[0] in 'A'..'D' && options.containsKey(upper)) {
            return upper
        }

        val asNumber = upper.toIntOrNull()
        if (asNumber != null && asNumber in 1..4) {
            val letter = OPTION_LETTERS[asNumber - 1]
            if (options.containsKey(letter)) return letter
        }

        val firstLetter = upper.firstOrNull { it in 'A'..'D' }
        if (firstLetter != null && options.containsKey(firstLetter.toString())) {
            return firstLetter.toString()
        }
        return null
    }

    private fun normalizeHeader(value: String): String {
        val decomposed = Normalizer.normalize(value.trim().lowercase(Locale.ROOT), Normalizer.Form.NFD)
        return decomposed.replace(Regex("\\p{M}+"), "").replace(Regex("[^a-z0-9]"), "")
    }

    private fun normalizeAgeBand(raw: String?): String? {
        val value = raw?.replace("–", "-")?.replace(" ", "")?.replace("_", "") ?: return null
        return when {
            value == "4-6" || value == "46" -> "4-6"
            value == "7-9" || value == "79" -> "7-9"
            value == "10-12" || value == "1012" -> "10-12"
            else -> null
        }
    }

    private fun normalizeLang(raw: String?): String? {
        val value = normalizeHeader(raw ?: return null)
        return when {
            value in setOf("vi", "vn", "vietnamese", "tiengviet") -> "vi"
            value in setOf("en", "english", "tienganh") -> "en"
            else -> null
        }
    }

    private fun optionsToJson(options: List<String>): String =
        options.joinToString(prefix = "[", postfix = "]", separator = ",") {
            "\"${escapeJson(it)}\""
        }

    private fun escapeJson(value: String): String {
        val builder = StringBuilder(value.length)
        for (char in value) {
            when (char) {
                '\\' -> builder.append("\\\\")
                '"' -> builder.append("\\\"")
                '\n' -> builder.append("\\n")
                '\r' -> builder.append("\\r")
                '\t' -> builder.append("\\t")
                else -> if (char < ' ') {
                    builder.append("\\u").append(char.code.toString(16).padStart(4, '0'))
                } else {
                    builder.append(char)
                }
            }
        }
        return builder.toString()
    }

    private val OPTION_LETTERS = listOf("A", "B", "C", "D")

    private val HEADER_WORDS = setOf(
        "question", "cauhoi", "correct", "answer", "dapan",
        "description", "giaithich", "stt"
    )
}
