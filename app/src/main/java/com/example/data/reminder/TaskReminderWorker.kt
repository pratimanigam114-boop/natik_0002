package com.example.data.reminder

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.data.db.AppDatabase
import kotlinx.coroutines.flow.firstOrNull

class TaskReminderWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val specificId = inputData.getLong("EXTRA_ID", -1L)
        val title = inputData.getString("EXTRA_TITLE")
        val message = inputData.getString("EXTRA_MESSAGE")
        val isBill = inputData.getBoolean("EXTRA_IS_BILL", false)

        // 1. If this was a targeted one-time scheduled work for a specific task/reminder
        if (specificId != -1L && !title.isNullOrBlank()) {
            NotificationHelper.showReminderNotification(
                context = applicationContext,
                notificationId = (specificId % 100000).toInt(),
                title = title,
                message = message ?: "Upcoming scheduled item",
                isBill = isBill
            )
            return Result.success()
        }

        // 2. Periodic background sweep: checks for any pending items due within the next 30 minutes
        try {
            val db = AppDatabase.getDatabase(applicationContext)
            val now = System.currentTimeMillis()
            val lookaheadWindow = now + (30 * 60 * 1000L) // 30 minutes ahead

            // Check upcoming uncompleted tasks with reminders
            val tasks = db.taskDao().getAllTasks().firstOrNull() ?: emptyList()
            for (task in tasks) {
                if (!task.isCompleted && task.reminderEnabled && task.dueDateMillis in (now - 60000L)..lookaheadWindow) {
                    NotificationHelper.showReminderNotification(
                        context = applicationContext,
                        notificationId = (task.id % 100000).toInt() + 1000,
                        title = "Upcoming Task: ${task.title}",
                        message = task.description.ifEmpty { "Priority: ${task.priority} • Category: ${task.category}" },
                        isBill = false
                    )
                }
            }

            // Check upcoming active reminders
            val reminders = db.reminderDao().getAllReminders().firstOrNull() ?: emptyList()
            for (reminder in reminders) {
                if (!reminder.isCompleted && reminder.triggerTimeMillis in (now - 60000L)..lookaheadWindow) {
                    NotificationHelper.showReminderNotification(
                        context = applicationContext,
                        notificationId = (reminder.id % 100000).toInt() + 2000,
                        title = reminder.title,
                        message = reminder.notes.ifEmpty { "Scheduled reminder" },
                        isBill = false
                    )
                }
            }

            // Check pending unpaid bills due soon
            val bills = db.billDao().getUnpaidBills().firstOrNull() ?: emptyList()
            for (bill in bills) {
                if (!bill.isPaid && bill.reminderSet && bill.dueDateMillis in (now - 60000L)..lookaheadWindow) {
                    NotificationHelper.showReminderNotification(
                        context = applicationContext,
                        notificationId = (bill.id % 100000).toInt() + 3000,
                        title = "Bill Due Soon: ${bill.title}",
                        message = "₹${bill.amount.toInt()} due for ${bill.provider.ifEmpty { bill.category }}",
                        isBill = true
                    )
                }
            }

            return Result.success()
        } catch (e: Exception) {
            return Result.retry()
        }
    }
}
