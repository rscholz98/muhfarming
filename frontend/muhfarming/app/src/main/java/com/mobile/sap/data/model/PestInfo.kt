package com.mobile.sap.data.model

import com.google.gson.annotations.SerializedName

data class PestInfoResponse(
    @SerializedName("pests")
    val pests: List<PestInfo>
)

data class PestInfo(
    @SerializedName("pest-name")
    val pestName: String,
    @SerializedName("pest-description")
    val pestDescription: String,
    @SerializedName("season")
    val season: String,
    @SerializedName("severity")
    val severity: String
)
