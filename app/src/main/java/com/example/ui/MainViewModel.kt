package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ai.AssistantActionType
import com.example.data.ai.AssistantEngine
import com.example.data.ai.ChatMessage
import com.example.data.ai.MessageSender
import com.example.data.db.AppDatabase
import com.example.data.model.AnalyticsLogItem
import com.example.data.model.BillItem
import com.example.data.model.ReminderItem
import com.example.data.model.SavingsGoal
import com.example.data.model.SubscriptionRecord
import com.example.data.model.TaskItem
import com.example.data.model.TransactionItem
import com.example.data.model.TripPlan
import com.example.data.payment.PaymentManager
import com.example.data.payment.PlanDetail
import com.example.data.reminder.NotificationHelper
import com.example.data.reminder.ReminderScheduler
import com.example.data.reminder.WorkManagerScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val assistantEngine = AssistantEngine(db)

    val tasks: StateFlow<List<TaskItem>> = db.taskDao().getAllTasks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val reminders: StateFlow<List<ReminderItem>> = db.reminderDao().getAllReminders()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val transactions: StateFlow<List<TransactionItem>> = db.transactionDao().getAllTransactions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val bills: StateFlow<List<BillItem>> = db.billDao().getAllBills()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val trips: StateFlow<List<TripPlan>> = db.tripDao().getAllTrips()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val goals: StateFlow<List<SavingsGoal>> = db.savingsGoalDao().getAllGoals()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val subscription: StateFlow<SubscriptionRecord?> = db.subscriptionDao().getSubscription()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val analyticsLogs: StateFlow<List<AnalyticsLogItem>> = db.analyticsDao().getRecentLogs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedDateMillis = MutableStateFlow(System.currentTimeMillis())
    val selectedDateMillis: StateFlow<Long> = _selectedDateMillis.asStateFlow()

    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(listOf(
        ChatMessage(
            sender = MessageSender.ASSISTANT,
            text = "👋 Hello! I am your **AI Life Assistant**.\n\nI can help you coordinate trips, organize your tasks, remind you of due bills, and analyze your finances. What can I do for you today?"
        )
    ))
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _isGeneratingAi = MutableStateFlow(false)
    val isGeneratingAi: StateFlow<Boolean> = _isGeneratingAi.asStateFlow()

    private val _userMonthlyBudget = MutableStateFlow(40000.0)
    val userMonthlyBudget: StateFlow<Double> = _userMonthlyBudget.asStateFlow()

    init {
        NotificationHelper.initNotificationChannels(application)
        WorkManagerScheduler.enqueuePeriodicReminderCheck(application)
        viewModelScope.launch {
            db.analyticsDao().insertLog(
                AnalyticsLogItem(eventType = "APP_OPENED", details = "User opened AI Life Assistant")
            )
        }
    }

    fun setSelectedDate(millis: Long) {
        _selectedDateMillis.value = millis
    }

    fun setMonthlyBudget(budget: Double) {
        _userMonthlyBudget.value = budget
    }

    // --- Task Actions ---
    fun addTask(
        title: String,
        description: String,
        category: String,
        priority: String,
        dueDateMillis: Long,
        dueTime: String,
        reminderEnabled: Boolean
    ) {
        viewModelScope.launch {
            val task = TaskItem(
                title = title.trim(),
                description = description.trim(),
                category = category,
                priority = priority,
                dueDateMillis = dueDateMillis,
                dueTime = dueTime,
                reminderEnabled = reminderEnabled
            )
            val taskId = db.taskDao().insertTask(task)

            if (reminderEnabled && dueDateMillis > System.currentTimeMillis()) {
                val reminderItem = ReminderItem(
                    title = "Task Due: $title",
                    notes = description.ifEmpty { "Category: $category" },
                    triggerTimeMillis = dueDateMillis,
                    associatedTaskId = taskId
                )
                val reminderId = db.reminderDao().insertReminder(reminderItem)
                ReminderScheduler.scheduleReminder(
                    context = getApplication(),
                    id = reminderId,
                    triggerMillis = dueDateMillis,
                    title = "Task Reminder: $title",
                    message = description.ifEmpty { "High priority task scheduled for now." }
                )
                WorkManagerScheduler.scheduleOneTimeReminder(
                    context = getApplication(),
                    id = reminderId,
                    triggerMillis = dueDateMillis,
                    title = "Task Reminder: $title",
                    message = description.ifEmpty { "Priority: $priority • Category: $category" },
                    isBill = false
                )
            }

            db.analyticsDao().insertLog(
                AnalyticsLogItem(eventType = "TASK_CREATED", details = "Task: $title, Priority: $priority")
            )
        }
    }

    fun toggleTaskComplete(task: TaskItem) {
        viewModelScope.launch {
            db.taskDao().updateTask(task.copy(isCompleted = !task.isCompleted))
        }
    }

    fun deleteTask(task: TaskItem) {
        viewModelScope.launch {
            db.taskDao().deleteTask(task)
        }
    }

    // --- Reminder Actions ---
    fun addReminder(
        title: String,
        notes: String,
        triggerTimeMillis: Long,
        recurrence: String = "None"
    ) {
        viewModelScope.launch {
            val reminder = ReminderItem(
                title = title.trim(),
                notes = notes.trim(),
                triggerTimeMillis = triggerTimeMillis,
                recurrence = recurrence
            )
            val id = db.reminderDao().insertReminder(reminder)
            ReminderScheduler.scheduleReminder(
                context = getApplication(),
                id = id,
                triggerMillis = triggerTimeMillis,
                title = title,
                message = notes.ifEmpty { "Scheduled Reminder" }
            )
            WorkManagerScheduler.scheduleOneTimeReminder(
                context = getApplication(),
                id = id,
                triggerMillis = triggerTimeMillis,
                title = title,
                message = notes.ifEmpty { "Scheduled Reminder" },
                isBill = false
            )
            db.analyticsDao().insertLog(
                AnalyticsLogItem(eventType = "REMINDER_CREATED", details = "Reminder: $title")
            )
        }
    }

    fun toggleReminderComplete(reminder: ReminderItem) {
        viewModelScope.launch {
            db.reminderDao().updateReminder(reminder.copy(isCompleted = !reminder.isCompleted))
        }
    }

    fun deleteReminder(reminder: ReminderItem) {
        viewModelScope.launch {
            ReminderScheduler.cancelReminder(getApplication(), reminder.id)
            WorkManagerScheduler.cancelReminder(getApplication(), reminder.id)
            db.reminderDao().deleteReminder(reminder)
        }
    }

    // --- Transaction Actions ---
    fun addTransaction(
        title: String,
        amount: Double,
        type: String,
        category: String,
        dateMillis: Long = System.currentTimeMillis(),
        paymentMethod: String = "UPI",
        notes: String = ""
    ) {
        viewModelScope.launch {
            val transaction = TransactionItem(
                title = title.trim(),
                amount = amount,
                type = type,
                category = category,
                dateMillis = dateMillis,
                paymentMethod = paymentMethod,
                notes = notes.trim()
            )
            db.transactionDao().insertTransaction(transaction)
            db.analyticsDao().insertLog(
                AnalyticsLogItem(eventType = "TRANSACTION_LOGGED", details = "$type: ₹$amount ($category)")
            )
        }
    }

    fun deleteTransaction(transaction: TransactionItem) {
        viewModelScope.launch {
            db.transactionDao().deleteTransaction(transaction)
        }
    }

    // --- Bill Actions ---
    fun addBill(
        title: String,
        provider: String,
        amount: Double,
        dueDateMillis: Long,
        category: String,
        recurrence: String,
        reminderSet: Boolean
    ) {
        viewModelScope.launch {
            val bill = BillItem(
                title = title.trim(),
                provider = provider.trim(),
                amount = amount,
                dueDateMillis = dueDateMillis,
                category = category,
                recurrence = recurrence,
                reminderSet = reminderSet
            )
            val billId = db.billDao().insertBill(bill)

            if (reminderSet && dueDateMillis > System.currentTimeMillis()) {
                val reminderId = db.reminderDao().insertReminder(
                    ReminderItem(
                        title = "Bill Due: $title",
                        notes = "Amount: ₹${amount.toInt()} due today",
                        triggerTimeMillis = dueDateMillis,
                        associatedBillId = billId
                    )
                )
                ReminderScheduler.scheduleReminder(
                    context = getApplication(),
                    id = reminderId,
                    triggerMillis = dueDateMillis,
                    title = "Upcoming Bill Due: $title",
                    message = "₹${amount.toInt()} due today for $provider",
                    isBill = true
                )
                WorkManagerScheduler.scheduleOneTimeReminder(
                    context = getApplication(),
                    id = reminderId,
                    triggerMillis = dueDateMillis,
                    title = "Upcoming Bill Due: $title",
                    message = "₹${amount.toInt()} due today for $provider",
                    isBill = true
                )
            }

            db.analyticsDao().insertLog(
                AnalyticsLogItem(eventType = "BILL_ADDED", details = "Bill: $title, Amount: ₹$amount")
            )
        }
    }

    fun markBillPaid(bill: BillItem, paymentRef: String = "UPI-PAID") {
        viewModelScope.launch {
            db.billDao().updateBill(
                bill.copy(
                    isPaid = true,
                    paidDateMillis = System.currentTimeMillis(),
                    paymentRef = paymentRef
                )
            )
            // Log as expense automatically
            db.transactionDao().insertTransaction(
                TransactionItem(
                    title = "Bill Payment: ${bill.title}",
                    amount = bill.amount,
                    type = "EXPENSE",
                    category = "Utilities",
                    dateMillis = System.currentTimeMillis(),
                    paymentMethod = "UPI",
                    notes = "Paid via $paymentRef"
                )
            )
            db.analyticsDao().insertLog(
                AnalyticsLogItem(eventType = "BILL_PAID", details = "Paid: ${bill.title}, Ref: $paymentRef")
            )
        }
    }

    fun deleteBill(bill: BillItem) {
        viewModelScope.launch {
            WorkManagerScheduler.cancelReminder(getApplication(), bill.id)
            db.billDao().deleteBill(bill)
        }
    }

    // --- Trip Actions ---
    fun addTrip(
        destination: String,
        startDateMillis: Long,
        endDateMillis: Long,
        totalBudget: Double,
        flightCost: Double = 0.0,
        hotelCost: Double = 0.0,
        dailyCost: Double = 0.0,
        itineraryText: String = "",
        checklistJson: String = ""
    ) {
        viewModelScope.launch {
            val trip = TripPlan(
                destination = destination.trim(),
                startDateMillis = startDateMillis,
                endDateMillis = endDateMillis,
                totalBudget = totalBudget,
                estimatedFlightCost = flightCost,
                estimatedHotelCost = hotelCost,
                estimatedDailyCost = dailyCost,
                itineraryText = itineraryText,
                checklistJson = checklistJson
            )
            db.tripDao().insertTrip(trip)
            db.analyticsDao().insertLog(
                AnalyticsLogItem(eventType = "TRIP_PLANNED", details = "Destination: $destination, Budget: ₹$totalBudget")
            )
        }
    }

    fun deleteTrip(trip: TripPlan) {
        viewModelScope.launch {
            db.tripDao().deleteTrip(trip)
        }
    }

    // --- Savings Goal Actions ---
    fun addGoal(title: String, targetAmount: Double, currentAmount: Double, targetDateMillis: Long, category: String) {
        viewModelScope.launch {
            val goal = SavingsGoal(
                title = title.trim(),
                targetAmount = targetAmount,
                currentAmount = currentAmount,
                targetDateMillis = targetDateMillis,
                category = category
            )
            db.savingsGoalDao().insertGoal(goal)
        }
    }

    fun updateGoalProgress(goal: SavingsGoal, addedAmount: Double) {
        viewModelScope.launch {
            val newAmount = (goal.currentAmount + addedAmount).coerceAtMost(goal.targetAmount * 2)
            db.savingsGoalDao().updateGoal(goal.copy(currentAmount = newAmount))
        }
    }

    fun deleteGoal(goal: SavingsGoal) {
        viewModelScope.launch {
            db.savingsGoalDao().deleteGoal(goal)
        }
    }

    // --- Assistant Messaging ---
    fun sendUserMessage(text: String) {
        if (text.isBlank()) return
        val userMsg = ChatMessage(sender = MessageSender.USER, text = text)
        _chatMessages.value = _chatMessages.value + userMsg
        _isGeneratingAi.value = true

        viewModelScope.launch {
            val responseMsg = assistantEngine.processQuery(text)
            _chatMessages.value = _chatMessages.value + responseMsg
            _isGeneratingAi.value = false
        }
    }

    fun clearChat() {
        _chatMessages.value = listOf(
            ChatMessage(
                sender = MessageSender.ASSISTANT,
                text = "Chat cleared. What can I help you coordinate or organize?"
            )
        )
    }

    // --- Payment & Subscription Actions ---
    suspend fun processSubscriptionPurchase(plan: PlanDetail, paymentMethod: String, transactionRef: String) {
        PaymentManager.verifyAndActivateSubscription(
            context = getApplication(),
            db = db,
            plan = plan,
            paymentMethod = paymentMethod,
            transactionRef = transactionRef
        )
    }

    suspend fun restoreSubscription(): Boolean {
        return PaymentManager.restorePurchases(db)
    }

    // --- Data Reset (for settings & testing clean state) ---
    fun clearAllUserData() {
        viewModelScope.launch {
            db.taskDao().deleteAll()
            db.reminderDao().deleteAll()
            db.transactionDao().deleteAll()
            db.billDao().deleteAll()
            db.tripDao().deleteAll()
            db.savingsGoalDao().deleteAll()
            clearChat()
        }
    }
}
