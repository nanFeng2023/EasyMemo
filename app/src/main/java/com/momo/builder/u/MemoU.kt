package com.momo.builder.u

import com.momo.builder.bean.MemoBean
import org.json.JSONArray
import org.json.JSONObject
import java.util.Locale

object MemoU {
    val list = arrayListOf<MemoBean>()
    var clickMemo: MemoBean? = null

    fun getLocalMemoList() {
        runCatching {
            val jsonArray = JSONArray(MmkvU.getStr("memo"))
            for (index in 0 until jsonArray.length()) {
                val jsonObject = JSONObject(jsonArray.getString(index))
                list.add(
                    MemoBean(
                        jsonObject.optString("title"),
                        jsonObject.optString("htmlContent"),
                        jsonObject.optString("content"),
                        jsonObject.optString("labelColor"),
                        jsonObject.optLong("time"),
                    )
                )
            }
        }
        firstInitTwoMemo()
    }

    fun saveMemo(
        title: String,
        htmlContent: String,
        content: String,
        labelColor: String
    ) {
        val clickIndex = getClickIndex()
        if (clickIndex >= 0) {
            val bean = list[clickIndex]
            bean.title=title
            bean.htmlContent = htmlContent
            bean.content = content
            bean.labelColor = labelColor
        } else {
            val memoBean =
                MemoBean(title, htmlContent, content, labelColor, System.currentTimeMillis())
            list.add(memoBean)
            clickMemo = memoBean
        }
        saveList()
    }

    fun search(key: String): ArrayList<MemoBean> {
        if (key.isEmpty()) {
            return list
        }
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
                title = "1. Welcome to Easy Memo.",
                htmlContent = "<html><body><p><span style=\"color:#575757;\">This is a sample note to help you get started with our service.</span></p></body></html>",
                content = "This is a sample note to help you get started with our service.",
                labelColor = "#F36E9D",
                time = System.currentTimeMillis()
            )
        )
        list.add(
            MemoBean(
                title = "2. How to create a new note?",
                htmlContent = "<html><body><p><span style=\"color:#575757;\">Tap the \"➕\" button below, enter your information, and click \"✔️\" to save.</span></p></body></html>",
                content = "Tap the \"➕\" button below, enter your information, and click \"✔\uFE0F\" to save.",
                labelColor = "#F36E9D",
                time = System.currentTimeMillis()
            )
        )
        list.add(
            MemoBean(
                title = "3. How to edit an existing note?",
                htmlContent = "<html><body><p><span style=\"color:#575757;\">Simply tap on a note to view/edit its content.</span></p></body></html>",
                content = "Simply tap on a note to view/edit its content.",
                labelColor = "#F36E9D",
                time = System.currentTimeMillis()
            )
        )
        MmkvU.saveStr("first", "1")
        saveList()
    }
}