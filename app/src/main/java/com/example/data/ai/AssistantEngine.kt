package com.example.data.ai

import com.example.data.db.AppDatabase
import com.example.data.model.BillItem
import com.example.data.model.TaskItem
import com.example.data.model.TransactionItem
import kotlinx.coroutines.flow.firstOrNull
import java.text.NumberFormat
import java.util.Calendar
import java.util.Locale

class AssistantEngine(private val db: AppDatabase) {

    suspend fun processQuery(query: String): ChatMessage {
        val lower = query.lowercase().trim()
        val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("en-IN"))

        // 1. Check for Trip Planning Intent
        if (lower.contains("trip") || lower.contains("plan") && (lower.contains("goa") || lower.contains("manali") || lower.contains("kerala") || lower.contains("budget") || lower.contains("days") || lower.contains("day"))) {
            val destination = when {
                lower.contains("goa") -> "Goa"
                lower.contains("manali") -> "Manali"
                lower.contains("kerala") -> "Kerala"
                lower.contains("jaipur") -> "Jaipur"
                lower.contains("udaipur") -> "Udaipur"
                else -> "Goa"
            }
            val budget = if (lower.contains("15000") || lower.contains("15,000")) 15000.0 else 20000.0

            val reply = buildString {
                appendLine("✈️ **3-Day Trip Itinerary for $destination**")
                appendLine("🎯 Estimated Budget: ₹${budget.toInt()}")
                appendLine()
                appendLine("🗓️ **Day 1: Arrival & Coastal Vibe**")
                appendLine("• Morning: Check-in, brunch at local seaside cafe")
                appendLine("• Afternoon: Relax at Anjuna/Baga beach & water sports")
                appendLine("• Evening: Sunset view at Chapora Fort & beach shack dinner (Est: ₹2,200)")
                appendLine()
                appendLine("🗓️ **Day 2: Heritage, Culture & Cuisine**")
                appendLine("• Morning: Historic Basilica of Bom Jesus & Old Goa churches")
                appendLine("• Afternoon: Colorful streets of Fontainhas Latin Quarter, Panaji")
                appendLine("• Evening: Mandovi river sunset boat cruise (Est: ₹1,800)")
                appendLine()
                appendLine("🗓️ **Day 3: Serenity & Souvenirs**")
                appendLine("• Morning: Palolem beach walk & kayak rental")
                appendLine("• Afternoon: Spice plantation tour & authentic Goan lunch")
                appendLine("• Evening: Flea market shopping & departure (Est: ₹1,500)")
                appendLine()
                appendLine("💰 **Estimated Budget Breakdown:**")
                appendLine("• Stay (2 nights): ₹5,000")
                appendLine("• Travel/Transit: ₹4,500")
                appendLine("• Meals & Cafes: ₹3,500")
                appendLine("• Sightseeing & Leisure: ₹2,000")
                appendLine("Total: ₹15,000")
            }

            return ChatMessage(
                sender = MessageSender.ASSISTANT,
                text = reply,
                actionType = AssistantActionType.CREATE_TRIP,
                actionPayload = destination
            )
        }

        // 2. Check for Bill Reminder Intent
        if (lower.contains("remind") && lower.contains("bill")) {
            val billName = when {
                lower.contains("electricity") -> "Electricity Bill"
                lower.contains("water") -> "Water Bill"
                lower.contains("wifi") || lower.contains("internet") -> "Internet Bill"
                lower.contains("mobile") -> "Mobile Recharge"
                lower.contains("rent") -> "House Rent"
                lower.contains("credit card") -> "Credit Card Bill"
                else -> "Utility Bill"
            }

            val reply = "🔔 I've set up a reminder for your **$billName** scheduled for tomorrow at 9:00 AM. Would you like me to register it to your Bill Manager as well?"
            return ChatMessage(
                sender = MessageSender.ASSISTANT,
                text = reply,
                actionType = AssistantActionType.CREATE_BILL,
                actionPayload = billName
            )
        }

        // 3. Check for "Show upcoming bills"
        if (lower.contains("upcoming bill") || lower.contains("show me my bill") || lower.contains("my bills")) {
            val unpaid = db.billDao().getUnpaidBills().firstOrNull() ?: emptyList()
            if (unpaid.isEmpty()) {
                return ChatMessage(
                    sender = MessageSender.ASSISTANT,
                    text = "📄 You have **no unpaid bills** recorded in your dashboard right now.\n\nYou can tap below to record a bill to keep track of due dates.",
                    actionType = AssistantActionType.VIEW_BILLS
                )
            } else {
                val totalDue = unpaid.sumOf { it.amount }
                val summary = buildString {
                    appendLine("📋 You have **${unpaid.size} upcoming bill(s)** totaling ₹${totalDue.toInt()}:")
                    unpaid.take(5).forEach {
                        appendLine("• **${it.title}**: ₹${it.amount.toInt()} (${it.category})")
                    }
                }
                return ChatMessage(
                    sender = MessageSender.ASSISTANT,
                    text = summary,
                    actionType = AssistantActionType.VIEW_BILLS
                )
            }
        }

