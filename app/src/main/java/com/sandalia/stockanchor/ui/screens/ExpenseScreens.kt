package com.sandalia.stockanchor.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sandalia.stockanchor.data.local.ExpenseEntity
import com.sandalia.stockanchor.domain.Perm
import com.sandalia.stockanchor.ui.components.AppCard
import com.sandalia.stockanchor.ui.components.ConfirmDialog
import com.sandalia.stockanchor.ui.components.EmptyState
import com.sandalia.stockanchor.ui.components.LocalContainer
import com.sandalia.stockanchor.ui.components.LockedFeature
import com.sandalia.stockanchor.ui.components.NumberField
import com.sandalia.stockanchor.ui.components.ScreenScaffold
import com.sandalia.stockanchor.ui.components.rememberRunner
import com.sandalia.stockanchor.util.Dates
import com.sandalia.stockanchor.util.Fmt
import com.sandalia.stockanchor.util.Period
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset

/** Suggested categories (same list as the server / web panel); any text is accepted. */
private val EXPENSE_CATEGORIES = listOf("Rent", "Salaries", "Utilities", "Transport", "Marketing", "Maintenance", "Supplies", "Taxes & fees", "Other")

/* ============================================================ Expenses */

@Composable
fun ExpensesScreen(onBack: () -> Unit) {
    val container = LocalContainer.current
    val session by container.session.state.collectAsStateWithLifecycle()
    val runner = rememberRunner()
    var period by rememberSaveable { mutableStateOf(Period.MONTH) }
    val (from, to) = remember(period) { Dates.range(period) }
    val expenses by remember(from, to) { container.repo.expenses(from, to) }.collectAsStateWithLifecycle(emptyList())
    val byCategory by remember(from, to) { container.repo.expenseCategoryTotals(from, to) }.collectAsStateWithLifecycle(emptyList())
    val used by container.repo.usedExpenseCategories().collectAsStateWithLifecycle(emptyList())
    var editing by remember { mutableStateOf<ExpenseEntity?>(null) }
    var deleting by remember { mutableStateOf<ExpenseEntity?>(null) }
    val canEdit = session.can(Perm.EXPENSES_MANAGE)
    val cur = session.currency
    val total = expenses.sumOf { it.amount }

    ScreenScaffold(
        "Expenses", subtitle = "Net profit = gross profit − expenses", onBack = onBack, busy = runner.busy,
        floatingActionButton = {
            if (canEdit && session.premiumActive) ExtendedFloatingActionButton(
                onClick = { editing = ExpenseEntity(category = "", amount = 0.0, date = System.currentTimeMillis()) },
                icon = { Icon(Icons.Filled.Add, null) }, text = { Text("Expense") },
            )
        },
    ) { pad ->
        if (!session.premiumActive) {
            LockedFeature("Expenses & net profit", "Record rent, salaries and other costs to see your real profit after expenses.", Modifier.padding(pad))
            return@ScreenScaffold
        }
        LazyColumn(Modifier.padding(pad), contentPadding = PaddingValues(16.dp, 8.dp, 16.dp, 96.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                    Period.entries.forEachIndexed { i, p ->
                        SegmentedButton(selected = period == p, onClick = { period = p }, shape = SegmentedButtonDefaults.itemShape(i, Period.entries.size)) { Text(p.label) }
                    }
                }
            }
            item {
                AppCard(Modifier.fillMaxWidth()) {
                    Text("TOTAL EXPENSES · ${period.label.uppercase()}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(Fmt.money(cur, total), style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.error)
                    Text("${expenses.size} ${if (expenses.size == 1) "entry" else "entries"}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (byCategory.isNotEmpty()) {
                        Spacer(Modifier.height(12.dp))
                        byCategory.forEach { row ->
                            Row(Modifier.fillMaxWidth().padding(top = 8.dp)) {
                                Text(row.category, Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                                Text(Fmt.money(cur, row.total), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                            }
                            LinearProgressIndicator(
                                progress = { if (total > 0) (row.total / total).toFloat() else 0f },
                                modifier = Modifier.fillMaxWidth().padding(top = 4.dp), color = MaterialTheme.colorScheme.error,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant, drawStopIndicator = {},
                            )
                        }
                    }
                }
            }
            if (expenses.isEmpty()) item {
                EmptyState(Icons.Filled.Payments, "No expenses ${period.label.lowercase()}", "Record rent, salaries and other costs to see your net profit on the dashboard.")
            }
            items(expenses, key = { it.id }) { e ->
                AppCard(Modifier.fillMaxWidth(), onClick = if (canEdit) ({ editing = e }) else null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(e.category, style = MaterialTheme.typography.titleSmall)
                            Text(listOfNotNull(Fmt.dateTime(e.date), e.note, e.userName).joinToString(" · "),
                                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text(Fmt.money(cur, e.amount), fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }

    editing?.let { e ->
        ExpenseDialog(
            expense = e,
            suggestions = (used + EXPENSE_CATEGORIES).distinct(),
            currency = cur,
            onDismiss = { editing = null },
            onSave = { saved -> editing = null; runner.run("Expense saved") { container.repo.saveExpense(saved) } },
            onDelete = if (e.id != 0L) ({ editing = null; deleting = e }) else null,
        )
    }
    deleting?.let { e ->
        ConfirmDialog("Delete expense?", "${e.category} · ${Fmt.money(cur, e.amount)} on ${Fmt.date(e.date)}",
            onConfirm = { runner.run("Expense deleted") { container.repo.deleteExpense(e.id) } }, onDismiss = { deleting = null })
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ExpenseDialog(
    expense: ExpenseEntity,
    suggestions: List<String>,
    currency: String,
    onDismiss: () -> Unit,
    onSave: (ExpenseEntity) -> Unit,
    onDelete: (() -> Unit)?,
) {
    var category by remember { mutableStateOf(expense.category) }
    var amount by remember { mutableStateOf(if (expense.amount > 0) Fmt.qty(expense.amount).replace(",", "") else "") }
    var note by remember { mutableStateOf(expense.note.orEmpty()) }
    var date by remember { mutableLongStateOf(expense.date) }
    var picking by remember { mutableStateOf(false) }
    val value = amount.toDoubleOrNull() ?: 0.0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (expense.id == 0L) "Add expense" else "Edit expense") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(category, { category = it }, label = { Text("Category") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    suggestions.forEach { s -> FilterChip(selected = category == s, onClick = { category = s }, label = { Text(s) }) }
                }
                NumberField(amount, { amount = it }, "Amount", Modifier.fillMaxWidth(), prefix = currency)
                OutlinedButton(onClick = { picking = true }, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Filled.CalendarMonth, null); Spacer(Modifier.width(8.dp)); Text(Fmt.date(date))
                }
                OutlinedTextField(note, { note = it }, label = { Text("Note") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            TextButton(enabled = category.isNotBlank() && value > 0, onClick = {
                onSave(expense.copy(category = category, amount = value, note = note, date = date))
            }) { Text("Save") }
        },
        dismissButton = {
            Row {
                onDelete?.let { TextButton(onClick = it) { Text("Delete", color = MaterialTheme.colorScheme.error) } }
                TextButton(onClick = onDismiss) { Text("Cancel") }
            }
        },
    )

    if (picking) {
        // DatePicker works in UTC midnights; keep the original time of day so the expense stays in its local day.
        val zone = ZoneId.systemDefault()
        val current = Instant.ofEpochMilli(date).atZone(zone)
        val todayUtc = LocalDate.now(zone).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        val state = rememberDatePickerState(
            initialSelectedDateMillis = current.toLocalDate().atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli(),
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long) = utcTimeMillis <= todayUtc
            },
        )
        DatePickerDialog(
            onDismissRequest = { picking = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let { picked ->
                        val day = Instant.ofEpochMilli(picked).atZone(ZoneOffset.UTC).toLocalDate()
                        date = day.atTime(current.toLocalTime()).atZone(zone).toInstant().toEpochMilli()
                    }
                    picking = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { picking = false }) { Text("Cancel") } },
        ) { DatePicker(state) }
    }
}
