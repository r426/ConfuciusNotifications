package com.ryeslim.confuciusnotifications.model

import android.content.Context
import android.content.Intent
import com.ryeslim.confuciusnotifications.dataclass.Proverb
import java.io.*
import java.util.*
import kotlin.collections.ArrayList

class WorkWithProverbs

private constructor() {
    private var context: Context? = null

    private var index = -1

    // the list of proverbs seen during this session
    private val currentList: ArrayList<Proverb> = ArrayList()
    private var bookmarkIndex: Int = 0

    fun startClean() {
        currentList.clear()
        index = -1
    }

    fun getQuoteOfTheDay(): Proverb? {
        index++
        val fileName = "quote_of_the_day.txt"
        val file = File(context!!.filesDir, fileName)
        try {
            val reader = FileReader(file)
            val sc = Scanner(reader)
            while (sc.hasNext()) {
                val id = sc.nextShort()
                val proverb = sc.nextLine().trim { it <= ' ' }
                currentList.add(Proverb(id, proverb))
            }
            sc.close()
            reader.close()
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return currentList[index]
    }

    fun getTheSame(): Proverb? {
        return if (index >= 0) currentList[index] else theNext
    }

    val theNext: Proverb
        get() {
            index++
            if (index == currentList.size) {
                val proverb = AllProverbs.instance!!.random
                currentList.add(proverb)
            }
            return currentList[index]
        }

    val thePrevious: Proverb
        get() {
            index--
            if (index < 0) {
                index = currentList.size - 1
            }
            return currentList[index]
        }

    val theFirst: Proverb
        get() {
            index = 0
            return currentList[index]
        }

    val theLast: Proverb
        get() {
            index = currentList.size - 1
            return currentList[index]
        }

    private var listOfBookmarks: ArrayList<Proverb>? = null


    fun addToFile() {

        val fileName = "bookmarks.txt"
        var writer: FileWriter? = null
        val file = File(context!!.filesDir, fileName)
        try {
            writer = FileWriter(file, true)//will add to the existing information in the file

        } catch (e: IOException) {
            e.printStackTrace()
        }

        val prefix = "\n"

        if (writer != null) {
            try {
                writer.write("${currentList[index].id} ")
                writer.write("${currentList[index].proverb}$prefix")
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

    fun share() {
        val sendIntent = Intent()
        sendIntent.action = Intent.ACTION_SEND
        sendIntent.putExtra(Intent.EXTRA_TEXT, currentList[index].proverb)
        sendIntent.type = "text/plain"
        context!!.startActivity(Intent.createChooser(sendIntent, "Send to"))
    }

    /**
     * shares the quote from the favorites list
     * @param index – the position in the RecyclerView
     */
    fun share(index: Int) {
        val sendIntent = Intent()
        sendIntent.action = Intent.ACTION_SEND
        sendIntent.putExtra(
            Intent.EXTRA_TEXT, listOfBookmarks!![index].proverb
        )
        sendIntent.type = "text/plain"
        context!!.startActivity(Intent.createChooser(sendIntent, "Send to"))
    }

    fun setContext(context: Context) {
        this.context = context
    }

    //reads the saved favorites list from the file
    fun readBookmarks(): ArrayList<Proverb> {

        listOfBookmarks = ArrayList()
        val fileName = "bookmarks.txt"
        val file = File(context!!.filesDir, fileName)
        try {

            val reader = FileReader(file)
            val sc = Scanner(reader)

            while (sc.hasNext()) {
                val id = sc.nextShort()
                val proverb = sc.nextLine().trim { it <= ' ' }
                listOfBookmarks!!.add(Proverb(id, proverb))
            }
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }

        // listOfBookmarks is reversed "upside down" so that the newest saved proverbs are shown at the top
        listOfBookmarks!!.reverse()
        return listOfBookmarks!!
    }

    fun removeFromArray() {
        listOfBookmarks!!.removeAt(bookmarkIndex)
    }

    fun saveUpdatedList() {

        val fileName = "bookmarks.txt"
        var writer: FileWriter? = null
        val file = File(context!!.filesDir, fileName)
        try {
            writer = FileWriter(file, false)//will empty the file before writing

        } catch (e: IOException) {
            e.printStackTrace()
        }

        // listOfBookmarks is "upside down" so that the newest saved proverbs are shown at the top
        // but in the file the new proverbs are added to the end
        // that is why the the listOfProverbs should be reversed before writing to the file
        // and then reversed back to be ready to be shown on the screen
        listOfBookmarks!!.reverse()
        val listOfBookmarksCopy = listOfBookmarks?.toList()
        for (index in listOfBookmarks!!.indices) {
            val prefix = "\n"
            if (writer != null) {
                try {
                    writer.write("${listOfBookmarksCopy?.get(index)?.id} ")
                    writer.write("${listOfBookmarksCopy?.get(index)?.proverb}$prefix")
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
        listOfBookmarks!!.reverse()
        try {
            writer!!.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun isBookmarked(thisId: Short): Boolean {
        bookmarkIndex = 0
        while (bookmarkIndex < listOfBookmarks!!.size) {
            if (listOfBookmarks!![bookmarkIndex].id === thisId) return true
            bookmarkIndex++
        }
        return false
    }

    companion object {

        private var instance: WorkWithProverbs? = null

        //singleton
        fun getInstance(): WorkWithProverbs {
            if (instance == null) {
                instance =
                    WorkWithProverbs()
            }
            return instance!!
        }
    }
}
