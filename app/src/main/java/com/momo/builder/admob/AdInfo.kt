package com.momo.builder.admob

import com.momo.builder.bean.AdBean
import com.momo.builder.conf.LocalConf
import com.momo.builder.u.MmkvU
import com.momo.builder.u.key
import org.json.JSONObject

object AdInfo {
    private var maxS=40
    private var maxC=6

    private var click=0
    private var show=0

    var openAdShowing=false

    private val nativeAdRefresh= hashMapOf<String,Boolean>()

    fun setMax(string: String){
        runCatching {
            val jsonObject = JSONObject(string)
            maxS=jsonObject.optInt("fE2_amp")
            maxC=jsonObject.optInt("fE2_spend")
            MmkvU.saveStr("fE2_epifa",string)
        }
    }

    fun addNum(isClick:Boolean){
        if (isClick){
            click++
            MmkvU.saveInt(key("click"),click)
        }else{
            show++
            MmkvU.saveInt(key("show"),show)
        }
    }

    fun getInt(){
        click=MmkvU.getInt(key("click"))
        show=MmkvU.getInt(key("show"))
    }

    fun isLimit()=click>= maxC||show>= maxS

    fun getAdList(type: String):List<AdBean>{
        val list= arrayListOf<AdBean>()
        runCatching {
            val jsonArray = JSONObject(getAd()).getJSONArray(type)
            for (index in 0 until jsonArray.length()){
                val jsonObject = jsonArray.getJSONObject(index)
                list.add(
                    AdBean(
                        jsonObject.optString("fE2_anothe"),
                        jsonObject.optString("fE2_oesop"),
                        jsonObject.optString("fE2_crass"),
                        jsonObject.optInt("fE2_dipso"),
                    )
                )
            }
        }
        return list.filter { it.fE2_oesop == "admob" }.sortedByDescending { it.fE2_dipso }
    }

    private fun getAd():String{
        val str = MmkvU.getStr("fE2_epifa")
        if (str.isEmpty()){
            return LocalConf.ad
        }
        return str
    }

    fun canLoadNativeAd(type: String) = nativeAdRefresh[type]?:true

    fun clearNativeMap(){
        nativeAdRefresh.clear()
    }

    fun setNativeAdBool(type:String,boolean: Boolean){
        nativeAdRefresh[type]=boolean
    }
}