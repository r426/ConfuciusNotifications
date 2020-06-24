package com.ryeslim.confuciusretrofitcoroutines.model

import android.content.Context
import android.content.Intent
import com.ryeslim.confuciusretrofitcoroutines.dataclass.Proverb
import java.io.*
import java.util.*
import kotlin.collections.ArrayList

class WorkWithProverbs//constructor
private constructor() {

    private var index = -1
    private val theCurrentList: ArrayList<Proverb> =
        ArrayList()//the list of proverbs seen during this session
    private var context: Context? = null
    private var bookmarkIndex: Int = 0

    val theNext: Proverb
        get() {
            index++
            if (index == theCurrentList.size) {
                val proverb = AllProverbs.getInstance().random
                theCurrentList.add(proverb)
            }
            return theCurrentList[index]
        }

    val thePrevious: Proverb
        get() {
            index--
            if (index < 0) {
                index = theCurrentList.size - 1
            }
            return theCurrentList[index]
        }

    val theFirst: Proverb
        get() {
            index = 0
            return theCurrentList[index]
        }

    val theLast: Proverb
        get() {
            index = theCurrentList.size - 1
            return theCurrentList[index]
        }

    private var listOfBookmarks: ArrayList<Proverb>? = null


    fun addToTheFile() {

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
                writer.write("${theCurrentList[index].theID} ")
                writer.write("${theCurrentList[index].proverb}$prefix")
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

    //adapted from https://developer.android.com/training/sharing/send#java
    fun shareSomehow() {
        val sendIntent = Intent()
        sendIntent.action = Intent.ACTION_SEND
        sendIntent.putExtra(Intent.EXTRA_TEXT, theCurrentList[index].proverb)
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
                val mID = sc.nextShort()
                val mProverb = sc.nextLine().trim { it <= ' ' }
                listOfBookmarks!!.add(
                    Proverb(
                        mID,
                        mProverb
                    )
                )
            }
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }

        // listOfBookmarks is reversed "upside down" so that the newest saved proverbs are shown at the top
        Collections.reverse(listOfBookmarks)
        return listOfBookmarks!!
    }

    fun removeFromArray() {
        listOfBookmarks!!.removeAt(bookmarkIndex)
    }

    fun saveTheUpdatedList() {

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
        Collections.reverse(listOfBookmarks)
        val listOfBookmarksCopy = listOfBookmarks?.toList()
        for (index in listOfBookmarks!!.indices) {
            val prefix = "\n"
            if (writer != null) {
                try {
                    writer.write("${listOfBookmarksCopy?.get(index)?.theID} ")
                    writer.write("${listOfBookmarksCopy?.get(index)?.proverb}$prefix")
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
        Collections.reverse(listOfBookmarks)
        try {
            writer!!.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun isBookmarked(thisID: Short): Boolean {
        bookmarkIndex = 0
        while (bookmarkIndex < listOfBookmarks!!.size) {
            if (listOfBookmarks!![bookmarkIndex].theID === thisID) return true
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
