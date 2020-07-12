package com.ryeslim.confuciusnotifications.adapter

import android.content.Context
import android.content.DialogInterface
import android.content.DialogInterface.OnShowListener
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.ryeslim.confuciusnotifications.R
import com.ryeslim.confuciusnotifications.dataclass.Proverb
import com.ryeslim.confuciusnotifications.model.WorkWithProverbs
import kotlinx.android.synthetic.main.one_item.view.*
import java.security.AccessController.getContext


//Adapters are used to connect data with View items in a list.
//The adapter receives 1) the context of the activity and 2) the array of objects.
//It will bind our data to the populated ViewHolders in the RecyclerView
class ProverbListAdapter(context: Context, private val proverbList: ArrayList<Proverb>) :
    RecyclerView.Adapter<ProverbListAdapter.ProverbViewHolder>() {

    private val thisContext: Context = context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProverbViewHolder {
        //LayoutInflator inflates the XML for a list item.
        val proverbRow: View =
            LayoutInflater.from(thisContext).inflate(R.layout.one_item, parent, false)
        return ProverbViewHolder(proverbRow)
    }

    override fun getItemCount(): Int {
        return proverbList.size
    }

    //onBindViewHolder takes a data collection and
    //applies a rotating rendering of visible data applied to the ViewHolders.

    override fun onBindViewHolder(holder: ProverbViewHolder, position: Int) {
        val thisProverb = proverbList[position]
        holder.textViewInTheHolder!!.text = thisProverb.proverb

        holder.shareViewInTheHolder.setOnClickListener {
            WorkWithProverbs.getInstance().share(position)
        }

        holder.deleteButtonInTheHolder.setOnClickListener {

            val alertDialogBuilder = AlertDialog.Builder(this.thisContext)
            alertDialogBuilder.setMessage(R.string.delete_from_favorites)

            // add the buttons
            alertDialogBuilder
                .setPositiveButton(R.string.delete) { dialog, which ->
                    proverbList.removeAt(position)
                    notifyDataSetChanged()
                    WorkWithProverbs.getInstance().saveUpdatedList()
                }
                .setNegativeButton(R.string.no) { dialog, id ->
                    // User cancelled the dialog
                }
                .setOnCancelListener { }

            // create and show the alert dialog
            val alertDialog = alertDialogBuilder.create()
            alertDialog.getWindow()!!.setDimAmount(0.2f)
            alertDialog.show()

            val buttonYes = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE)
            val buttonNo = alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE)
            with(buttonYes) {
                setTextColor(android.graphics.Color.rgb(4, 36, 65))
            }
            with(buttonNo) {
                setTextColor(android.graphics.Color.rgb(4, 36, 65))
            }
        }
    }

    //ProverbViewHolder populates the UI from the one_item.xml file
    inner class ProverbViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val textViewInTheHolder = v.proverb
        val shareViewInTheHolder = v.share
        val deleteButtonInTheHolder = v.delete
    }
}