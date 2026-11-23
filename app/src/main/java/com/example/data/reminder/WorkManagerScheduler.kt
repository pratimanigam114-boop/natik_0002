package com.example.data.reminder

import android.content.Context
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object WorkManagerScheduler {

    private const val PERIODIC_WORK_TAG = "periodic_task_reminder_check"

    fun scheduleOneTimeReminder(
        context: Context,
        id: Long,
        triggerMillis: Long,
        title: String,
        message: String,
        isBill: Boolean = false
    ) {
        val now = System.currentTimeMillis()
        val delayMillis = (triggerMillis - now).coerceAtLeast(0L)

        val inputData = Data.Builder()
            .putLong("EXTRA_ID", id)
            .putString("EXTRA_TITLE", title)
            .putString("EXTRA_MESSAGE", message)
            .putBoolean("EXTRA_IS_BILL", isBill)
            .build()

        val workRequest = OneTimeWorkRequestBuilder<TaskReminderWorker>()
            .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
            .setInputData(inputData)
            .addTag("reminder_$id")
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            "work_reminder_$id",
            ExistingWorkPolicy.REPLACE,
            workRequest
        )
    }

    fun cancelReminder(context: Context, id: Long) {
        WorkManager.getInstance(context).cancelUniqueWork("work_reminder_$id")
    }

    fun enqueuePeriodicReminderCheck(context: Context) {
        val periodicRequest = PeriodicWorkRequestBuilder<TaskReminderWorker>(
            15, TimeUnit.MINUTES,
            5, TimeUnit.MINUTES // Flex interval
        )
            .addTag(PERIODIC_WORK_TAG)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            PERIODIC_WORK_TAG,
            ExistingPeriodicWorkPolicy.KEEP,
            periodicRequest
        )
    }
}
