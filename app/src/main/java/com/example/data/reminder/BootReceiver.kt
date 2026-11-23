package com.example.data.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.data.db.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val db = AppDatabase.getDatabase(context)
                    val now = System.currentTimeMillis()
                    WorkManagerScheduler.enqueuePeriodicReminderCheck(context)
                    val reminders = db.reminderDao().getAllReminders().firstOrNull() ?: emptyList()
                    for (reminder in reminders) {
                        if (!reminder.isCompleted && reminder.triggerTimeMillis > now) {
                            ReminderScheduler.scheduleReminder(
                                context = context,
                                id = reminder.id,
                                triggerMillis = reminder.triggerTimeMillis,
                                title = reminder.title,
                                message = reminder.notes.ifEmpty { "Reminder" }
                            )
                            WorkManagerScheduler.scheduleOneTimeReminder(
                                context = context,
                                id = reminder.id,
                                triggerMillis = reminder.triggerTimeMillis,
                                title = reminder.title,
                                message = reminder.notes.ifEmpty { "Reminder" },
                                isBill = reminder.associatedBillId != null
                            )
                        }
                    }
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
