package com.zengaishua.app.ui.screen

import android.content.Intent
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.zengaishua.app.data.model.QuestionBank
import com.zengaishua.app.ui.viewmodel.MineViewModel
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MineScreen(
    viewModel: MineViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var showDeleteDialog by remember { mutableStateOf<QuestionBank?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("我的") }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 学习统计
            item {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "学习统计",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        val totalQuestions = uiState.banks.sumOf { it.totalCount }
                        val completedQuestions = uiState.banks.sumOf { it.completedCount }
                        val correctQuestions = uiState.banks.sumOf { it.correctCount }
                        val accuracy = if (completedQuestions > 0) {
                            (correctQuestions * 100.0 / completedQuestions).toInt()
                        } else 0

                        StatItem("题库数量", "${uiState.banks.size}")
                        Divider(modifier = Modifier.padding(vertical = 8.dp))
                        StatItem("总题数", "$totalQuestions")
                        Divider(modifier = Modifier.padding(vertical = 8.dp))
                        StatItem("已完成", "$completedQuestions")
                        Divider(modifier = Modifier.padding(vertical = 8.dp))
                        StatItem("正确率", "$accuracy%")
                    }
                }
            }

            // 题库管理
            item {
                Text(
                    text = "题库管理",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            items(uiState.banks) { bank ->
                BankManagementCard(
                    bank = bank,
                    onExport = {
                        viewModel.selectBank(bank)
                        viewModel.exportData()
                    },
                    onDelete = {
                        showDeleteDialog = bank
                    }
                )
            }
        }
    }

    // 删除确认对话框
    showDeleteDialog?.let { bank ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("删除题库") },
            text = { Text("确定要删除题库「${bank.name}」吗?这将删除所有相关数据。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteBank(bank)
                        showDeleteDialog = null
                    }
                ) {
                    Text("删除", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) {
                    Text("取消")
                }
            }
        )
    }

    // 导出数据
    LaunchedEffect(uiState.exportResult) {
        uiState.exportResult?.let { data ->
            try {
                val fileName = "zas_${uiState.selectedBank?.name}_${System.currentTimeMillis()}.json"
                val file = File(context.cacheDir, fileName)
                file.writeText(data)

                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )

                val shareIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_STREAM, uri)
                    type = "application/json"
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(Intent.createChooser(shareIntent, "导出学习数据"))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}

@Composable
fun StatItem(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun BankManagementCard(
    bank: QuestionBank,
    onExport: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = bank.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "总题数: ${bank.totalCount} | 已完成: ${bank.completedCount}",
                style = MaterialTheme.typography.bodyMedium
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onExport) {
                    Icon(
                        Icons.Default.Share,
                        contentDescription = "导出",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("导出数据")
                }
                Spacer(modifier = Modifier.width(8.dp))
                TextButton(
                    onClick = onDelete,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "删除",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("删除")
                }
            }
        }
    }
}
