package com.zengaishua.app.ui.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.zengaishua.app.data.model.Question
import com.zengaishua.app.ui.viewmodel.PracticeMode
import com.zengaishua.app.ui.viewmodel.PracticeViewModel
import com.zengaishua.app.ui.viewmodel.StudyMode
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuestionScreen(
    viewModel: PracticeViewModel,
    onBack: () -> Unit,
    onShowQuestionList: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val question = uiState.currentQuestion

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "题目 ${uiState.currentIndex + 1}/${uiState.questions.size}"
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(onClick = onShowQuestionList) {
                        Icon(Icons.Default.List, contentDescription = "题目列表")
                    }
                    IconButton(onClick = { viewModel.toggleFavorite() }) {
                        Icon(
                            if (question?.isFavorite == true) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = "收藏",
                            tint = if (question?.isFavorite == true) Color(0xFFFFD700) else LocalContentColor.current
                        )
                    }
                    var showModeMenu by remember { mutableStateOf(false) }
                    IconButton(onClick = { showModeMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "更多")
                    }
                    DropdownMenu(
                        expanded = showModeMenu,
                        onDismissRequest = { showModeMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("练习模式") },
                            onClick = {
                                viewModel.setStudyMode(StudyMode.PRACTICE)
                                showModeMenu = false
                            },
                            leadingIcon = {
                                if (uiState.studyMode == StudyMode.PRACTICE) {
                                    Icon(Icons.Default.Check, contentDescription = null)
                                }
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("背题模式") },
                            onClick = {
                                viewModel.setStudyMode(StudyMode.MEMORIZE)
                                showModeMenu = false
                            },
                            leadingIcon = {
                                if (uiState.studyMode == StudyMode.MEMORIZE) {
                                    Icon(Icons.Default.Check, contentDescription = null)
                                }
                            }
                        )
                        Divider()
                        DropdownMenuItem(
                            text = { Text("顺序刷题") },
                            onClick = {
                                viewModel.setPracticeMode(PracticeMode.SEQUENTIAL)
                                showModeMenu = false
                            },
                            leadingIcon = {
                                if (uiState.practiceMode == PracticeMode.SEQUENTIAL) {
                                    Icon(Icons.Default.Check, contentDescription = null)
                                }
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("随机刷题") },
                            onClick = {
                                viewModel.setPracticeMode(PracticeMode.RANDOM)
                                showModeMenu = false
                            },
                            leadingIcon = {
                                if (uiState.practiceMode == PracticeMode.RANDOM) {
                                    Icon(Icons.Default.Check, contentDescription = null)
                                }
                            }
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        if (question == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("暂无题目")
            }
        } else {
            if (uiState.studyMode == StudyMode.MEMORIZE) {
                MemorizeContent(
                    question = question,
                    onPrevious = { viewModel.previousQuestion() },
                    onNext = { viewModel.nextQuestion() },
                    parseOptions = { viewModel.parseOptions(it) },
                    canGoPrevious = uiState.currentIndex > 0,
                    canGoNext = uiState.currentIndex < uiState.questions.size - 1,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                )
            } else {
                QuestionContent(
                    question = question,
                    selectedAnswers = uiState.selectedAnswers,
                    showAnswer = uiState.showAnswer,
                    isCorrect = uiState.isCorrect,
                    onAnswerSelected = { viewModel.toggleAnswer(it) },
                    onSubmit = { viewModel.submitAnswer() },
                    onNext = { viewModel.nextQuestion() },
                    onPrevious = { viewModel.previousQuestion() },
                    canGoPrevious = uiState.currentIndex > 0,
                    canGoNext = uiState.currentIndex < uiState.questions.size - 1,
                    parseOptions = { viewModel.parseOptions(it) },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                )
            }
        }
    }
}

