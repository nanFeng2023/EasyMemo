package com.momo.builder.bean

import org.json.JSONObject

class MemoBean(
    var title: String,
    var htmlContent: String,
    var content: String,
    var labelColor: String,
    val time: Long,
) {

    fun getJsonStr(): String {
        val jsonObject = JSONObject()
        jsonObject.put("title", title)
        jsonObject.put("htmlContent", htmlContent)
        jsonObject.put("content", content)
        jsonObject.put("labelColor", labelColor)
        jsonObject.put("time", time)
        return jsonObject.toString()
    }
}