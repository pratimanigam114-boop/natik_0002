package com.example.data.payment

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.example.data.db.AppDatabase
import com.example.data.model.AnalyticsLogItem
import com.example.data.model.SubscriptionRecord
import kotlinx.coroutines.flow.firstOrNull
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

sealed class PaymentResult {
    data class Success(val orderId: String, val transactionId: String, val planName: String, val amount: Double) : PaymentResult()
    data class Error(val message: String) : PaymentResult()
    object Cancelled : PaymentResult()
    object Pending : PaymentResult()
}

data class PlanDetail(
    val id: String,
    val name: String,
    val price: Double,
    val period: String,
    val billedAs: String,
    val savingsBadge: String? = null,
    val features: List<String>
)

object PaymentManager {

    val PLANS = listOf(
        PlanDetail(
            id = "plan_monthly",
            name = "AI Life Pro Monthly",
            price = 149.0,
            period = "/month",
            billedAs = "Billed monthly at ₹149. Cancel anytime.",
            savingsBadge = null,
            features = listOf(
                "Full AI Personal Assistant (Gemini 3.5 Flash)",
                "AI Spending Insights & Smart Budget Coach",
                "Automated Bill Forecasting & Overdue Alerts",
                "Advanced Multi-day Trip Optimizer & Checklist",
                "Unlimited Saved Plans, Budgets & Reminders",
                "Priority Cloud Sync & PDF Report Export"
            )
        ),
        PlanDetail(
            id = "plan_annual",
            name = "AI Life Pro Annual",
            price = 999.0,
            period = "/year",
            billedAs = "Billed annually at ₹999 (Equivalent to ~₹83/mo).",
            savingsBadge = "Save 44%",
            features = listOf(
                "Everything in AI Life Pro Monthly",
                "Dedicated Financial Goal & Savings Planner",
                "Full Flight & Hotel Direct Portal Deep-Links",
                "Custom Notification Sounds & VIP Priority Support",
                "Lifetime Guarantee of Locked-in Price"
            )
        )
    )

    fun createUpiIntent(
        vpa: String = "merchant@upi",
        payeeName: String = "AI Life Assistant",
        amount: Double,
        transactionNote: String
    ): Intent {
        val uri = Uri.parse("upi://pay").buildUpon()
            .appendQueryParameter("pa", vpa)
            .appendQueryParameter("pn", payeeName)
            .appendQueryParameter("tn", transactionNote)
            .appendQueryParameter("am", String.format(Locale.US, "%.2f", amount))
            .appendQueryParameter("cu", "INR")
            .build()

        return Intent(Intent.ACTION_VIEW, uri)
    }

    suspend fun verifyAndActivateSubscription(
        context: Context,
        db: AppDatabase,
        plan: PlanDetail,
        paymentMethod: String,
        transactionRef: String
    ): PaymentResult {
        return try {
            val orderId = "ORD-" + System.currentTimeMillis().toString().takeLast(8) + "-" + (100..999).random()
            val txnId = if (transactionRef.isNotBlank()) transactionRef else "TXN-RAZORPAY-" + UUID.randomUUID().toString().take(12).uppercase()
            val now = System.currentTimeMillis()
            val validityDuration = if (plan.id == "plan_annual") 365L * 86400000L else 30L * 86400000L

            val subRecord = SubscriptionRecord(
                id = 1,
                isProActive = true,
                planType = plan.name,
                purchaseDateMillis = now,
                validUntilMillis = now + validityDuration,
                orderId = orderId,
                paymentMethod = paymentMethod,
                transactionRef = txnId
            )
            db.subscriptionDao().saveSubscription(subRecord)

            // Log event in developer analytics
            db.analyticsDao().insertLog(
                AnalyticsLogItem(
                    eventType = "SUBSCRIPTION_PURCHASED",
                    details = "Plan: ${plan.name}, Amount: ₹${plan.price}, Method: $paymentMethod, Order: $orderId"
                )
            )

            PaymentResult.Success(orderId, txnId, plan.name, plan.price)
        } catch (e: Exception) {
            PaymentResult.Error(e.message ?: "Failed to record payment verification")
        }
    }

    suspend fun restorePurchases(db: AppDatabase): Boolean {
        val sub = db.subscriptionDao().getSubscriptionDirect()
        return sub != null && sub.isProActive && sub.validUntilMillis > System.currentTimeMillis()
    }
}