@Composable
fun QuestionContent(
    question: Question,
    selectedAnswers: Set<String>,
    showAnswer: Boolean,
    isCorrect: Boolean?,
    onAnswerSelected: (String) -> Unit,
    onSubmit: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    canGoPrevious: Boolean,
    canGoNext: Boolean,
    parseOptions: (String) -> List<com.zengaishua.app.data.model.QuestionOption>,
    modifier: Modifier = Modifier
) {
    var dragOffset by remember { mutableStateOf(0f) }
    val scrollState = rememberScrollState()
    
    Box(
        modifier = modifier
            .pointerInput(canGoPrevious, canGoNext) { // 依赖导航状态
                detectHorizontalDragGestures(
                    onDragEnd = {
                        if (abs(dragOffset) > 100) { // 滑动阈值
                            when {
                                dragOffset < 0 && canGoNext -> onNext() // 向左滑动，下一题
                                dragOffset > 0 && canGoPrevious -> onPrevious() // 向右滑动，上一题
                            }
                        }
                        dragOffset = 0f
                    },
                    onHorizontalDrag = { change, dragAmount ->
                        // 只有在不是垂直滚动时才处理水平滑动
                        if (abs(dragAmount) > abs(change.position.y - change.previousPosition.y)) {
                            dragOffset += dragAmount
                        }
                    }
                )
            }
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {
        // 题目类型标签
        val typeText = when (question.type) {
            1 -> "单选题"
            2 -> "多选题"
            3 -> "判断题"
            else -> "未知"
        }
        Surface(
            color = MaterialTheme.colorScheme.primaryContainer,
            shape = MaterialTheme.shapes.small
        ) {
            Text(
                text = typeText,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                style = MaterialTheme.typography.labelMedium
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 题干
        Text(
            text = question.stem,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(24.dp))

        // 选项
        val options = parseOptions(question.optionsJson)
        options.forEach { option ->
            OptionItem(
                option = option,
                isSelected = selectedAnswers.contains(option.tag),
                showAnswer = showAnswer,
                correctAnswer = question.answer,
                onClick = { onAnswerSelected(option.tag) },
                enabled = !showAnswer
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        // 答题结果和解析
        if (showAnswer) {
            Spacer(modifier = Modifier.height(16.dp))
            
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (isCorrect == true)
                        MaterialTheme.colorScheme.primaryContainer
                    else
                        MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            if (isCorrect == true) Icons.Default.Check else Icons.Default.Close,
                            contentDescription = null,
                            tint = if (isCorrect == true)
                                MaterialTheme.colorScheme.onPrimaryContainer
                            else
                                MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isCorrect == true) "回答正确" else "回答错误",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("正确答案: ${question.answer}")
                }
            }

            if (question.explanation.isNotBlank()) {
                Spacer(modifier = Modifier.height(16.dp))
                Card {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "解析",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = question.explanation,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 按钮
        if (!showAnswer) {
            Button(
                onClick = onSubmit,
                modifier = Modifier.fillMaxWidth(),
                enabled = selectedAnswers.isNotEmpty()
            ) {
                Text("提交答案")
            }
        } else {
            Button(
                onClick = onNext,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("下一题")
            }
        }
        }
    }
}

@Composable
fun MemorizeContent(
    question: Question,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    parseOptions: (String) -> List<com.zengaishua.app.data.model.QuestionOption>,
    canGoPrevious: Boolean,
    canGoNext: Boolean,
    modifier: Modifier = Modifier
) {
    var dragOffset by remember { mutableStateOf(0f) }
    val scrollState = rememberScrollState()
    
    Box(
        modifier = modifier
            .pointerInput(canGoPrevious, canGoNext) { // 依赖导航状态
                detectHorizontalDragGestures(
                    onDragEnd = {
                        if (abs(dragOffset) > 100) { // 滑动阈值
                            when {
                                dragOffset < 0 && canGoNext -> onNext() // 向左滑动，下一题
                                dragOffset > 0 && canGoPrevious -> onPrevious() // 向右滑动，上一题
                            }
                        }
                        dragOffset = 0f
                    },
                    onHorizontalDrag = { change, dragAmount ->
                        // 只有在不是垂直滚动时才处理水平滑动
                        if (abs(dragAmount) > abs(change.position.y - change.previousPosition.y)) {
                            dragOffset += dragAmount
                        }
                    }
                )
            }
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {
        // 题目类型标签
        val typeText = when (question.type) {
            1 -> "单选题"
            2 -> "多选题"
            3 -> "判断题"
            else -> "未知"
        }
        Surface(
            color = MaterialTheme.colorScheme.primaryContainer,
            shape = MaterialTheme.shapes.small
        ) {
            Text(
                text = typeText,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                style = MaterialTheme.typography.labelMedium
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 题干
        Text(
            text = question.stem,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(24.dp))

        // 选项
        val options = parseOptions(question.optionsJson)
        val correctAnswers = question.answer.split(",").toSet()
        
        options.forEach { option ->
            val isCorrect = correctAnswers.contains(option.tag)
            MemorizeOptionItem(
                option = option,
                isCorrect = isCorrect
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 正确答案
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "正确答案: ${question.answer}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // 解析
        if (question.explanation.isNotBlank()) {
            Spacer(modifier = Modifier.height(16.dp))
            Card {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "解析",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = question.explanation,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 导航按钮
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = onPrevious,
                enabled = canGoPrevious,
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("上一题")
            }
            Button(
                onClick = onNext,
                enabled = canGoNext,
                modifier = Modifier.weight(1f)
            ) {
                Text("下一题")
                Spacer(modifier = Modifier.width(4.dp))
                Icon(Icons.Default.ArrowForward, contentDescription = null)
            }
        }
        }
    }
}

@Composable
fun MemorizeOptionItem(
    option: com.zengaishua.app.data.model.QuestionOption,
    isCorrect: Boolean
) {
    val borderColor = if (isCorrect) Color(0xFF4CAF50) else MaterialTheme.colorScheme.outline
    val backgroundColor = if (isCorrect) Color(0xFF4CAF50).copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = backgroundColor,
        border = BorderStroke(2.dp, borderColor)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${option.tag}.",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.width(32.dp)
            )
            Text(
                text = option.value,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )
            if (isCorrect) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = "正确答案",
                    tint = Color(0xFF4CAF50)
                )
            }
        }
    }
}

@Composable
fun OptionItem(
    option: com.zengaishua.app.data.model.QuestionOption,
    isSelected: Boolean,
    showAnswer: Boolean,
    correctAnswer: String,
    onClick: () -> Unit,
    enabled: Boolean
) {
    val correctAnswers = correctAnswer.split(",").toSet()
    val isCorrectOption = correctAnswers.contains(option.tag)
    
    val borderColor = when {
        showAnswer && isCorrectOption -> Color(0xFF4CAF50)
        showAnswer && isSelected && !isCorrectOption -> Color(0xFFF44336)
        isSelected -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.outline
    }

    val backgroundColor = when {
        showAnswer && isCorrectOption -> Color(0xFF4CAF50).copy(alpha = 0.1f)
        showAnswer && isSelected && !isCorrectOption -> Color(0xFFF44336).copy(alpha = 0.1f)
        isSelected -> MaterialTheme.colorScheme.primaryContainer
        else -> MaterialTheme.colorScheme.surface
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(
                selected = isSelected,
                onClick = onClick,
                enabled = enabled
            ),
        shape = MaterialTheme.shapes.medium,
        color = backgroundColor,
        border = BorderStroke(2.dp, borderColor)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${option.tag}.",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.width(32.dp)
            )
            Text(
                text = option.value,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}
