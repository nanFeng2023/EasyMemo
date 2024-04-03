package com.momo.builder.u

import android.util.Log
import android.view.View
import com.momo.builder.BuildConfig
import java.text.SimpleDateFormat
import java.util.*

fun logMemo(string: String) {
    if (BuildConfig.DEBUG) {
        Log.e("memo", string)
    }
}

fun View.show(show: Boolean) {
    visibility = if (show) View.VISIBLE else View.GONE
}

fun key(string: String) = "${string}...${
    SimpleDateFormat(
        "yyyy-MM-dd",
        Locale.getDefault()
    ).format(Date(System.currentTimeMillis()))
}"
