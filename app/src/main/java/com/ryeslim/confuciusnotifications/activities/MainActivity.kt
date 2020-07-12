package com.ryeslim.confuciusnotifications.activities

import android.app.*
import android.app.TimePickerDialog.OnTimeSetListener
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import android.widget.ToggleButton
import androidx.appcompat.app.AppCompatActivity
import com.ryeslim.confuciusnotifications.AlarmReceiver
import com.ryeslim.confuciusnotifications.R
import com.ryeslim.confuciusnotifications.dataclass.Proverb
import com.ryeslim.confuciusnotifications.model.AllProverbs
import com.ryeslim.confuciusnotifications.model.SwipeDetector
import com.ryeslim.confuciusnotifications.model.WorkWithProverbs
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.*
import kotlin.coroutines.CoroutineContext

class MainActivity : AppCompatActivity(), CoroutineScope {

    private var notificationManager: NotificationManager? = null

    private var job: Job = Job()
    override val coroutineContext: CoroutineContext get() = Dispatchers.Main + job

    private var yesBookmarked: Int = 0 // resource id for the filled heart icon
    private var notBookmarked: Int = 0 // resource id for the heart contour icon
    private lateinit var thisProverb: Proverb // the quote on the screen

    // global ImageView for the two-state favorite (heart) icon, which can be either
    // filled (for bookmarked quotes) or contour (not bookmarked quotes)
    private lateinit var favorite: ImageView

    // if notifications are on, shows the time selected by the user
    private var notifications: TextView? = null

