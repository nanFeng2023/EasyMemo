package com.momo.builder.u

import com.momo.builder.bean.MemoBean
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object MemoU {
    val list = arrayListOf<MemoBean>()
    var clickMemo: MemoBean? = null

    fun getLocalMemoList() {
        runCatching {
            val jsonArray = JSONArray(MmkvU.getStr("memo"))
            for (index in 0 until jsonArray.length()) {
                val jsonObject = JSONObject(jsonArray.getString(index))
                val memo = MemoBean(
                    jsonObject.optString("title"),
                    jsonObject.optString("htmlContent"),
                    jsonObject.optString("content"),
                    jsonObject.optString("labelColor"),
                    jsonObject.optLong("time"),
                    jsonObject.optInt("type"),
                )
                memo.isDone = jsonObject.optBoolean("isDone")
                memo.isNewCreate = jsonObject.optBoolean("isNewCreate")
                memo.lockTime = jsonObject.optLong("lockTime")
                memo.isLockOpen = jsonObject.optBoolean("isLockOpen")
                memo.clockId = jsonObject.optInt("clockId")
                memo.timeType = jsonObject.optInt("timeType")
                list.add(memo)
            }
        }
        firstInitTwoMemo()
    }

    fun saveMemo(memoBean: MemoBean) {
        val clickIndex = getClickIndex()
        if (clickIndex >= 0) {
            val bean = list[clickIndex]
            bean.title = memoBean.title
            bean.htmlContent = memoBean.htmlContent
            bean.content = memoBean.content
            bean.labelColor = memoBean.labelColor
            bean.type = memoBean.type
            bean.isDone = memoBean.isDone
            bean.isNewCreate = memoBean.isNewCreate
            bean.isLockOpen = memoBean.isLockOpen
            bean.lockTime = memoBean.lockTime
        } else {
            list.add(memoBean)
            clickMemo = memoBean
        }
        saveList()
    }

    fun search(key: String): ArrayList<MemoBean> {
//        if (key.isEmpty()) {
//            return list
//        }
        val searchList = arrayListOf<MemoBean>()
        list.forEach {
            if (it.title.lowercase(Locale.getDefault())
                    .contains(key.lowercase(Locale.getDefault())) || it.content.lowercase(
                    Locale.getDefault()
                )
                    .contains(key.lowercase(Locale.getDefault()))
            ) {
                searchList.add(it)
            }
        }
        return searchList
    }

    fun delete() {
        val clickIndex = getClickIndex()
        if (clickIndex >= 0) {
            list.removeAt(clickIndex)
            saveList()
        }
    }

    private fun getClickIndex(): Int {
        var cuindex = -1
        if (null != clickMemo) {
            for (index in 0 until list.size) {
                if (list[index].time == clickMemo?.time) {
                    cuindex = index
                    break
                }
            }
        }
        return cuindex
    }

    private fun saveList() {
        val jsonArray = JSONArray()
        list.forEach { jsonArray.put(it.getJsonStr()) }
        MmkvU.saveStr("memo", jsonArray.toString())
    }

    private fun firstInitTwoMemo() {
        if (MmkvU.getStr("first").isNotEmpty()) {
            return
        }
        //"<html><body><p><span style=\"color:#575757;\">yggg</span></p></body></html>"
        list.add(
            MemoBean(
                title = "Work List",
                htmlContent = "<html><body><p><span style=\"color:#333333;\">Today ${obtainDate()}. Click the button on the right to finish.</span></p></body></html>",
                content = "Today ${obtainDate()}. Click the button on the right to finish.",
                labelColor = "#F36E9D",
                time = System.currentTimeMillis() + 100, type = 1
            ).apply {
                lockTime = System.currentTimeMillis()
            }
        )
        list.add(
            MemoBean(
                title = "User tutorial",
                htmlContent = "<html><body><p><span style=\"color:#333333;\">Click in to change colors, record your life, and manage your to-do list</span></p></body></html>",
                content = "Click in to change colors, record your life, and manage your to-do list",
                labelColor = "#F3EDEF",
                time = System.currentTimeMillis() + 200, type = 1
            ).apply {
                lockTime = System.currentTimeMillis()
            }
        )
        list.add(
            MemoBean(
                title = "Welcome to Easy Memo",
                htmlContent = "<html><body><p><span style=\"color:#333333;\">This is an example text.You can delete it by clicking in it.</span></p></body></html>",
                content = "This is an example text.You can delete it by clicking in it.",
                labelColor = "#E6F2FF",
                time = System.currentTimeMillis() + 300, type = 0
            )
        )
        MmkvU.saveStr("first", "1")
        saveList()
    }

    fun obtainDate(): String =
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
}