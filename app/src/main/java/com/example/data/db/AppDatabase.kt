package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.AnalyticsLogItem
import com.example.data.model.BillItem
import com.example.data.model.ReminderItem
import com.example.data.model.SavingsGoal
import com.example.data.model.SubscriptionRecord
import com.example.data.model.TaskItem
import com.example.data.model.TripPlan
import com.example.data.model.TransactionItem

@Database(
    entities = [
        TaskItem::class,
        ReminderItem::class,
        TransactionItem::class,
        BillItem::class,
        TripPlan::class,
        SavingsGoal::class,
        SubscriptionRecord::class,
        AnalyticsLogItem::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun reminderDao(): ReminderDao
    abstract fun transactionDao(): TransactionDao
    abstract fun billDao(): BillDao
    abstract fun tripDao(): TripDao
    abstract fun savingsGoalDao(): SavingsGoalDao
    abstract fun subscriptionDao(): SubscriptionDao
    abstract fun analyticsDao(): AnalyticsDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "ai_life_assistant_db"
                ).fallbackToDestructiveMigration(dropAllTables = true)
                 .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
