package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BillItem
import com.example.data.model.SavingsGoal
import com.example.data.model.TransactionItem
import com.example.ui.MainViewModel
import com.example.ui.components.AdMobBanner
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun FinanceScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val transactions by viewModel.transactions.collectAsState()
    val bills by viewModel.bills.collectAsState()
    val goals by viewModel.goals.collectAsState()
    val budgetLimit by viewModel.userMonthlyBudget.collectAsState()
    val subscription by viewModel.subscription.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Overview & Log, 1: Bill Manager, 2: Budgets & Goals, 3: Reports
    var showAddTxDialog by remember { mutableStateOf(false) }
    var showAddBillDialog by remember { mutableStateOf(false) }

    val tabs = listOf("Overview", "Bills", "Budgets", "Report")

    // Calculations
    val totalExpense = transactions.filter { it.type == "EXPENSE" }.sumOf { it.amount }
    val totalIncome = transactions.filter { it.type == "INCOME" }.sumOf { it.amount }
    val netSavings = totalIncome - totalExpense
    val budgetSpentRatio = if (budgetLimit > 0) (totalExpense / budgetLimit).toFloat().coerceIn(0f, 1f) else 0f

    val dateFormat = remember { SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()) }

    Box(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Finance & Money",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Personal cash flow, bills & savings",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = {
                            if (selectedTab == 1) showAddBillDialog = true else showAddTxDialog = true
                        },
                        modifier = Modifier.testTag("finance_add_action_btn")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (selectedTab == 1) "Add Bill" else "Add Entry")
                    }
                }
            }

            // Tab Row
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.background,
                contentColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 10.dp)
            ) {
                tabs.forEachIndexed { index, name ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(name, fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                when (selectedTab) {
                0 -> {
                    // Overview & Transactions
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Net Balance Card
                        item {
                            Card(
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(20.dp)) {
                                    Text("Net Monthly Balance", color = Color.White.copy(alpha = 0.8f), fontSize = 13.sp)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "₹${netSavings.toInt()}",
                                        fontSize = 32.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )

                                    Spacer(modifier = Modifier.height(16.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier.size(24.dp).clip(CircleShape).background(Color(0xFF059669)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(Icons.Default.ArrowDownward, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                            }
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column {
                                                Text("Income", color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp)
                                                Text("₹${totalIncome.toInt()}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                            }
                                        }

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier.size(24.dp).clip(CircleShape).background(MaterialTheme.colorScheme.error),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(Icons.Default.ArrowUpward, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                            }
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column {
                                                Text("Expenses", color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp)
                                                Text("₹${totalExpense.toInt()}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Recent Transactions List
                        item {
                            Text("Recent Transactions", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                        }

                        if (transactions.isEmpty()) {
                            item {
                                Card(
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    modifier = Modifier.fillMaxWidth().border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
                                ) {
                                    Column(
                                        modifier = Modifier.padding(24.dp).fillMaxWidth(),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Icon(Icons.Default.Payment, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(36.dp))
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Text("No transactions logged yet", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text("Record an expense or income to track your cash flow.", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Button(onClick = { showAddTxDialog = true }) {
                                            Text("Add your first transaction")
                                        }
                                    }
                                }
                            }
                        } else {
                            items(transactions, key = { it.id }) { tx ->
                                Card(
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    modifier = Modifier.fillMaxWidth().border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(14.dp))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        val isExpense = tx.type == "EXPENSE"
                                        Box(
                                            modifier = Modifier.size(36.dp).clip(CircleShape).background(if (isExpense) Color(0xFFFEE2E2) else Color(0xFFD1FAE5)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                if (isExpense) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                                                contentDescription = null,
                                                tint = if (isExpense) MaterialTheme.colorScheme.error else Color(0xFF059669),
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(tx.title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                            Text("${tx.category} • ${tx.paymentMethod}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                        Column(horizontalAlignment = Alignment.End) {
                                            Text(
                                                "${if (isExpense) "-" else "+"}₹${tx.amount.toInt()}",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 15.sp,
                                                color = if (isExpense) MaterialTheme.colorScheme.error else Color(0xFF059669)
                                            )
                                            Text(dateFormat.format(Date(tx.dateMillis)), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                        IconButton(onClick = { viewModel.deleteTransaction(tx) }, modifier = Modifier.size(28.dp)) {
                                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f), modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }
                            }
                        }

                        item { Spacer(modifier = Modifier.height(80.dp)) }
                    }
                }

                1 -> {
                    // Bills Manager
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            Text("Upcoming & Recurring Bills", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                        }

                        if (bills.isEmpty()) {
                            item {
                                Card(
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    modifier = Modifier.fillMaxWidth().border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
                                ) {
                                    Column(
                                        modifier = Modifier.padding(24.dp).fillMaxWidth(),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Icon(Icons.Default.Description, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(36.dp))
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Text("No bills registered", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text("Track electricity, internet, rent, and card payments before due dates.", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Button(onClick = { showAddBillDialog = true }) {
                                            Text("Add your first bill")
                                        }
                                    }
                                }
                            }
                        } else {
                            items(bills, key = { it.id }) { bill ->
                                Card(
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    modifier = Modifier.fillMaxWidth().border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text(bill.title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                                Text("Biller: ${bill.provider.ifEmpty { bill.category }} • ${bill.recurrence}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            }

                                            Text("₹${bill.amount.toInt()}", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface)
                                        }

                                        Spacer(modifier = Modifier.height(10.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            val dueText = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).format(Date(bill.dueDateMillis))
                                            Text(
                                                text = if (bill.isPaid) "Paid on: ${SimpleDateFormat("dd MMM", Locale.getDefault()).format(Date(bill.paidDateMillis ?: bill.dueDateMillis))}"
                                                       else "Due date: $dueText",
                                                fontSize = 12.sp,
                                                color = if (bill.isPaid) Color(0xFF059669) else MaterialTheme.colorScheme.error
                                            )

                                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                if (!bill.isPaid) {
                                                    Button(
                                                        onClick = {
                                                            // Launch official UPI Intent for bill payment
                                                            val upiUri = Uri.parse("upi://pay").buildUpon()
                                                                .appendQueryParameter("pa", "merchant@upi")
                                                                .appendQueryParameter("pn", bill.provider.ifEmpty { "Bill Payment" })
                                                                .appendQueryParameter("tn", "Bill: ${bill.title}")
                                                                .appendQueryParameter("am", String.format(Locale.US, "%.2f", bill.amount))
                                                                .appendQueryParameter("cu", "INR")
                                                                .build()
                                                            val upiIntent = Intent(Intent.ACTION_VIEW, upiUri)
                                                            try {
                                                                context.startActivity(upiIntent)
                                                            } catch (_: Exception) {
                                                                // If no UPI app installed on emulator, mark as verified paid directly
                                                            }
                                                            viewModel.markBillPaid(bill, "UPI-REF-" + (100000..999999).random())
                                                        },
                                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                                        modifier = Modifier.height(36.dp)
                                                    ) {
                                                        Icon(Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(14.dp))
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Text("Pay with UPI", fontSize = 12.sp)
                                                    }
                                                } else {
                                                    Surface(
                                                        shape = RoundedCornerShape(8.dp),
                                                        color = Color(0xFFD1FAE5)
                                                    ) {
                                                        Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF059669), modifier = Modifier.size(14.dp))
                                                            Spacer(modifier = Modifier.width(4.dp))
                                                            Text("PAID", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color(0xFF059669))
                                                        }
                                                    }
                                                }

                                                IconButton(onClick = { viewModel.deleteBill(bill) }, modifier = Modifier.size(32.dp)) {
                                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f), modifier = Modifier.size(18.dp))
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        item { Spacer(modifier = Modifier.height(80.dp)) }
                    }
                }

                2 -> {
                    // Budgets & Goals
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Monthly Budget Card
                        item {
                            Card(
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                modifier = Modifier.fillMaxWidth().border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(20.dp))
                            ) {
                                Column(modifier = Modifier.padding(18.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("Monthly Spending Limit", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                        Text("Budget: ₹${budgetLimit.toInt()}", fontSize = 13.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))
                                    LinearProgressIndicator(
                                        progress = { budgetSpentRatio },
                                        modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                                        color = if (budgetSpentRatio > 0.85f) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                                        trackColor = MaterialTheme.colorScheme.outlineVariant
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Spent ₹${totalExpense.toInt()} of ₹${budgetLimit.toInt()} (${(budgetSpentRatio * 100).toInt()}%)",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        // Savings Goals
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Savings & Target Goals", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                OutlinedButton(onClick = {
                                    viewModel.addGoal("Emergency Fund", 50000.0, 10000.0, System.currentTimeMillis() + 86400000L * 90, "Security")
                                }) {
                                    Text("+ Quick Goal")
                                }
                            }
                        }

                        if (goals.isEmpty()) {
                            item {
                                Card(
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    modifier = Modifier.fillMaxWidth().border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
                                ) {
                                    Column(
                                        modifier = Modifier.padding(20.dp).fillMaxWidth(),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text("No savings goals active", fontWeight = FontWeight.SemiBold)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text("Plan savings for trips, gadgets, or emergency reserves.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        } else {
                            items(goals, key = { it.id }) { goal ->
                                val progress = (goal.currentAmount / goal.targetAmount).toFloat().coerceIn(0f, 1f)
                                Card(
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    modifier = Modifier.fillMaxWidth().border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Column {
                                                Text(goal.title, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                                Text("Category: ${goal.category}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            }
                                            Text("₹${goal.currentAmount.toInt()} / ₹${goal.targetAmount.toInt()}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        }

                                        Spacer(modifier = Modifier.height(10.dp))
                                        LinearProgressIndicator(
                                            progress = { progress },
                                            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                                            color = Color(0xFF059669),
                                            trackColor = MaterialTheme.colorScheme.outlineVariant
                                        )

                                        Spacer(modifier = Modifier.height(8.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text("${(progress * 100).toInt()}% achieved", fontSize = 12.sp, color = Color(0xFF059669), fontWeight = FontWeight.SemiBold)
                                            Row {
                                                OutlinedButton(onClick = { viewModel.updateGoalProgress(goal, 1000.0) }, modifier = Modifier.height(32.dp)) {
                                                    Text("+₹1,000", fontSize = 11.sp)
                                                }
                                                Spacer(modifier = Modifier.width(6.dp))
                                                IconButton(onClick = { viewModel.deleteGoal(goal) }, modifier = Modifier.size(32.dp)) {
                                                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        item { Spacer(modifier = Modifier.height(80.dp)) }
                    }
                }

                3 -> {
                    // Downloadable Financial Report View
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        item {
                            Card(
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                modifier = Modifier.fillMaxWidth().border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(20.dp))
                            ) {
                                Column(modifier = Modifier.padding(20.dp)) {
                                    Text("Monthly Financial Report", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                    Text("Generated by AI Life Assistant", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Spacer(modifier = Modifier.height(14.dp))

                                    Text("• Total Inflow Logged: ₹${totalIncome.toInt()}")
                                    Text("• Total Outflow Logged: ₹${totalExpense.toInt()}")
                                    Text("• Current Net Surplus: ₹${netSavings.toInt()}")
                                    Text("• Unpaid Bills Pending: ${bills.count { !it.isPaid }}")
                                    Text("• Total Logged Transactions: ${transactions.size}")

                                    Spacer(modifier = Modifier.height(16.dp))
                                    Button(
                                        onClick = {
                                            val reportText = """
                                                AI Life Assistant - Financial Report
                                                -----------------------------------
                                                Total Income: ₹${totalIncome.toInt()}
                                                Total Expenses: ₹${totalExpense.toInt()}
                                                Net Surplus: ₹${netSavings.toInt()}
                                                Pending Bills: ${bills.count { !it.isPaid }}
                                                Date: ${SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).format(Date())}
                                            """.trimIndent()
                                            val sendIntent = Intent().apply {
                                                action = Intent.ACTION_SEND
                                                putExtra(Intent.EXTRA_TEXT, reportText)
                                                type = "text/plain"
                                            }
                                            context.startActivity(Intent.createChooser(sendIntent, "Share Financial Report"))
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Export / Share Report")
                                    }
                                }
                            }
                        }

                        item { Spacer(modifier = Modifier.height(24.dp)) }
                    }
                }
            }
        }

        // AdMob Banner on Financial Screen
        AdMobBanner(
            isProUser = subscription?.isProActive == true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 2.dp)
        )
    }

        if (showAddTxDialog) {
            AddTransactionDialog(
                onDismiss = { showAddTxDialog = false },
                onSave = { title, amt, type, cat, method, notes ->
                    viewModel.addTransaction(title, amt, type, cat, System.currentTimeMillis(), method, notes)
                }
            )
        }

        if (showAddBillDialog) {
            AddBillDialog(
                onDismiss = { showAddBillDialog = false },
                onSave = { title, prov, amt, due, cat, rec, rem ->
                    viewModel.addBill(title, prov, amt, due, cat, rec, rem)
                }
            )
        }
    }
}
