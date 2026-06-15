package com.example.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity

object NotificationHelper {
    const val STUDY_CHANNEL_ID = "study_materials_channel"
    const val FEE_CHANNEL_ID = "fee_reminders_channel"

    private const val STUDY_NOTIFICATION_ID = 1001
    private const val FEE_NOTIFICATION_ID = 1002

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val studyChannel = NotificationChannel(
                STUDY_CHANNEL_ID,
                "Study Materials",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alerts students when new worksheets or study resources are uploaded by the tutor."
                enableLights(true)
                enableVibration(true)
            }

            val feeChannel = NotificationChannel(
                FEE_CHANNEL_ID,
                "Fee Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alerts parents and students when monthly fees are pending or outstanding."
                enableLights(true)
                enableVibration(true)
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(studyChannel)
            notificationManager.createNotificationChannel(feeChannel)
        }
    }

    fun triggerStudyMaterialNotification(context: Context, title: String, category: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, STUDY_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("📚 New Study Material")
            .setContentText("'$title' has been uploaded in category '$category'!")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("A new educational resource '$title' ($category) is now available in your study dashboard. Open the app to view and practice!")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(context)) {
            try {
                notify(STUDY_NOTIFICATION_ID, builder.build())
            } catch (e: SecurityException) {
                // Ignore missing permissions (handled in UI)
            }
        }
    }

    fun triggerFeeReminderNotification(context: Context, studentName: String, amountDue: Double, month: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            1,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, FEE_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("🔔 Tuition Fee Outstanding")
            .setContentText("Overdue reminder: ₹$amountDue pending for month $month ($studentName)")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("Dear Parent, this is an automated reminder that student $studentName has outstanding course fees of ₹$amountDue due for the month of $month. Kindly clear the pending balance to ensure uninterrupted classes.")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(context)) {
            try {
                notify(FEE_NOTIFICATION_ID + studentName.hashCode(), builder.build())
            } catch (e: SecurityException) {
                // Ignore missing permissions
            }
        }
    }
}