        // 4. Check for "How much did I spend this month"
        if (lower.contains("spend") || lower.contains("spent") || lower.contains("expense this month") || lower.contains("how much did i")) {
            val cal = Calendar.getInstance()
            cal.set(Calendar.DAY_OF_MONTH, 1)
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            val startOfMonth = cal.timeInMillis
            val endOfMonth = System.currentTimeMillis() + 86400000L

            val transactions = db.transactionDao().getTransactionsInRange(startOfMonth, endOfMonth).firstOrNull() ?: emptyList()
            val expenses = transactions.filter { it.type == "EXPENSE" }
            val income = transactions.filter { it.type == "INCOME" }

            val totalExpense = expenses.sumOf { it.amount }
            val totalIncome = income.sumOf { it.amount }

            if (transactions.isEmpty()) {
                return ChatMessage(
                    sender = MessageSender.ASSISTANT,
                    text = "📊 You haven't logged any transactions for this month yet. Start tracking your income and expenses in the Finance tab to see comprehensive analytics!",
                    actionType = AssistantActionType.VIEW_FINANCE
                )
            } else {
                val reply = buildString {
                    appendLine("💰 **This Month's Financial Summary:**")
                    appendLine("• Total Expenses: ₹${totalExpense.toInt()}")
                    appendLine("• Total Income: ₹${totalIncome.toInt()}")
                    appendLine("• Net Savings: ₹${(totalIncome - totalExpense).toInt()}")
                    appendLine()
                    appendLine("You can view category breakdown and set monthly spending limits in the Finance dashboard.")
                }
                return ChatMessage(
                    sender = MessageSender.ASSISTANT,
                    text = reply,
                    actionType = AssistantActionType.VIEW_FINANCE
                )
            }
        }

        // 5. Check for "Help me organize my tasks for this week"
        if (lower.contains("organize") && lower.contains("task") || lower.contains("tasks this week") || lower.contains("week plan")) {
            val allTasks = db.taskDao().getAllTasks().firstOrNull() ?: emptyList()
            val pending = allTasks.filter { !it.isCompleted }

            val reply = if (pending.isEmpty()) {
                "📝 You currently have no pending tasks recorded for this week. Would you like to create your top 3 weekly goals right now? Tap 'Add Task' or tell me what you need to get done!"
            } else {
                buildString {
                    appendLine("🎯 **Here is your task breakdown for the week:**")
                    appendLine("You have **${pending.size} active tasks**.")
                    pending.take(4).forEach {
                        appendLine("• [${it.priority.uppercase()}] ${it.title} (${it.category})")
                    }
                    appendLine("\nTip: Tackle high-priority tasks in your morning productivity window.")
                }
            }

            return ChatMessage(
                sender = MessageSender.ASSISTANT,
                text = reply,
                actionType = AssistantActionType.CREATE_TASK
            )
        }

        // 6. Check for Flight comparison
        if (lower.contains("flight") || lower.contains("airline") || lower.contains("airfare")) {
            val reply = "✈️ I can help you find and compare verified domestic and international flights with transparent baggage allowance, stops, duration, and direct booking links.\n\nHead over to the Travel section to choose your route and travel dates."
            return ChatMessage(
                sender = MessageSender.ASSISTANT,
                text = reply,
                actionType = AssistantActionType.SEARCH_FLIGHTS
            )
        }

        // 7. General AI query: If Gemini API Key is available, call live Gemini
        if (GeminiClient.isApiKeyConfigured()) {
            val pendingTasksCount = db.taskDao().getAllTasks().firstOrNull()?.count { !it.isCompleted } ?: 0
            val unpaidBillsCount = db.billDao().getUnpaidBills().firstOrNull()?.size ?: 0

            val contextPrompt = """
                User asks: "$query"
                Current User Context:
                - Pending Tasks: $pendingTasksCount
                - Unpaid Bills: $unpaidBillsCount
                Please provide a helpful, modern, structured response.
            """.trimIndent()

            val result = GeminiClient.generateResponse(contextPrompt)
            if (result.isSuccess) {
                return ChatMessage(
                    sender = MessageSender.ASSISTANT,
                    text = result.getOrNull() ?: "Here to assist you with your schedule, finances, and plans."
                )
            }
        }

        // Default intelligent fallback
        return ChatMessage(
            sender = MessageSender.ASSISTANT,
            text = "I'm your **AI Life Assistant**. I can help you with:\n\n• **Travel**: 'Plan a 3-day trip to Goa under ₹15,000'\n• **Reminders**: 'Remind me to pay electricity bill tomorrow'\n• **Finances**: 'How much did I spend this month?'\n• **Productivity**: 'Help me organize my tasks for this week'\n• **Bills**: 'Show me my upcoming bills'\n\nWhat would you like to accomplish today?"
        )
    }
}
