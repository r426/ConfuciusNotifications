package com.ryeslim.confuciusretrofitcoroutines.model

import com.ryeslim.confuciusretrofitcoroutines.activities.MainActivity
import com.ryeslim.confuciusretrofitcoroutines.dataclass.Proverb
import com.ryeslim.confuciusretrofitcoroutines.retrofit.ProverbApi
import com.ryeslim.confuciusretrofitcoroutines.retrofit.ServiceFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.lang.Math.floor


class AllProverbs() {

    val theListOfAll = ArrayList<Proverb>()

    val random: Proverb
        get() {
            val result: Proverb
            val i = floor(Math.random() * theListOfAll.size).toInt()
            result = theListOfAll[i]
            return result
        }

    init {
        theListOfAll.ensureCapacity(1000)
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
                        theListOfAll.addAll(response!!.body()!!)
                        MainActivity.instance?.goForward()
                    }
                } else {
                    //
                }
            }

        } catch (e: Exception) {
            e.stackTrace
            //
        }
    }

    companion object {

        private var instance: AllProverbs? = null

        fun getInstance(): AllProverbs {
            if (instance == null) {
                instance =
                    AllProverbs()
            }
            return instance!!
        }
    }
}
