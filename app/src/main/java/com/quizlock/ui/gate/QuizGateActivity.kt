package com.quizlock.ui.gate

import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quizlock.QuizGateApplication
import com.quizlock.core.data.ProfileEntity
import com.quizlock.core.data.QuestionEntity
import com.quizlock.core.i18n.AppLanguage
import com.quizlock.core.i18n.LocalStrings
import com.quizlock.service.QuizGateAccessibilityService
import com.quizlock.ui.components.PinPadModal
import com.quizlock.ui.theme.QuizAmberSecondary
import com.quizlock.ui.theme.QuizBlueLight
import com.quizlock.ui.theme.QuizBluePrimary
import com.quizlock.ui.theme.QuizGateTheme
import com.quizlock.ui.theme.QuizGreenLight
import com.quizlock.ui.theme.QuizGreenSuccess
import com.quizlock.ui.theme.QuizRedError
import com.quizlock.ui.theme.QuizRedLight
import com.quizlock.ui.theme.Slate100
import com.quizlock.ui.theme.Slate200
import com.quizlock.ui.theme.Slate400
import com.quizlock.ui.theme.Slate600
import com.quizlock.ui.theme.Slate700
import com.quizlock.ui.theme.Slate800
import com.quizlock.ui.theme.Slate900
import com.quizlock.ui.theme.TvFocusBorder
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONArray
import java.util.Locale
import kotlin.math.roundToInt

class QuizGateActivity : ComponentActivity(), TextToSpeech.OnInitListener {

    companion object {
        const val EXTRA_QUESTION_COUNT = "extra_question_count"
    }

    private var tts: TextToSpeech? = null
    private var isTtsReady = false
    private var optionSelectHandler: ((Int) -> Unit)? = null
    private var appLanguage = AppLanguage.VI

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        QuizGateAccessibilityService.isGateActive = true

        tts = TextToSpeech(this, this)

        setContent {
            val app = application as QuizGateApplication
            val repo = app.repository
            val appLanguage by repo.appLanguageFlow.collectAsState(initial = com.quizlock.core.i18n.AppLanguage.VI)
            val strings = com.quizlock.core.i18n.AppStrings.of(appLanguage)

            LaunchedEffect(appLanguage) {
                this@QuizGateActivity.appLanguage = appLanguage
            }

            androidx.compose.runtime.CompositionLocalProvider(
                com.quizlock.core.i18n.LocalStrings provides strings,
                com.quizlock.core.i18n.LocalAppLanguage provides appLanguage
            ) {
                QuizGateTheme {
                    // Prevent bypassing via back button
                    BackHandler {
                        // Do not allow dismissal by back press
                    }

                    QuizGateScreen(
                        repository = repo,
                        onSpeak = { text -> speakQuestion(text) },
                        onRegisterKeyHandler = { handler -> optionSelectHandler = handler },
                        onGateFinished = {
                            QuizGateAccessibilityService.isGateActive = false
                            QuizGateAccessibilityService.requestEvaluation()
                            finish()
                        }
                    )
                }
            }
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_1, KeyEvent.KEYCODE_NUMPAD_1 -> {
                optionSelectHandler?.invoke(0)
                return true
            }
            KeyEvent.KEYCODE_2, KeyEvent.KEYCODE_NUMPAD_2 -> {
                optionSelectHandler?.invoke(1)
                return true
            }
            KeyEvent.KEYCODE_3, KeyEvent.KEYCODE_NUMPAD_3 -> {
                optionSelectHandler?.invoke(2)
                return true
            }
            KeyEvent.KEYCODE_4, KeyEvent.KEYCODE_NUMPAD_4 -> {
                optionSelectHandler?.invoke(3)
                return true
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale.forLanguageTag("vi-VN"))
            isTtsReady = (result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED)
            if (!isTtsReady) {
                // Fallback to default
                tts?.setLanguage(Locale.getDefault())
                isTtsReady = true
            }
        }
    }

    private fun speakQuestion(text: String) {
        if (isTtsReady) {
            val locale = if (appLanguage == AppLanguage.EN) Locale.US else Locale.forLanguageTag("vi-VN")
            tts?.setLanguage(locale)
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "QUIZ_TTS")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        tts?.stop()
        tts?.shutdown()
        QuizGateAccessibilityService.isGateActive = false
        QuizGateAccessibilityService.requestEvaluation()
    }
}

