package com.ryeslim.confuciusnotifications.model

import android.content.Context
import com.ryeslim.confuciusnotifications.activities.MainActivity
import com.ryeslim.confuciusnotifications.dataclass.Proverb
import com.ryeslim.confuciusnotifications.retrofit.ProverbApi
import com.ryeslim.confuciusnotifications.retrofit.ServiceFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.*
import java.lang.Math.floor
import java.util.*

class AllProverbs private constructor() {
    private val listOfAll: ArrayList<Proverb> = ArrayList()

    //    private var id: Short = 0
    //    private var proverb: String? = null
    private var context: Context? = null
    fun setContext(context: Context?) {
        this.context = context
    }

    val random: Proverb
        get() {
            val result: Proverb
            val i = kotlin.math.floor(Math.random() * listOfAll.size).toInt()
            result = listOfAll[i]
            return result
        }

    suspend fun fetchProverb() {
        var response: retrofit2.Response<List<Proverb>>? = null
        try {
            withContext(Dispatchers.IO) {
                response = ServiceFactory.createRetrofitService(
                    ProverbApi::class.java,
                    "https://api.npoint.io/"
                )
                    .getProverbAsync().await()
            }
            if (response != null) {
                if (response!!.isSuccessful) {
                    if (response!!.body() != null) {
                        listOfAll.addAll(response!!.body()!!)
                        saveUpdatedListOfAll()
                        saveUpdatedOn()
                        MainActivity.instance!!.makeFirstStep()
                    }
                } else {
                    MainActivity.instance!!.errorMessage("Server error. Please try later")
                }
            }

        } catch (e: Exception) {
            e.stackTrace
        }
    }

    private fun saveUpdatedListOfAll() {
        val fileName = "all_quotes.txt"
        var writer: FileWriter? = null
        val file = File(context!!.filesDir, fileName)
        try {
            writer = FileWriter(file, false) //will empty the file before writing
        } catch (e: IOException) {
            e.printStackTrace()
        }
        for (index in listOfAll.indices) {
            val prefix = "\n"
            if (writer != null) {
                try {
                    writer.write(listOfAll[index].id.toString() + " ")
                    writer.write(listOfAll[index].proverb + prefix)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
        try {
            writer!!.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun quoteFileReadable(): Boolean {
        val quoteFileName = "all_quotes.txt"
        val quoteFile = File(context!!.filesDir, quoteFileName)

        // checks if the quote file exists and is readable
        return quoteFile.isFile && quotesFromFileOK()
    }

    fun quotesUpToDate(): Boolean {
        val updatedOnFileName = "updated_on.txt"
        val updatedOn = File(context!!.filesDir, updatedOnFileName)

        // checks if the data is up to date
        return updatedOn.isFile && isUpToDate
    }

    //reads the saved quotes from the file and
    // assigns them to the ListOfAll array
    private fun quotesFromFileOK(): Boolean {
        var quotesOK = false
        val fileName = "all_quotes.txt"
        val file = File(context!!.filesDir, fileName)
        try {
            val reader = FileReader(file)
            val sc = Scanner(reader)
            while (sc.hasNext()) {
                val id = sc.nextShort()
                val proverb = sc.nextLine().trim { it <= ' ' }
                listOfAll.add(Proverb(id, proverb))
            }
            sc.close()
            reader.close()
            quotesOK = true
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return quotesOK
    }

    private val isUpToDate: Boolean
        get() {
            val tenDays = 1000 * 60 * 60 * 24 * 10.toLong()
            var updatedOn = 0L
            val fileName = "updated_on.txt"
            var file: FileInputStream? = null
            try {
                file = FileInputStream(context!!.getFileStreamPath(fileName))
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            }
            val data = DataInputStream(file)
            try {
                updatedOn = data.readLong()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            try {
                data.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            try {
                file!!.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            return System.currentTimeMillis() - updatedOn < tenDays
        }

    private fun saveUpdatedOn() {
        val updatedOn = System.currentTimeMillis()
        val fileName = "updated_on.txt"
        var streamFile: FileOutputStream? = null
        val file = File(context!!.filesDir, fileName)
        try {
            streamFile = FileOutputStream(file)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        val data = DataOutputStream(streamFile)
        try {
            data.writeLong(updatedOn)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        try {
            data.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun makeNotifyReadable() {
        val fileName = "selected_time.txt"
        val timeFile = File(context!!.filesDir, fileName)
        if (!timeFile.isFile) saveYesNotify(false)
    }

    fun yesNotify(): Boolean {
        val fileName = "notify.txt"
        var b = false
        var file: FileInputStream? = null
        try {
            file = FileInputStream(context!!.getFileStreamPath(fileName))
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }
        val data = DataInputStream(file)
        try {
            b = data.readBoolean()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        try {
            data.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        try {
            file!!.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return b
    }

    fun saveYesNotify(notify: Boolean) {
        val fileName = "notify.txt"
        var streamFile: FileOutputStream? = null
        val file = File(context!!.filesDir, fileName)
        try {
            streamFile = FileOutputStream(file)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        val data = DataOutputStream(streamFile)
        try {
            data.writeBoolean(notify)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        try {
            data.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun deleteQuoteOfTheDay() {
        val fileName = "quote_of_the_day.txt"
        val quoteFile = File(context!!.filesDir, fileName)
        quoteFile.delete()
    }

    fun findQuoteOfTheDay(): Boolean {
        val fileName = "quote_of_the_day.txt"
        val quoteFile = File(context!!.filesDir, fileName)
        return quoteFile.isFile
    }

    fun saveSelectedTime(selectedHour: Int, selectedMinute: Int) {
        var selectedHour = selectedHour
        val fileName = "selected_time.txt"
        var writer: FileWriter? = null
        val file = File(context!!.filesDir, fileName)
        try {
            writer = FileWriter(file, false) //will empty the file before writing
        } catch (e: IOException) {
            e.printStackTrace()
        }
        val prefix = "\n"
        var timeOfTheDay = ""
        timeOfTheDay = if (selectedHour < 12) "am" else "pm"
        if (selectedHour == 0) selectedHour += 12 else if (selectedHour > 12) selectedHour -= 12
        if (writer != null) {
            try {
                writer.write(selectedHour.toString() + prefix)
                writer.write(selectedMinute.toString() + prefix)
                writer.write(timeOfTheDay + prefix)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        try {
            writer!!.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    val selectedTime: String
        get() {
            var selectedHour = ""
            var selectedMinute = ""
            var timeOfTheDay = ""
            val fileName = "selected_time.txt"
            val file = File(context!!.filesDir, fileName)
            try {
                val reader = FileReader(file)
                val sc = Scanner(reader)
                while (sc.hasNext()) {
                    selectedHour = sc.nextLine().trim { it <= ' ' }
                    selectedMinute = sc.nextLine().trim { it <= ' ' }
                    timeOfTheDay = sc.nextLine().trim { it <= ' ' }
                }
                sc.close()
                reader.close()
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            if (selectedMinute.length == 1) {
                selectedMinute = "0$selectedMinute"
            }
            val showTheTime = "$selectedHour:$selectedMinute$timeOfTheDay"
            println(showTheTime)
            return showTheTime
        }

    fun deleteSelectedTime() {
        val fileName = "selected_time.txt"
        val timeFile = File(context!!.filesDir, fileName)
        timeFile.delete()
    }

    companion object {
        var instance: AllProverbs? = null
            get() {
                if (field == null) {
                    field = AllProverbs()
                }
                return field
            }
            private set
    }

    init {
        listOfAll.ensureCapacity(700)
    }
}