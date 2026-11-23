package com.example.data.ai

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val sender: MessageSender,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val actionType: AssistantActionType = AssistantActionType.NONE,
    val actionPayload: String? = null
)

enum class MessageSender {
    USER,
    ASSISTANT
}

enum class AssistantActionType {
    NONE,
    CREATE_TASK,
    CREATE_REMINDER,
    CREATE_BILL,
    CREATE_TRIP,
    VIEW_FINANCE,
    VIEW_BILLS,
    SEARCH_FLIGHTS
}