@Composable
fun QuizGateScreen(
    repository: com.quizlock.core.data.QuizGateRepository,
    onSpeak: (String) -> Unit,
    onRegisterKeyHandler: (((Int) -> Unit)?) -> Unit,
    onGateFinished: () -> Unit
) {
    val s = LocalStrings.current
    val coroutineScope = rememberCoroutineScope()
    var currentProfile by remember { mutableStateOf<ProfileEntity?>(null) }
    var currentQuestion by remember { mutableStateOf<QuestionEntity?>(null) }
    var parsedOptions by remember { mutableStateOf<List<String>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    var attemptCount by remember { mutableIntStateOf(0) }
    val disabledOptions = remember { mutableStateListOf<String>() }
    var isVictory by remember { mutableStateOf(false) }
    var victoryExplanation by remember { mutableStateOf("") }
    var showParentPinModal by remember { mutableStateOf(false) }

    val shakeOffset = remember { Animatable(0f) }

    fun loadQuestionForActiveProfile() {
        coroutineScope.launch {
            isLoading = true
            attemptCount = 0
            disabledOptions.clear()
            isVictory = false

            val profile = repository.getActiveProfile()
            currentProfile = profile
            val ageBand = profile?.ageBand ?: "7-9"
            val profileId = profile?.id ?: 1L

            val q = repository.getNextQuestion(profileId, ageBand)
            currentQuestion = q
            parsedOptions = parseOptions(q.optionsJson)
            isLoading = false
        }
    }

    LaunchedEffect(Unit) {
        loadQuestionForActiveProfile()
    }

    fun handleOptionSelected(selected: String) {
        val q = currentQuestion ?: return
        val profile = currentProfile ?: return

        if (selected == q.answer) {
            // Correct answer!
            isVictory = true
            victoryExplanation = q.explanation ?: s.gateVictoryDefaultExplanation
            coroutineScope.launch {
                repository.recordAttempt(profile.id, q.id, true, attemptCount + 1, 5)
                repository.markQuestionSeen(profile.id, q.id)
                val ledger = repository.getTodayLedger(profile.id)
                repository.recordGateSolved(profile.id, ledger.watchedSeconds)
                delay(2200L) // Show celebratory screen for ~2 seconds
                onGateFinished()
            }
        } else {
            // Wrong answer
            attemptCount++
            disabledOptions.add(selected)
            coroutineScope.launch {
                // Shake effect
                shakeOffset.animateTo(12f, tween(50))
                shakeOffset.animateTo(-12f, tween(50))
                shakeOffset.animateTo(8f, tween(50))
                shakeOffset.animateTo(-8f, tween(50))
                shakeOffset.animateTo(0f, tween(50))

                repository.recordAttempt(profile.id, q.id, false, attemptCount, 5)

                if (attemptCount >= 3) {
                    // Swap to fresh question so child is never stuck
                    delay(800L)
                    loadQuestionForActiveProfile()
                }
            }
        }
    }

    DisposableEffect(parsedOptions, disabledOptions, isVictory) {
        onRegisterKeyHandler { index ->
            if (index in parsedOptions.indices) {
                val opt = parsedOptions[index]
                if (!disabledOptions.contains(opt) && !isVictory) {
                    handleOptionSelected(opt)
                }
            }
        }
        onDispose {
            onRegisterKeyHandler(null)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Slate900, Slate800, Color(0xFF172554))
                )
            )
            .testTag("quiz_gate_screen"),
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator(color = QuizAmberSecondary)
        } else if (isVictory) {
            // Victory Celebration Card
            VictoryCard(explanation = victoryExplanation)
        } else {
            val q = currentQuestion
            val profile = currentProfile

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .safeDrawingPadding()
                    .padding(20.dp)
                    .widthIn(max = 680.dp)
                    .offset { IntOffset(shakeOffset.value.roundToInt(), 0) },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Bar with Child profile info & Parent Override button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(QuizBlueLight),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = profile?.name?.take(1) ?: "K",
                                fontWeight = FontWeight.Bold,
                                color = QuizBluePrimary,
                                fontSize = 20.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "${s.gateQuizWith} ${profile?.name ?: s.gateFallbackChildName}",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                            Text(
                                text = s.gateContinueSubtitle,
                                color = Slate400,
                                fontSize = 13.sp
                            )
                        }
                    }

                    // Parent emergency unlock button
                    TextButton(
                        onClick = { showParentPinModal = true },
                        modifier = Modifier.testTag("parent_override_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = s.gateUnlockDesc,
                            tint = Slate400,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = s.gateParentShort,
                            color = Slate400,
                            fontSize = 13.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Question Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Slate800),
                    elevation = CardDefaults.cardElevation(8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Topic Badge & Audio Button
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = QuizBlueLight
                            ) {
                                Text(
                                    text = (q?.topic ?: s.gateTopicFallback).uppercase(),
                                    color = QuizBluePrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }

                            // TTS Button for voice read-aloud
                            IconButton(
                                onClick = { q?.question?.let { onSpeak(it) } },
                                modifier = Modifier
                                    .size(40.dp)
                                    .testTag("tts_speak_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.VolumeUp,
                                    contentDescription = s.gateReadQuestionDesc,
                                    tint = QuizAmberSecondary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = q?.question ?: "",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )

                        // Hint Card when attemptCount >= 2
                        AnimatedVisibility(visible = attemptCount >= 2 && !q?.hint.isNullOrEmpty()) {
                            Card(
                                modifier = Modifier
                                    .padding(top = 16.dp)
                                    .fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF422006)),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Lightbulb,
                                        contentDescription = s.gateHint,
                                        tint = QuizAmberSecondary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = s.gateHintPrefix.format(q?.hint),
                                        color = QuizAmberSecondary,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // 4 Choices in 2x2 Grid (Supports D-pad focus & numeric keys 1-4)
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val row1 = parsedOptions.take(2)
                    val row2 = parsedOptions.drop(2).take(2)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        row1.forEachIndexed { idx, opt ->
                            val labelLetter = if (idx == 0) "A" else "B"
                            val numberKey = if (idx == 0) "1" else "2"
                            QuizChoiceButton(
                                letter = labelLetter,
                                numberKey = numberKey,
                                text = opt,
                                isDisabled = disabledOptions.contains(opt),
                                onClick = { handleOptionSelected(opt) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        row2.forEachIndexed { idx, opt ->
                            val labelLetter = if (idx == 0) "C" else "D"
                            val numberKey = if (idx == 0) "3" else "4"
                            QuizChoiceButton(
                                letter = labelLetter,
                                numberKey = numberKey,
                                text = opt,
                                isDisabled = disabledOptions.contains(opt),
                                onClick = { handleOptionSelected(opt) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Attempt feedback text
                if (attemptCount == 1) {
                    Text(
                        text = s.gateWrongFirst,
                        color = QuizAmberSecondary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                } else if (attemptCount == 2) {
                    Text(
                        text = s.gateWrongSecond,
                        color = QuizAmberSecondary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // Parent PIN Modal
        if (showParentPinModal) {
            PinPadModal(
                repository = repository,
                title = s.gateParentModalTitle,
                subtitle = s.gateParentModalSubtitle,
                onPinSuccess = {
                    showParentPinModal = false
                    onGateFinished()
                },
                onDismiss = { showParentPinModal = false }
            )
        }
    }
}

@Composable
private fun QuizChoiceButton(
    letter: String,
    numberKey: String,
    text: String,
    isDisabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    Surface(
        onClick = { if (!isDisabled) onClick() },
        enabled = !isDisabled,
        interactionSource = interactionSource,
        shape = RoundedCornerShape(20.dp),
        color = if (isDisabled) Slate700 else Slate800,
        modifier = modifier
            .height(68.dp)
            .focusable(enabled = !isDisabled, interactionSource = interactionSource)
            .then(
                if (isFocused) Modifier.border(3.dp, TvFocusBorder, RoundedCornerShape(20.dp))
                else Modifier.border(1.dp, if (isDisabled) Slate700 else Slate600, RoundedCornerShape(20.dp))
            )
            .testTag("quiz_choice_$letter")
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(
                        if (isDisabled) QuizRedLight
                        else if (isFocused) TvFocusBorder
                        else QuizBlueLight
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$letter ($numberKey)",
                    fontWeight = FontWeight.Bold,
                    color = if (isDisabled) QuizRedError else Slate900,
                    fontSize = 12.sp
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = text,
                color = if (isDisabled) Slate400 else Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2
            )
        }
    }
}

@Composable
private fun VictoryCard(explanation: String) {
    val s = LocalStrings.current
    Card(
        modifier = Modifier
            .padding(32.dp)
            .widthIn(max = 480.dp),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Slate800),
        elevation = CardDefaults.cardElevation(12.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(32.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = s.gateVictoryDesc,
                tint = QuizGreenSuccess,
                modifier = Modifier.size(72.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = s.gateVictoryTitle,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = s.gateVictorySubtitle,
                style = MaterialTheme.typography.titleMedium,
                color = QuizGreenSuccess,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(16.dp))

            Surface(
                color = Slate900,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = explanation,
                    color = Slate200,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(16.dp),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                repeat(3) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = s.gateStarDesc,
                        tint = QuizAmberSecondary,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = s.gateReturning,
                color = Slate400,
                fontSize = 13.sp
            )
        }
    }
}

private fun parseOptions(optionsJson: String): List<String> {
    return try {
        val array = JSONArray(optionsJson)
        val list = mutableListOf<String>()
        for (i in 0 until array.length()) {
            list.add(array.getString(i))
        }
        list
    } catch (_: Exception) {
        listOf("A", "B", "C", "D")
    }
}
