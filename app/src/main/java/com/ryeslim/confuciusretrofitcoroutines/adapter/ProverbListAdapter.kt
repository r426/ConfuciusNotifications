package com.ryeslim.confuciusretrofitcoroutines.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ryeslim.confuciusretrofitcoroutines.R
import com.ryeslim.confuciusretrofitcoroutines.dataclass.Proverb
import com.ryeslim.confuciusretrofitcoroutines.model.WorkWithProverbs
import kotlinx.android.synthetic.main.one_item.view.*

//Developed following the Simple RecyclerView Android Example in Kotlin by SANTOSH DAHAL
//https://www.suntos.com.np/kotlin-android-sample-projects-with-source-code-in-android-studio/simple-recyclerview-android-example-in-kotlin-source-code.html

//Adapters are used to connect data with View items in a list.
//The adapter receives 1) the context of the activity and 2) the array of objects.
//It will bind our data to the populated ViewHolders in the RecyclerView
class ProverbListAdapter(context: Context, private val proverbList: ArrayList<Proverb>) :
    RecyclerView.Adapter<ProverbListAdapter.ProverbViewHolder>() {

    private val mContext: Context

    init {
        mContext = context
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProverbViewHolder {
        //LayoutInflator inflates the XML for a list item.
        val proverbRow: View =
            LayoutInflater.from(mContext).inflate(R.layout.one_item, parent, false)
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
        holder.buttonInTheHolder.setOnClickListener {
            proverbList.removeAt(position)
            //Notify the adapter, that the data has changed so it can
            //update the RecyclerView to display the data.
            notifyDataSetChanged()
            WorkWithProverbs.getInstance().saveTheUpdatedList()
        }
    }

    //ProverbViewHolder populates the UI from the one_item.xml file
    inner class ProverbViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val textViewInTheHolder = v.text_in_the_item
        val buttonInTheHolder = v.button_to_delete_one
    }
}