package com.ryeslim.confuciusnotifications


import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.ryeslim.confuciusnotifications.activities.MainActivity
import com.ryeslim.confuciusnotifications.dataclass.Proverb
import java.io.*
import java.util.*
import kotlin.math.floor

class AlarmReceiver : BroadcastReceiver() {
    private var notificationManager: NotificationManager? = null
    private var listOfAll: ArrayList<Proverb>? = null
    private var quoteOfTheDay: Proverb? = null
    private var notification = ""
    private var id: Short = 0
    private var proverb: String? = null
    private val random: Proverb
        get() {
            val result: Proverb
            val i = floor(Math.random() * listOfAll!!.size).toInt()
            result = listOfAll!![i]
            return result
        }

    /**
     * Called when the BroadcastReceiver receives an Intent broadcast.
     *
     * @param context The Context in which the receiver is running.
     * @param intent  The Intent being received.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onReceive(context: Context, intent: Intent) {
        listOfAll = ArrayList<Proverb>()
        listOfAll!!.ensureCapacity(700)
        notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val fileName = "all_quotes.txt"
        val file = File(context.filesDir, fileName)
        try {
            val reader = FileReader(file)
            val sc = Scanner(reader)
            while (sc.hasNext()) {
                id = sc.nextShort()
                proverb = sc.nextLine().trim { it <= ' ' }
                listOfAll!!.add(Proverb(id, proverb!!))
            }
            sc.close()
            reader.close()
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        quoteOfTheDay = random

        //Save the quote of the day
        val fileName1 = "quote_of_the_day.txt"
        var writer: FileWriter? = null
        val file1 = File(context.filesDir, fileName1)
        try {
            writer = FileWriter(file1, false) //will empty the file before writing
        } catch (e: IOException) {
            e.printStackTrace()
        }
        val prefix = "\n"
        if (writer != null) {
            try {
                writer.write(quoteOfTheDay!!.id.toString() + " ")
                writer.write(quoteOfTheDay!!.proverb + prefix)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        try {
            writer!!.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        notification = quoteOfTheDay!!.proverb
        //Deliver the notification.
        deliverNotification(context)
    }

    /**
     * Builds and delivers the notification.
     *
     * @param context, activity context.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun deliverNotification(context: Context) {
        // Create the content intent for the notification, which launches
        // this activity
        val contentIntent = Intent(context, MainActivity::class.java)
        val contentPendingIntent = PendingIntent.getActivity(
            context,
            NOTIFICATION_ID,
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        // Build the notification
        val builder: Notification.Builder? =
            Notification.Builder(context, PRIMARY_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_stand_up)
                .setContentIntent(contentPendingIntent)
                .setStyle(
                    Notification.BigTextStyle()
                        .bigText(notification)
                )
                .setPriority(Notification.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setDefaults(NotificationCompat.DEFAULT_ALL)

        // Deliver the notification
        notificationManager!!.notify(NOTIFICATION_ID, builder!!.build())
    }

    companion object {
        // Notification ID.
        private const val NOTIFICATION_ID = 0

        // Notification channel ID.
        private const val PRIMARY_CHANNEL_ID = "primary_notification_channel"
    }
}
