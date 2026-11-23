package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class TaskItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val category: String = "Personal", // Personal, Work, Home, Travel, Finance, Health, Shopping, Study
    val priority: String = "Medium", // Low, Medium, High
    val dueDateMillis: Long = 0L,
    val dueTime: String = "",
    val isCompleted: Boolean = false,
    val reminderEnabled: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "reminders")
data class ReminderItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val notes: String = "",
    val triggerTimeMillis: Long,
    val isCompleted: Boolean = false,
    val recurrence: String = "None", // None, Daily, Weekly, Monthly
    val associatedTaskId: Long? = null,
    val associatedBillId: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "transactions")
data class TransactionItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val amount: Double,
    val type: String, // "INCOME" or "EXPENSE"
    val category: String, // Salary, Food, Utilities, Shopping, Travel, Entertainment, Rent, Health, Other
    val dateMillis: Long = System.currentTimeMillis(),
    val paymentMethod: String = "UPI", // UPI, Card, Net Banking, Cash
    val notes: String = ""
)

@Entity(tableName = "bills")
data class BillItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val provider: String = "",
    val amount: Double,
    val dueDateMillis: Long,
    val category: String = "Utilities", // Electricity, Mobile, Internet, Rent, Credit Card, Water, Subscription
    val recurrence: String = "Monthly", // One-time, Monthly, Quarterly, Yearly
    val isPaid: Boolean = false,
    val paidDateMillis: Long? = null,
    val paymentRef: String? = null,
    val reminderSet: Boolean = true
)

@Entity(tableName = "trips")
data class TripPlan(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val destination: String,
    val startDateMillis: Long,
    val endDateMillis: Long,
    val totalBudget: Double,
    val estimatedFlightCost: Double = 0.0,
    val estimatedHotelCost: Double = 0.0,
    val estimatedDailyCost: Double = 0.0,
    val itineraryText: String = "",
    val checklistJson: String = "",
    val notes: String = "",
    val status: String = "Upcoming" // Upcoming, Active, Completed
)

@Entity(tableName = "savings_goals")
data class SavingsGoal(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val targetAmount: Double,
    val currentAmount: Double = 0.0,
    val targetDateMillis: Long = 0L,
    val category: String = "General"
)

@Entity(tableName = "subscription_record")
data class SubscriptionRecord(
    @PrimaryKey
    val id: Int = 1,
    val isProActive: Boolean = false,
    val planType: String = "Free", // Free, Pro Monthly, Pro Annual
    val purchaseDateMillis: Long = 0L,
    val validUntilMillis: Long = 0L,
    val orderId: String = "",
    val paymentMethod: String = "",
    val transactionRef: String = ""
)

@Entity(tableName = "analytics_log")
data class AnalyticsLogItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val eventType: String,
    val details: String,
    val timestampMillis: Long = System.currentTimeMillis()
)
