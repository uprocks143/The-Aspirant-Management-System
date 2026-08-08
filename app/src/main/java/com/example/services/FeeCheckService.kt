package com.example.services

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import com.example.data.database.AppDatabase
import com.example.data.model.FeePayment
import com.example.data.model.Student
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import java.util.Calendar

class FeeCheckService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("FeeCheckService", "Fee check background service started")
        serviceScope.launch {
            try {
                val count = calculateAndAlertUnpaidFees(applicationContext)
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        applicationContext,
                        "Fee check complete! Calculated unpaid fees and sent $count alert notifications.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } catch (e: Exception) {
                Log.e("FeeCheckService", "Error running fee check calculation: ${e.localizedMessage}")
            } finally {
                stopSelf(startId)
            }
        }
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        Log.d("FeeCheckService", "Fee check background service destroyed")
    }

    companion object {
        fun startService(context: Context) {
            val intent = Intent(context, FeeCheckService::class.java)
            context.startService(intent)
        }

        suspend fun calculateAndAlertUnpaidFees(context: Context): Int {
            Log.d("FeeCheckService", "Starting auto-calculation of unpaid fees...")
            val db = AppDatabase.getDatabase(context)
            val dao = db.appDao()
            
            // Get all students
            val students = dao.getAllStudents().first()
            val batches = dao.getAllBatches().first()
            val allPayments = dao.getAllFeePayments().first()
            
            val calendar = Calendar.getInstance()
            val currentYear = calendar.get(Calendar.YEAR)
            val currentMonth = calendar.get(Calendar.MONTH) // 0-indexed (0 is Jan)

            var alertsTriggered = 0

            for (student in students) {
                if (student.isAlumni) continue
                
                val batch = batches.find { it.id == student.batchId } ?: continue
                val feesAmount = batch.feesAmount
                
                // Parse student startDate, e.g. "2026-03-15"
                val startDateStr = student.startDate
                val startYear: Int
                val startMonth: Int // 0-indexed
                
                if (startDateStr.isNotEmpty()) {
                    val parts = startDateStr.split("-")
                    if (parts.size >= 2) {
                        startYear = parts[0].toIntOrNull() ?: currentYear
                        startMonth = (parts[1].toIntOrNull() ?: (currentMonth + 1)) - 1
                    } else {
                        startYear = currentYear
                        startMonth = currentMonth
                    }
                } else {
                    startYear = currentYear
                    startMonth = currentMonth
                }
                
                // We want to loop from startYear & startMonth up to currentYear & currentMonth
                val monthsList = getMonthsInRange(startYear, startMonth, currentYear, currentMonth)
                
                for (mInfo in monthsList) {
                    val monthName = mInfo.first // e.g. "March 2026"
                    
                    // Filter completed payments for this student and this month
                    val studentPaymentsForMonth = allPayments.filter {
                        it.studentId == student.id && 
                        it.monthPaidFor.equals(monthName, ignoreCase = true) && 
                        (it.transactionStatus.equals("SUCCESS", ignoreCase = true) || it.transactionStatus.isEmpty())
                    }
                    
                    val totalPaid = studentPaymentsForMonth.sumOf { it.amountPaid + it.discount }
                    
                    if (totalPaid < feesAmount) {
                        val unpaidAmount = feesAmount - totalPaid
                        Log.d("FeeCheckService", "Student ${student.name} owes ₹$unpaidAmount for $monthName")
                        
                        // Check if a PENDING FeePayment record already exists so we don't duplicate,
                        // or check if we should auto-calculate it and create a pending installment record
                        val pendingExists = allPayments.any {
                            it.studentId == student.id &&
                            it.monthPaidFor.equals(monthName, ignoreCase = true) &&
                            it.transactionStatus.equals("PENDING", ignoreCase = true)
                        }
                        
                        if (!pendingExists && unpaidAmount > 0) {
                            val pendingPayment = FeePayment(
                                studentId = student.id,
                                feeType = "Monthly-based",
                                amountPaid = 0.0,
                                discount = 0.0,
                                receiptNo = "REC-AUTO-${System.currentTimeMillis() % 100000}",
                                monthPaidFor = monthName,
                                paymentMode = "UPI",
                                remarks = "Auto-calculated unpaid fee installment of ₹$unpaidAmount",
                                transactionStatus = "PENDING"
                            )
                            dao.insertFeePayment(pendingPayment)
                        }
                        
                        // Trigger fee reminder alert notification
                        NotificationHelper.triggerFeeReminderNotification(
                            context = context,
                            studentName = student.name,
                            amountDue = unpaidAmount,
                            month = monthName
                        )
                        alertsTriggered++
                    }
                }
            }
            Log.d("FeeCheckService", "Auto-calculation complete. Triggered $alertsTriggered alert notifications.")
            return alertsTriggered
        }
        
        private fun getMonthsInRange(startYear: Int, startMonth: Int, endYear: Int, endMonth: Int): List<Pair<String, Pair<Int, Int>>> {
            val months = listOf(
                "January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"
            )
            val result = mutableListOf<Pair<String, Pair<Int, Int>>>()
            
            var currYear = startYear
            var currMonth = startMonth
            
            // Limit loop safety bounds (e.g. max 24 months to avoid infinite loops on invalid input)
            var iterations = 0
            while ((currYear < endYear || (currYear == endYear && currMonth <= endMonth)) && iterations < 120) {
                val name = months.getOrElse(currMonth) { "June" }
                result.add(Pair("$name $currYear", Pair(currYear, currMonth)))
                
                currMonth++
                if (currMonth > 11) {
                    currMonth = 0
                    currYear++
                }
                iterations++
            }
            return result
        }
    }
}
