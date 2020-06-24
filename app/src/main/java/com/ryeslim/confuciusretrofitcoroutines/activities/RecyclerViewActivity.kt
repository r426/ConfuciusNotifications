package com.ryeslim.confuciusretrofitcoroutines.activities

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ryeslim.confuciusretrofitcoroutines.R
import com.ryeslim.confuciusretrofitcoroutines.adapter.ProverbListAdapter
import com.ryeslim.confuciusretrofitcoroutines.model.WorkWithProverbs

class RecyclerViewActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recyclerview)

        // 1) Create an adapter, whose data source is a list of Proverb objects.
        val adapter = ProverbListAdapter(
            this,
            WorkWithProverbs.getInstance().readBookmarks()
        )

        // 2) Find the RecyclerView object (the location where to drop the whole list)
        val recyclerView = findViewById<View>(R.id.recycler_view) as RecyclerView

        // 3) Give the RecyclerView a default layout manager.
        recyclerView.layoutManager = LinearLayoutManager(this)

        // 4) Make the RecyclerView use the adapter
        recyclerView.adapter = adapter
    }

    //When < icon is clicked, returns to MainActivity
    fun carryOn(view: View) {
        this.finish()
    }
}
