package com.momo.builder.bean

class ResultBean(
    val ad: Any? = null,
    val time: Long = 0L,
    val adBean: AdBean,
    var bigType: String
) {
    fun isEx() = (System.currentTimeMillis() - time) >= 3600000L
}