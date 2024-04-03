package com.momo.builder.bean

import org.json.JSONObject

class MemoBean(
    var title: String,
    var htmlContent: String,
    var content: String,
    var labelColor: String,
    val time: Long,
    var type: Int = 0
) {
    var isDone = false
    var isNewCreate = false
    fun getJsonStr(): String {
        val jsonObject = JSONObject()
        jsonObject.put("title", title)
        jsonObject.put("htmlContent", htmlContent)
        jsonObject.put("content", content)
        jsonObject.put("labelColor", labelColor)
        jsonObject.put("time", time)
        jsonObject.put("type", type)
        jsonObject.put("isDone", isDone)
        jsonObject.put("isNewCreate", isNewCreate)
        return jsonObject.toString()
    }
}