package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.AnalyticsLogItem
import com.example.data.model.BillItem
import com.example.data.model.ReminderItem
import com.example.data.model.SavingsGoal
import com.example.data.model.SubscriptionRecord
import com.example.data.model.TaskItem
import com.example.data.model.TripPlan
import com.example.data.model.TransactionItem
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks ORDER BY isCompleted ASC, dueDateMillis ASC, id DESC")
    fun getAllTasks(): Flow<List<TaskItem>>

    @Query("SELECT * FROM tasks WHERE dueDateMillis >= :startOfDay AND dueDateMillis <= :endOfDay ORDER BY isCompleted ASC, id DESC")
    fun getTasksForDay(startOfDay: Long, endOfDay: Long): Flow<List<TaskItem>>

    @Query("SELECT * FROM tasks WHERE id = :id")
    suspend fun getTaskById(id: Long): TaskItem?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskItem): Long

    @Update
    suspend fun updateTask(task: TaskItem)

    @Delete
    suspend fun deleteTask(task: TaskItem)

    @Query("DELETE FROM tasks")
    suspend fun deleteAll()
}

@Dao
interface ReminderDao {
    @Query("SELECT * FROM reminders ORDER BY isCompleted ASC, triggerTimeMillis ASC")
    fun getAllReminders(): Flow<List<ReminderItem>>

    @Query("SELECT * FROM reminders WHERE triggerTimeMillis >= :start AND triggerTimeMillis <= :end ORDER BY triggerTimeMillis ASC")
    fun getRemindersInRange(start: Long, end: Long): Flow<List<ReminderItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: ReminderItem): Long

    @Update
    suspend fun updateReminder(reminder: ReminderItem)

    @Delete
    suspend fun deleteReminder(reminder: ReminderItem)

    @Query("DELETE FROM reminders")
    suspend fun deleteAll()
}

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY dateMillis DESC")
    fun getAllTransactions(): Flow<List<TransactionItem>>

    @Query("SELECT * FROM transactions WHERE dateMillis >= :start AND dateMillis <= :end ORDER BY dateMillis DESC")
    fun getTransactionsInRange(start: Long, end: Long): Flow<List<TransactionItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionItem): Long

    @Delete
    suspend fun deleteTransaction(transaction: TransactionItem)

    @Query("DELETE FROM transactions")
    suspend fun deleteAll()
}

@Dao
interface BillDao {
    @Query("SELECT * FROM bills ORDER BY isPaid ASC, dueDateMillis ASC")
    fun getAllBills(): Flow<List<BillItem>>

    @Query("SELECT * FROM bills WHERE isPaid = 0 ORDER BY dueDateMillis ASC")
    fun getUnpaidBills(): Flow<List<BillItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBill(bill: BillItem): Long

    @Update
    suspend fun updateBill(bill: BillItem)

    @Delete
    suspend fun deleteBill(bill: BillItem)

    @Query("DELETE FROM bills")
    suspend fun deleteAll()
}

@Dao
interface TripDao {
    @Query("SELECT * FROM trips ORDER BY startDateMillis ASC")
    fun getAllTrips(): Flow<List<TripPlan>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrip(trip: TripPlan): Long

    @Update
    suspend fun updateTrip(trip: TripPlan)

    @Delete
    suspend fun deleteTrip(trip: TripPlan)

    @Query("DELETE FROM trips")
    suspend fun deleteAll()
}

@Dao
interface SavingsGoalDao {
    @Query("SELECT * FROM savings_goals ORDER BY id DESC")
    fun getAllGoals(): Flow<List<SavingsGoal>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: SavingsGoal): Long

    @Update
    suspend fun updateGoal(goal: SavingsGoal)

    @Delete
    suspend fun deleteGoal(goal: SavingsGoal)

    @Query("DELETE FROM savings_goals")
    suspend fun deleteAll()
}

@Dao
interface SubscriptionDao {
    @Query("SELECT * FROM subscription_record WHERE id = 1")
    fun getSubscription(): Flow<SubscriptionRecord?>

    @Query("SELECT * FROM subscription_record WHERE id = 1")
    suspend fun getSubscriptionDirect(): SubscriptionRecord?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSubscription(sub: SubscriptionRecord)
}

@Dao
interface AnalyticsDao {
    @Query("SELECT * FROM analytics_log ORDER BY timestampMillis DESC LIMIT 100")
    fun getRecentLogs(): Flow<List<AnalyticsLogItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: AnalyticsLogItem)

    @Query("SELECT COUNT(*) FROM analytics_log")
    suspend fun getLogCount(): Int
}
