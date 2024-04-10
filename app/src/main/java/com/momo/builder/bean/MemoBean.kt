package com.momo.builder.bean

import com.momo.builder.u.MmkvU
import org.json.JSONObject

class MemoBean(
    var title: String,
    var htmlContent: String,
    var content: String,
    var labelColor: String,
    var time: Long,
    var type: Int = 0
) {
    var isDone = false
    var isNewCreate = true
    var lockTime: Long = 0L
    var isLockOpen = false
    var clockId = MmkvU.getInt(MmkvU.CLOCK_ID) + 1
    var timeType = 0

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
        jsonObject.put("lockTime", lockTime)
        jsonObject.put("isLockOpen", isLockOpen)
        jsonObject.put("clockId", clockId)
        //更新clockId
        MmkvU.saveInt(MmkvU.CLOCK_ID, clockId)
        jsonObject.put("timeType", timeType)
        return jsonObject.toString()
    }
}