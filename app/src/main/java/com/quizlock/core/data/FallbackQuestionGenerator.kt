package com.quizlock.core.data

import kotlin.random.Random

object FallbackQuestionGenerator {

    fun generate(ageBand: String, lang: String = "vi"): QuestionEntity {
        return when (ageBand) {
            "4-6" -> generateBand4to6(lang)
            "7-9" -> generateBand7to9(lang)
            else -> generateBand10to12(lang)
        }
    }

    private fun generateBand4to6(lang: String): QuestionEntity {
        val isAddition = Random.nextBoolean()
        val a = Random.nextInt(1, 9)
        val b = Random.nextInt(1, 9)
        val id = "fb_46_${System.currentTimeMillis()}_${Random.nextInt(100, 999)}"

        return if (isAddition) {
            val ans = a + b
            val options = makeOptions(ans, 0, 20)
            val question = if (lang == "en") "What is $a + $b?" else "$a + $b bằng bao nhiêu?"
            val hint = if (lang == "en") "Count up $b fingers starting from $a." else "Hãy đếm thêm $b ngón tay bắt đầu từ $a nhé."
            val explanation = if (lang == "en") "$a plus $b equals $ans." else "$a cộng thêm $b bằng $ans."
            QuestionEntity(
                id = id,
                type = "choice",
                topic = if (lang == "en") "Math" else "toán",
                ageBand = "4-6",
                difficulty = 1,
                lang = lang,
                question = question,
                optionsJson = formatOptionsJson(options),
                answer = ans.toString(),
                hint = hint,
                explanation = explanation
            )
        } else {
            val max = maxOf(a, b)
            val min = minOf(a, b)
            val ans = max - min
            val options = makeOptions(ans, 0, 15)
            val question = if (lang == "en") "What is $max - $min?" else "$max - $min bằng bao nhiêu?"
            val hint = if (lang == "en") "Start at $max and count backward $min steps." else "Bắt đầu từ $max rồi đếm lùi $min bước nha."
            val explanation = if (lang == "en") "$max minus $min equals $ans." else "$max bớt đi $min còn $ans."
            QuestionEntity(
                id = id,
                type = "choice",
                topic = if (lang == "en") "Math" else "toán",
                ageBand = "4-6",
                difficulty = 1,
                lang = lang,
                question = question,
                optionsJson = formatOptionsJson(options),
                answer = ans.toString(),
                hint = hint,
                explanation = explanation
            )
        }
    }

    private fun generateBand7to9(lang: String): QuestionEntity {
        val isMult = Random.nextBoolean()
        val a = Random.nextInt(3, 10)
        val b = Random.nextInt(3, 10)
        val id = "fb_79_${System.currentTimeMillis()}_${Random.nextInt(100, 999)}"

        return if (isMult) {
            val ans = a * b
            val options = makeOptions(ans, 1, 100)
            val question = if (lang == "en") "What is $a × $b?" else "$a × $b bằng bao nhiêu?"
            val hint = if (lang == "en") "Recall the $a times table or $b times table." else "Nhớ lại bảng nhân $a hoặc bảng nhân $b nhé."
            val explanation = if (lang == "en") "$a multiplied by $b equals $ans." else "$a nhân $b bằng $ans."
            QuestionEntity(
                id = id,
                type = "choice",
                topic = if (lang == "en") "Math" else "toán",
                ageBand = "7-9",
                difficulty = 2,
                lang = lang,
                question = question,
                optionsJson = formatOptionsJson(options),
                answer = ans.toString(),
                hint = hint,
                explanation = explanation
            )
        } else {
            val prod = a * b
            val ans = a
            val options = makeOptions(ans, 1, 12)
            val question = if (lang == "en") "What is $prod ÷ $b?" else "$prod : $b bằng bao nhiêu?"
            val hint = if (lang == "en") "What number times $b equals $prod?" else "Số nào nhân với $b thì bằng $prod?"
            val explanation = if (lang == "en") "$prod divided by $b equals $ans because $ans × $b = $prod." else "$prod chia $b bằng $ans vì $ans × $b = $prod."
            QuestionEntity(
                id = id,
                type = "choice",
                topic = if (lang == "en") "Math" else "toán",
                ageBand = "7-9",
                difficulty = 2,
                lang = lang,
                question = question,
                optionsJson = formatOptionsJson(options),
                answer = ans.toString(),
                hint = hint,
                explanation = explanation
            )
        }
    }

