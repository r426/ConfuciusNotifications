package com.ryeslim.confuciusretrofitcoroutines.dataclass

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class Proverb(

    @Expose
    @SerializedName("id")
    val theID: Short,
    @Expose
    @SerializedName("text")
    val proverb: String
)
