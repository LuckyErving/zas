package com.zengaishua.app.ui.screen

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.zengaishua.app.data.model.QuestionBank
import com.zengaishua.app.ui.viewmodel.PracticeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PracticeScreen(
    viewModel: PracticeViewModel,
    onBankClick: (QuestionBank) -> Unit,
    onFavoritesClick: (String) -> Unit,
    onWrongQuestionsClick: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var showImportDialog by remember { mutableStateOf(false) }
    var bankName by remember { mutableStateOf("") }
    var selectedUri by remember { mutableStateOf<Uri?>(null) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedUri = it
            showImportDialog = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("刷题") },
                actions = {
                    IconButton(onClick = { launcher.launch("application/json") }) {
                        Icon(Icons.Default.Add, contentDescription = "导入习题")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (uiState.banks.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("暂无题库", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("点击右上角 + 导入习题", style = MaterialTheme.typography.bodyMedium)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.banks) { bank ->
                    BankCard(
                        bank = bank,
                        onClick = { onBankClick(bank) },
                        onFavoritesClick = { onFavoritesClick(bank.id) },
                        onWrongQuestionsClick = { onWrongQuestionsClick(bank.id) }
                    )
                }
            }
        }
    }

    if (showImportDialog && selectedUri != null) {
        AlertDialog(
            onDismissRequest = { showImportDialog = false },
            title = { Text("导入题库") },
            text = {
                OutlinedTextField(
                    value = bankName,
                    onValueChange = { bankName = it },
                    label = { Text("题库名称") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (bankName.isNotBlank()) {
                            viewModel.importQuestions(selectedUri!!, bankName)
                            showImportDialog = false
                            bankName = ""
                            selectedUri = null
                        }
                    }
                ) {
                    Text("导入")
                }
            },
            dismissButton = {
                TextButton(onClick = { showImportDialog = false }) {
                    Text("取消")
                }
            }
        )
    }

    if (uiState.isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }
}

@Composable
fun BankCard(
    bank: QuestionBank,
    onClick: () -> Unit,
    onFavoritesClick: () -> Unit,
    onWrongQuestionsClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
        ) {
            Text(
                text = bank.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("总题数: ${bank.totalCount}", style = MaterialTheme.typography.bodyMedium)
                    Text(
                        "已完成: ${bank.completedCount}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    val accuracy = if (bank.completedCount > 0) {
                        (bank.correctCount * 100.0 / bank.completedCount).toInt()
                    } else 0
                    Text("正确率: $accuracy%", style = MaterialTheme.typography.bodyMedium)
                }
                Column(horizontalAlignment = Alignment.End) {
                    TextButton(onClick = onFavoritesClick) {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = "收藏",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("收藏夹")
                    }
                    TextButton(onClick = onWrongQuestionsClick) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = "错题本",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("错题本")
                    }
                }
            }
        }
    }
}
