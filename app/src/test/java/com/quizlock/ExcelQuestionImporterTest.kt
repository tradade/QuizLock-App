package com.quizlock

import com.quizlock.core.data.ExcelQuestionImporter
import java.io.ByteArrayInputStream
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExcelQuestionImporterTest {

    private fun parseCsv(
        csv: String,
        defaults: ExcelQuestionImporter.Defaults = ExcelQuestionImporter.Defaults()
    ): ExcelQuestionImporter.Result =
        ExcelQuestionImporter.parse(ByteArrayInputStream(csv.toByteArray(Charsets.UTF_8)), defaults)

    @Test
    fun `parses the bundled xlsx template`() {
        val stream = requireNotNull(javaClass.classLoader?.getResourceAsStream("quizgate-template.xlsx")) {
            "Missing quizgate-template.xlsx test resource"
        }

        val result = ExcelQuestionImporter.parse(
            stream,
            ExcelQuestionImporter.Defaults(ageBand = "7-9", lang = "vi")
        )

        assertTrue("Expected success but was $result", result is ExcelQuestionImporter.Result.Success)
        val success = result as ExcelQuestionImporter.Result.Success
        assertEquals(8, success.questions.size)
        assertEquals(0, success.skippedRows)

        val first = success.questions.first()
        assertEquals("choice", first.type)
        assertEquals("Thủ đô của Việt Nam là thành phố nào?", first.question)
        assertEquals("Hà Nội", first.answer)
        assertTrue(first.optionsJson.contains("Hà Nội"))
        assertEquals("Hà Nội là thủ đô của nước CHXHCN Việt Nam.", first.explanation)
        assertEquals("7-9", first.ageBand)
        assertEquals("vi", first.lang)
        assertEquals("Tổng hợp", first.topic)
        assertEquals(2, first.difficulty)
        assertEquals(1, first.active)

        val trueFalse = success.questions[1]
        assertEquals("Sai", trueFalse.answer)
        assertTrue(trueFalse.optionsJson.contains("Đúng"))
        assertTrue(trueFalse.optionsJson.contains("Sai"))
    }

    @Test
    fun `parses csv with quoted commas and maps columns`() {
        val csv = """
            STT,Question,A,B,C,D,Correct,Description
            1,"Ba trạng thái phổ biến nhất của vật chất?","Rắn, Lỏng, Khí","Rắn, Lỏng, Plasma","Khí, Hơi, Chân không",,A,"Ba trạng thái là rắn, lỏng và khí."
            2,"Mặt trời quay quanh Trái Đất?",Đúng,Sai,,,B,Trái Đất quay quanh Mặt Trời.
        """.trimIndent()

        val result = parseCsv(csv)
        assertTrue(result is ExcelQuestionImporter.Result.Success)
        val success = result as ExcelQuestionImporter.Result.Success
        assertEquals(2, success.questions.size)

        val first = success.questions.first()
        assertEquals("Ba trạng thái phổ biến nhất của vật chất?", first.question)
        assertEquals("Rắn, Lỏng, Khí", first.answer)
        assertTrue(first.optionsJson.contains("Rắn, Lỏng, Khí"))
        assertEquals("Ba trạng thái là rắn, lỏng và khí.", first.explanation)
    }

    @Test
    fun `supports optional columns and numeric correct answers`() {
        val csv = """
            Question,A,B,C,D,Correct,Description,Topic,Age Band,Difficulty,Hint,Lang
            2 + 2 = ?,3,4,5,,2,2 + 2 equals 4,Math,4-6,1,Count on fingers,en
        """.trimIndent()

        val result = parseCsv(
            csv,
            ExcelQuestionImporter.Defaults(ageBand = "10-12", lang = "vi")
        )
        val success = result as ExcelQuestionImporter.Result.Success
        val question = success.questions.single()

        assertEquals("4", question.answer)
        assertEquals("Math", question.topic)
        assertEquals("4-6", question.ageBand)
        assertEquals("en", question.lang)
        assertEquals(1, question.difficulty)
        assertEquals("Count on fingers", question.hint)
        assertEquals("2 + 2 equals 4", question.explanation)
    }

    @Test
    fun `accepts the correct answer as option text`() {
        val csv = """
            Question,A,B,C,D,Correct,Description
            Mặt trời mọc ở đâu?,Đông,Tây,Nam,Bắc,Đông,Mặt trời mọc ở hướng Đông.
        """.trimIndent()

        val success = parseCsv(csv) as ExcelQuestionImporter.Result.Success
        assertEquals("Đông", success.questions.single().answer)
    }

    @Test
    fun `skips invalid rows and reports the count`() {
        val csv = """
            STT,Question,A,B,C,D,Correct,Description
            1,Câu hỏi hợp lệ,Một,Hai,,,A,Giải thích
            2,Không có đáp án,,,,,,
            3,Thiếu đáp án đúng,Một,Hai,,,,
        """.trimIndent()

        val success = parseCsv(csv) as ExcelQuestionImporter.Result.Success
        assertEquals(1, success.questions.size)
        assertEquals(2, success.skippedRows)
    }

    @Test
    fun `produces stable ids for re-import`() {
        val csv = """
            Question,A,B,C,D,Correct,Description
            Câu hỏi ổn định,Một,Hai,Ba,Bốn,A,Giải thích
        """.trimIndent()

        val first = parseCsv(csv) as ExcelQuestionImporter.Result.Success
        val second = parseCsv(csv) as ExcelQuestionImporter.Result.Success
        assertEquals(first.questions.single().id, second.questions.single().id)
    }

    @Test
    fun `rejects empty input`() {
        val result = parseCsv("")
        assertTrue(result is ExcelQuestionImporter.Result.Failure)
        assertEquals(
            ExcelQuestionImporter.Error.EMPTY_FILE,
            (result as ExcelQuestionImporter.Result.Failure).error
        )
    }

    @Test
    fun `rejects legacy xls files`() {
        val oleHeader = byteArrayOf(
            0xD0.toByte(), 0xCF.toByte(), 0x11.toByte(), 0xE0.toByte(),
            0xA1.toByte(), 0xB1.toByte(), 0x1A.toByte(), 0xE1.toByte()
        )
        val result = ExcelQuestionImporter.parse(ByteArrayInputStream(oleHeader))
        assertTrue(result is ExcelQuestionImporter.Result.Failure)
        assertEquals(
            ExcelQuestionImporter.Error.UNSUPPORTED_OLD_XLS,
            (result as ExcelQuestionImporter.Result.Failure).error
        )
    }

    @Test
    fun `rejects a file without valid questions`() {
        val result = parseCsv("STT,Question,A,B,C,D,Correct,Description\n1,,,,,,,")
        assertTrue(result is ExcelQuestionImporter.Result.Failure)
        assertEquals(
            ExcelQuestionImporter.Error.NO_VALID_QUESTIONS,
            (result as ExcelQuestionImporter.Result.Failure).error
        )
    }

    @Test
    fun `supports semicolon separated csv`() {
        val csv = """
            STT;Question;A;B;C;D;Correct;Description
            1;1 + 1 = ?;1;2;3;4;B;1 + 1 bằng 2.
        """.trimIndent()

        val success = parseCsv(csv) as ExcelQuestionImporter.Result.Success
        assertEquals("2", success.questions.single().answer)
    }
}
