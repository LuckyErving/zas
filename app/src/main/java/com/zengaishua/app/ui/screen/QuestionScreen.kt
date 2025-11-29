package com.zengaishua.app.ui.screen

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.zengaishua.app.data.model.Question
import com.zengaishua.app.ui.viewmodel.PracticeMode
import com.zengaishua.app.ui.viewmodel.PracticeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuestionScreen(
    viewModel: PracticeViewModel,
    onBack: () -> Unit
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
            QuestionContent(
                question = question,
                selectedAnswers = uiState.selectedAnswers,
                showAnswer = uiState.showAnswer,
                isCorrect = uiState.isCorrect,
                onAnswerSelected = { viewModel.toggleAnswer(it) },
                onSubmit = { viewModel.submitAnswer() },
                onNext = { viewModel.nextQuestion() },
                parseOptions = { viewModel.parseOptions(it) },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            )
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
    parseOptions: (String) -> List<com.zengaishua.app.data.model.QuestionOption>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
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
