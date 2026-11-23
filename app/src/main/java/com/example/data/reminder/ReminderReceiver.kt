package com.example.data.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.getLongExtra("EXTRA_ID", 0L)
        val title = intent.getStringExtra("EXTRA_TITLE") ?: "Scheduled Reminder"
        val message = intent.getStringExtra("EXTRA_MESSAGE") ?: "You have an item scheduled for now."
        val isBill = intent.getBooleanExtra("EXTRA_IS_BILL", false)

        NotificationHelper.showReminderNotification(
            context = context,
            notificationId = (id % 100000).toInt(),
            title = title,
            message = message,
            isBill = isBill
        )
    }
}
