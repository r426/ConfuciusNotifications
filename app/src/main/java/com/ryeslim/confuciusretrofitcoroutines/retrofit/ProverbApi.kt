package com.ryeslim.confuciusretrofitcoroutines.retrofit

import com.ryeslim.confuciusretrofitcoroutines.dataclass.Proverb
import kotlinx.coroutines.Deferred
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.GET

interface ProverbApi {

    @GET("d98f6ae8641988b97604")
    fun getProverbAsync(): Deferred<Response<List<Proverb>>>

}
