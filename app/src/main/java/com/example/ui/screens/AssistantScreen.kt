package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ai.AssistantActionType
import com.example.data.ai.ChatMessage
import com.example.data.ai.MessageSender
import com.example.ui.MainViewModel

@Composable
fun AssistantScreen(
    viewModel: MainViewModel,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val messages by viewModel.chatMessages.collectAsState()
    val isGenerating by viewModel.isGeneratingAi.collectAsState()
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    val quickPrompts = listOf(
        "Plan a three-day trip to Goa under ₹15,000.",
        "Remind me to pay my electricity bill tomorrow.",
        "Help me organize my tasks for this week.",
        "Show me my upcoming bills.",
        "Help me compare flight prices.",
        "How much did I spend this month?"
    )

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        // Assistant Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "AI Personal Assistant",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Intelligent travel, task & money assistant",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            IconButton(
                onClick = { viewModel.clearChat() },
                modifier = Modifier.testTag("clear_chat_button")
            ) {
                Icon(
                    Icons.Default.DeleteSweep,
                    contentDescription = "Clear Chat",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Quick Suggestions Horizontal Row
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
        ) {
            items(quickPrompts) { prompt ->
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .clickable { viewModel.sendUserMessage(prompt) }
                ) {
                    Text(
                        text = prompt,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Chat Messages List
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(messages, key = { it.id }) { msg ->
                ChatBubble(
                    message = msg,
                    onActionClick = { action, payload ->
                        when (action) {
                            AssistantActionType.CREATE_TRIP -> {
                                viewModel.addTrip(
                                    destination = payload ?: "Goa",
                                    startDateMillis = System.currentTimeMillis() + 86400000L * 7,
                                    endDateMillis = System.currentTimeMillis() + 86400000L * 10,
                                    totalBudget = 15000.0,
                                    flightCost = 4500.0,
                                    hotelCost = 5000.0,
                                    dailyCost = 3500.0,
                                    itineraryText = "Day 1: Beaches & Shacks\nDay 2: Fontainhas & Latin Quarter\nDay 3: River Cruise & Flea Market",
                                    checklistJson = ""
                                )
                                onNavigate("travel")
                            }
                            AssistantActionType.CREATE_BILL -> {
                                val billTitle = payload ?: "Utility Bill"
                                viewModel.addBill(
                                    title = billTitle,
                                    provider = "Biller",
                                    amount = 1250.0,
                                    dueDateMillis = System.currentTimeMillis() + 86400000L,
                                    category = "Utilities",
                                    recurrence = "Monthly",
                                    reminderSet = true
                                )
                                onNavigate("bills")
                            }
                            AssistantActionType.VIEW_BILLS -> onNavigate("bills")
                            AssistantActionType.VIEW_FINANCE -> onNavigate("finance")
                            AssistantActionType.SEARCH_FLIGHTS -> onNavigate("travel")
                            AssistantActionType.CREATE_TASK -> onNavigate("tasks")
                            else -> {}
                        }
                    }
                )
            }

            if (isGenerating) {
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 8.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "AI Life Assistant is thinking...",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(10.dp))
            }
        }

        // Input Field Bar
        Surface(
            tonalElevation = 6.dp,
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth().border(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = { Text("Ask anything or give an instruction...", fontSize = 14.sp) },
                    maxLines = 3,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("ai_chat_input_field")
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = {
                        val textToSend = inputText
                        if (textToSend.isNotBlank()) {
                            inputText = ""
                            viewModel.sendUserMessage(textToSend)
                        }
                    },
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                        .testTag("ai_chat_send_button")
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ChatBubble(
    message: ChatMessage,
    onActionClick: (AssistantActionType, String?) -> Unit
) {
    val isUser = message.sender == MessageSender.USER

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Top
    ) {
        if (!isUser) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Column(
            modifier = Modifier.widthIn(max = 300.dp),
            horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
        ) {
            Surface(
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = if (isUser) 16.dp else 4.dp,
                    bottomEnd = if (isUser) 4.dp else 16.dp
                ),
                color = if (isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                tonalElevation = if (isUser) 0.dp else 2.dp,
                modifier = Modifier.border(
                    1.dp,
                    if (isUser) Color.Transparent else MaterialTheme.colorScheme.outline,
                    RoundedCornerShape(16.dp)
                )
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = message.text,
                        fontSize = 14.sp,
                        color = if (isUser) Color.White else MaterialTheme.colorScheme.onSurface,
                        lineHeight = 20.sp
                    )

                    // Embedded Action Card
                    if (message.actionType != AssistantActionType.NONE) {
                        Spacer(modifier = Modifier.height(10.dp))
                        when (message.actionType) {
                            AssistantActionType.CREATE_TRIP -> {
                                Button(
                                    onClick = { onActionClick(message.actionType, message.actionPayload) },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Save Trip to Planner →", fontSize = 12.sp)
                                }
                            }
                            AssistantActionType.CREATE_BILL -> {
                                Button(
                                    onClick = { onActionClick(message.actionType, message.actionPayload) },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Add Bill Due to Manager →", fontSize = 12.sp)
                                }
                            }
                            AssistantActionType.VIEW_BILLS -> {
                                OutlinedButton(
                                    onClick = { onActionClick(message.actionType, null) },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Open Bill Manager →", fontSize = 12.sp)
                                }
                            }
                            AssistantActionType.VIEW_FINANCE -> {
                                OutlinedButton(
                                    onClick = { onActionClick(message.actionType, null) },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Open Finance Dashboard →", fontSize = 12.sp)
                                }
                            }
                            AssistantActionType.SEARCH_FLIGHTS -> {
                                Button(
                                    onClick = { onActionClick(message.actionType, null) },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Compare Flights Now →", fontSize = 12.sp)
                                }
                            }
                            AssistantActionType.CREATE_TASK -> {
                                Button(
                                    onClick = { onActionClick(message.actionType, null) },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Organize Tasks →", fontSize = 12.sp)
                                }
                            }
                            else -> {}
                        }
                    }
                }
            }
        }
    }
}
