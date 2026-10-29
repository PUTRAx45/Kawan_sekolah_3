package com.example.kawansekolah.ui.ai

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.kawansekolah.data.model.NotificationItem
import com.example.kawansekolah.data.model.UserRole
import com.example.kawansekolah.ui.ai.components.ChatMessageItem
import com.example.kawansekolah.ui.components.TopBarArea
import com.example.kawansekolah.ui.nilai.AcademicViewModel
import com.example.kawansekolah.ui.nilai.SubjectSyncManager
import kotlinx.coroutines.launch

@Composable
fun AiScreen(
    userName: String,
    userRole: UserRole = UserRole.SISWA,
    unreadCount: Int = 0,
    onNotificationClick: () -> Unit = {},
    onNewNotification: (NotificationItem) -> Unit = {},
    onUpdateGrade: (String, Double, Int, String, String, Int, Int, Int, Int) -> Unit = { _, _, _, _, _, _, _, _, _ -> },
    aiChatViewModel: AiChatViewModel = viewModel(),
    academicViewModel: AcademicViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by aiChatViewModel.uiState.collectAsState()

    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    val syncedSubjectName by SubjectSyncManager.selectedSubjectName.collectAsState()

    LaunchedEffect(userName, userRole) {
        aiChatViewModel.initChatSessionIfNeeded(userName, userRole)
    }

    LaunchedEffect(syncedSubjectName) {
        if (syncedSubjectName.isNotBlank() && syncedSubjectName != uiState.selectedSubjectName) {
            aiChatViewModel.setSelectedSubjectName(syncedSubjectName)
        }
    }

    // Auto-scroll to bottom when new chat message arrives
    LaunchedEffect(uiState.chatMessages.size) {
        if (uiState.chatMessages.isNotEmpty()) {
            coroutineScope.launch {
                listState.animateScrollToItem(uiState.chatMessages.size - 1)
            }
        }
    }

    val onSendMessage: (String) -> Unit = { query ->
        aiChatViewModel.sendMessage(
            context = context,
            query = query,
            userRole = userRole,
            onNewNotification = onNewNotification
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TopBarArea(
            userName = userName,
            unreadCount = unreadCount,
            onNotificationClick = onNotificationClick
        )

        // Quick Prompt Chips
        val quickPrompts = listOf(
            "📊 Ringkasan Nilai",
            "⚠️ Materi Terlemah",
            "💡 Saran Belajar",
            "📝 Latihan Soal"
        )

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(quickPrompts) { prompt ->
                SuggestionChip(
                    onClick = { onSendMessage(prompt) },
                    label = { Text(text = prompt, fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                    shape = RoundedCornerShape(16.dp)
                )
            }
        }

        // Chat Message List
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
        ) {
            items(uiState.chatMessages) { message ->
                ChatMessageItem(message = message)
            }

            if (uiState.isLoadingAi) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (uiState.waitingSeconds >= 5) "AI sedang memproses... (${uiState.waitingSeconds}s)" else "AI Scolarsia sedang berpikir...",
                            fontSize = 12.sp,
                            fontWeight = if (uiState.waitingSeconds >= 5) FontWeight.Bold else FontWeight.Medium,
                            color = if (uiState.waitingSeconds >= 5) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Retry Button when previous attempt failed
            if (!uiState.isLoadingAi && uiState.lastFailedQuery != null) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        OutlinedButton(
                            onClick = { uiState.lastFailedQuery?.let { onSendMessage(it) } },
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Coba Lagi Pertanyaan Sebelumnya", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Transparent Bottom Chat Input Bar
        Surface(
            color = Color.Transparent,
            tonalElevation = 0.dp,
            modifier = Modifier
                .fillMaxWidth()
                .imePadding()
                .padding(horizontal = 16.dp, vertical = 6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    modifier = Modifier.weight(1f),
                    placeholder = {
                        Text(
                            text = "Tanyakan apa saja kepada AI Scolarsia...",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    },
                    singleLine = true,
                    enabled = !uiState.isLoadingAi,
                    shape = RoundedCornerShape(28.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        if (inputText.isNotBlank() && !uiState.isLoadingAi) {
                            onSendMessage(inputText)
                            inputText = ""
                        }
                    },
                    enabled = inputText.isNotBlank() && !uiState.isLoadingAi,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            if (inputText.isNotBlank() && !uiState.isLoadingAi) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Kirim",
                        tint = if (inputText.isNotBlank() && !uiState.isLoadingAi) MaterialTheme.colorScheme.onPrimary
                               else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
