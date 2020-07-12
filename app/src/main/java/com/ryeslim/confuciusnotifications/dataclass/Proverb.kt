package com.ryeslim.confuciusnotifications.dataclass

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class Proverb(

    @Expose
    @SerializedName("id")
    val id: Short,
    @Expose
    @SerializedName("text")
    val proverb: String
)
