package com.android.services.models

data class TamperCount(
    var count: Int = 0,
    var time: Long = 0L,
    var lastUnprotectedTime : Long = 0L
)