    private fun generateBand10to12(lang: String): QuestionEntity {
        val mode = Random.nextInt(3)
        val id = "fb_1012_${System.currentTimeMillis()}_${Random.nextInt(100, 999)}"

        return when (mode) {
            0 -> {
                // Percentage
                val pct = listOf(10, 20, 25, 50).random()
                val base = listOf(80, 120, 160, 200, 400).random()
                val ans = (base * pct) / 100
                val options = makeOptions(ans, 5, 500)
                val question = if (lang == "en") "What is $pct% of $base?" else "$pct% của $base bằng bao nhiêu?"
                val hint = if (lang == "en") "Multiply $base by $pct and divide by 100." else "Nhân $base với $pct rồi chia cho 100."
                val explanation = if (lang == "en") "$pct% of $base = ($base × $pct) / 100 = $ans." else "$pct% của $base = ($base × $pct) / 100 = $ans."
                QuestionEntity(
                    id = id,
                    type = "choice",
                    topic = if (lang == "en") "Math" else "toán",
                    ageBand = "10-12",
                    difficulty = 3,
                    lang = lang,
                    question = question,
                    optionsJson = formatOptionsJson(options),
                    answer = ans.toString(),
                    hint = hint,
                    explanation = explanation
                )
            }
            1 -> {
                // Arithmetic order of operations
                val a = Random.nextInt(5, 15)
                val b = Random.nextInt(2, 6)
                val c = Random.nextInt(3, 8)
                val ans = a + b * c
                val options = makeOptions(ans, 10, 100)
                val question = if (lang == "en") "Calculate: $a + $b × $c = ?" else "Tính giá trị biểu thức: $a + $b × $c = ?"
                val hint = if (lang == "en") "Perform multiplication before addition: calculate $b × $c first." else "Thực hiện phép nhân trước phép cộng: tính $b × $c trước nhé."
                val explanation = if (lang == "en") "$a + ($b × $c) = $a + ${b * c} = $ans." else "$a + ($b × $c) = $a + ${b * c} = $ans."
                QuestionEntity(
                    id = id,
                    type = "choice",
                    topic = if (lang == "en") "Math" else "toán",
                    ageBand = "10-12",
                    difficulty = 3,
                    lang = lang,
                    question = question,
                    optionsJson = formatOptionsJson(options),
                    answer = ans.toString(),
                    hint = hint,
                    explanation = explanation
                )
            }
            else -> {
                // Number sequence
                val start = Random.nextInt(2, 10)
                val step = Random.nextInt(3, 8)
                val s1 = start
                val s2 = start + step
                val s3 = start + step * 2
                val ans = start + step * 3
                val options = makeOptions(ans, 1, 100)
                val question = if (lang == "en") "Find the next number in sequence: $s1, $s2, $s3, ...?" else "Tìm số tiếp theo trong dãy: $s1, $s2, $s3, ...?"
                val hint = if (lang == "en") "Notice the difference between consecutive numbers is $step." else "Khoảng cách giữa hai số liên tiếp là $step đơn vị."
                val explanation = if (lang == "en") "Each number increases by $step: $s3 + $step = $ans." else "Mỗi số tăng thêm $step đơn vị: $s3 + $step = $ans."
                QuestionEntity(
                    id = id,
                    type = "choice",
                    topic = if (lang == "en") "Logic" else "luận lý",
                    ageBand = "10-12",
                    difficulty = 3,
                    lang = lang,
                    question = question,
                    optionsJson = formatOptionsJson(options),
                    answer = ans.toString(),
                    hint = hint,
                    explanation = explanation
                )
            }
        }
    }

    private fun makeOptions(correct: Int, min: Int, max: Int): List<String> {
        val options = mutableSetOf(correct.toString())
        val deltas = listOf(-3, -2, -1, 1, 2, 3, 5, -5).shuffled()
        for (d in deltas) {
            val candidate = (correct + d).coerceIn(min, max)
            if (candidate != correct) {
                options.add(candidate.toString())
            }
            if (options.size == 4) break
        }
        var counter = 1
        while (options.size < 4) {
            options.add((correct + counter * 4).toString())
            counter++
        }
        return options.toList().shuffled()
    }

    private fun formatOptionsJson(options: List<String>): String {
        return options.joinToString(
            prefix = "[",
            postfix = "]",
            separator = ","
        ) { "\"${it.replace("\"", "\\\"")}\"" }
    }
}