    override fun onBackPressed() {
        moveTaskToBack(true)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val res = this.resources
        yesBookmarked = res.getIdentifier("ic_favorite", "drawable", this.packageName)
        notBookmarked = res.getIdentifier("ic_favorite_border", "drawable", this.packageName)

        WorkWithProverbs.getInstance().setContext(this)
        AllProverbs.instance!!.setContext(this)
        instance = this

        AllProverbs.instance!!.makeNotifyReadable() //no notifications by default on first launch

        if (savedInstanceState != null) {
            stayHere()
        } else {
            WorkWithProverbs.getInstance().startClean() //to handle the phone's back button

            //If the quoteFile is readable and up to date, makeFirstStep.
            //Yet if there is no internet, the outdated file is ok, as long as it is readable
            if (AllProverbs.instance!!.quoteFileReadable() && AllProverbs.instance!!.quotesUpToDate()
            ) {
                makeFirstStep()
            } else if (isOnline()) {
                launch {
                    // downloads the list of all quotes
                    AllProverbs.instance!!.fetchProverbs()
                }
            } else if (!AllProverbs.instance!!.quoteFileReadable()) {
                errorMessage(getString(R.string.connect_and_start_again))
            } else makeFirstStep()
        }


        // Set a "swipe" listener on the quote and react when clicked
        val swipe = SwipeDetector(this)
        proverb.setOnTouchListener(swipe)

        // Set a click listener on the quote and react when clicked
        proverb.setOnClickListener { goForward() }

        // Find the '>' ImageView, set a click listener on it and react when clicked
        nextPage.setOnClickListener { goForward() }

        // Find the '<' ImageView, set a click listener on it and react when clicked
        previousPage.setOnClickListener { goBackwards() }

        // Find the "first page" ImageView, set a click listener on it and react when clicked
        firstPage.setOnClickListener { firstPage() }

        // Find the "last page" ImageView, set a click listener on it and react when clicked
        lastPage.setOnClickListener { lastPage() }

        // Find the "favorite" ImageView, set a click listener on it and react when clicked
        favorite = findViewById(R.id.heart)
        favorite.setOnClickListener {
            if (WorkWithProverbs.getInstance().isBookmarked(thisProverb.id)) {
                unbookmark()
                favorite.setImageResource(notBookmarked)//switch to the heart contour icon
            } else {
                bookmark()
                favorite.setImageResource(yesBookmarked)//switch to the filled heart icon
            }
        }

        // Find the "show bookmarks" ImageView, set a click listener on it and react when clicked
        showBookmarks!!.setOnClickListener { showBookmarks() }

        // Find the "share" ImageView, set a click listener on it and react when clicked
        share.setOnClickListener { share() }

        notifications = findViewById(R.id.notifications)
        if (AllProverbs.instance!!.yesNotify()) {
            notifications!!.text = AllProverbs.instance!!.selectedTime
        } else {
            notifications!!.setText(R.string.notifications)
        }

        notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        val notifyButton: ToggleButton = findViewById(R.id.check_state)

        // Set up the Notification Broadcast Intent.
        val notifyIntent = Intent(this, AlarmReceiver::class.java)

        val alarmOn = PendingIntent.getBroadcast(
            this, NOTIFICATION_ID,
            notifyIntent, PendingIntent.FLAG_NO_CREATE
        ) != null
        notifyButton.isChecked = alarmOn
        notifyButton.isChecked = AllProverbs.instance!!.yesNotify()

        val notifyPendingIntent = PendingIntent.getBroadcast(
            this, NOTIFICATION_ID, notifyIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager

        // Set the click listener for the toggle button.
        notifyButton.setOnCheckedChangeListener { buttonView, isChecked ->
            val toastMessage: String
            if (isChecked) {
                val mcurrentTime = Calendar.getInstance()
                val hour = mcurrentTime[Calendar.HOUR_OF_DAY]
                val minute = mcurrentTime[Calendar.MINUTE]
                val timePickerDialog: TimePickerDialog
                timePickerDialog =
                    TimePickerDialog(
                        this@MainActivity,
                        OnTimeSetListener { timePicker, selectedHour, selectedMinute ->
                            if (alarmManager != null) {
                                val calendar =
                                    Calendar.getInstance()
                                calendar.timeInMillis = System.currentTimeMillis()
                                calendar[Calendar.HOUR_OF_DAY] = selectedHour
                                calendar[Calendar.MINUTE] = selectedMinute
                                if (Calendar.getInstance().after(calendar)) {
                                    calendar.add(Calendar.DAY_OF_MONTH, 1)
                                }
                                alarmManager.setRepeating(
                                    AlarmManager.RTC_WAKEUP, calendar.timeInMillis,
                                    AlarmManager.INTERVAL_DAY, notifyPendingIntent
                                )
                                AllProverbs.instance!!.saveYesNotify(true)
                                AllProverbs.instance!!
                                    .saveSelectedTime(selectedHour, selectedMinute)
                                val showTheTime: String =
                                    AllProverbs.instance!!.selectedTime
                                notifications!!.text = showTheTime
                            }
                        }, hour, minute, false
                    )
                timePickerDialog.setOnCancelListener {
                    AllProverbs.instance!!.saveYesNotify(false)
                    notifyButton.isChecked = false
                }
                timePickerDialog.setButton(
                    DialogInterface.BUTTON_POSITIVE, "OK" +
                            "", timePickerDialog
                )
                timePickerDialog.setButton(
                    DialogInterface.BUTTON_NEGATIVE,
                    "CANCEL",
                    timePickerDialog
                )
                timePickerDialog.show()

                // The toast message for the "on" case.
                toastMessage = getString(R.string.alarm_on_toast)
            } else {
                // Cancel notification if the alarm is turned off.
                notificationManager!!.cancelAll()
                AllProverbs.instance!!.saveYesNotify(false)
                AllProverbs.instance!!.deleteQuoteOfTheDay()
                AllProverbs.instance!!.deleteSelectedTime()
                notifications!!.setText(R.string.notifications)
                alarmManager.cancel(notifyPendingIntent)
                // The toast message for the "off" case.
                toastMessage = getString(R.string.alarm_off_toast)
            }
            showToast(
                this@MainActivity, toastMessage,
                Toast.LENGTH_LONG
            )
        }
        // Create the notification channel.
        createNotificationChannel()
    }

    fun makeFirstStep() {
        if (AllProverbs.instance!!.yesNotify() && AllProverbs.instance!!.findQuoteOfTheDay()
        ) {
            showQuoteOfTheDay()
            showToast(this, getString(R.string.quote_of_the_day), Toast.LENGTH_LONG)
        } else {
            goForward()
        }
    }

    private fun showQuoteOfTheDay() {
        thisProverb = WorkWithProverbs.getInstance().getQuoteOfTheDay()!!
        showToast(this, getString(R.string.quote_of_the_day), Toast.LENGTH_LONG)
        show(thisProverb)
    }

    private fun stayHere() {
        thisProverb = WorkWithProverbs.getInstance().getTheSame()!!
        show(thisProverb)
    }

    fun goForward() {
        thisProverb = WorkWithProverbs.getInstance().theNext
        show(thisProverb)
    }

    fun goBackwards() {
        thisProverb = WorkWithProverbs.getInstance().thePrevious
        show(thisProverb)
    }

    private fun firstPage() {
        thisProverb = WorkWithProverbs.getInstance().theFirst
        show(thisProverb)
    }

    private fun lastPage() {
        thisProverb = WorkWithProverbs.getInstance().theLast
        show(thisProverb)
    }

    private fun bookmark() {
        WorkWithProverbs.getInstance().addToFile()//add to the file
        WorkWithProverbs.getInstance()
            .readBookmarks()//update the listOfBookmarks array from the file
    }

    private fun unbookmark() {
        WorkWithProverbs.getInstance().removeFromArray()//remove from listOfBookmarks array
        WorkWithProverbs.getInstance().saveUpdatedList()//save to the file
    }

    private fun showBookmarks() {
        val intent = Intent(this, RecyclerViewActivity::class.java)
        startActivity(intent)
    }

    private fun share() {
        WorkWithProverbs.getInstance().share()
    }

    private fun show(thisProverb: Proverb) {

        // Every time a quote is shown, the app reads bookmarks from the file
        // to check if this quote has been bookmarked
        // in order to set the right heart icon
        WorkWithProverbs.getInstance().readBookmarks()

        favorite = findViewById(R.id.heart) // global ImageView

        proverb.text = thisProverb.proverb

        if (WorkWithProverbs.getInstance().isBookmarked(thisProverb.id)) {
            favorite.setImageResource(yesBookmarked)//set the filled heart
        } else {
            favorite.setImageResource(notBookmarked)//set the heart contour
        }
    }

    /**
     * Creates a Notification channel, for OREO and higher.
     */
    private fun createNotificationChannel() {

        // Create a notification manager object.
        notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        // Notification channels are only available in OREO and higher.
        // So, add a check on SDK version.
        if (Build.VERSION.SDK_INT >=
            Build.VERSION_CODES.O
        ) {

            // Create the NotificationChannel with all the parameters.
            val notificationChannel = NotificationChannel(
                PRIMARY_CHANNEL_ID,
                getString(R.string.channel_id),
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.GREEN
            notificationChannel.enableVibration(true)
            notificationChannel.description = getString(R.string.channel_description)
            notificationManager!!.createNotificationChannel(notificationChannel)
        }
    }

    private fun isOnline(): Boolean {
        val cm =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val netInfo = cm.activeNetworkInfo
        return netInfo != null && netInfo.isConnected
    }

    fun errorMessage(errorMessage: String) {
        object : CountDownTimer(5000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                window.setFlags(
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                )
                showToast(this@MainActivity, errorMessage, Toast.LENGTH_SHORT)
            }

            override fun onFinish() {
                if (Build.VERSION.SDK_INT >=
                    Build.VERSION_CODES.LOLLIPOP
                ) {
                    finishAndRemoveTask()
                } else finish()
            }
        }.start()
    }

    private fun showToast(
        context: Context,
        msg: String,
        duration: Int
    ) {
        val toast = Toast.makeText(context, msg, duration)
        val view = toast.view
        view.setBackgroundResource(android.R.drawable.toast_frame)
        view.setBackgroundColor(Color.TRANSPARENT)
        val text = view.findViewById<TextView>(android.R.id.message)
        text.background = context.resources.getDrawable(R.drawable.custom_toast)
        text.textSize = 16f
        toast.show()
    }

    companion object {

        // Notification ID.
        private const val NOTIFICATION_ID = 0

        // Notification channel ID.
        private const val PRIMARY_CHANNEL_ID = "primary_notification_channel"
        var instance: MainActivity? = null
            private set
    }
}

