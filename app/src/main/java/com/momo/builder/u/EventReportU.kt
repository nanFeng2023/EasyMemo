package com.momo.builder.u

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.telephony.TelephonyManager
import android.util.Log
import android.webkit.WebView
import com.google.android.gms.ads.AdValue
import com.lzy.okgo.OkGo
import com.lzy.okgo.cache.CacheMode
import com.lzy.okgo.callback.StringCallback
import com.momo.builder.BuildConfig
import com.momo.builder.bean.ResultBean
import com.momo.builder.memoApp
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import java.util.Locale
import java.util.UUID

object EventReportU {
    val land_show = "land_show"
    val land_stay = "land_stay"
    val home_show = "home_show"
    val home_stay = "home_stay"
    val home_add = "home_add"
    val home_set = "home_set"
    val home_search = "home_search"
    val home_note = "home_note"
    val text_show = "text_show"
    val text_stay = "text_stay"
    val text_theme = "text_theme"
    val text_delete = "text_delete"
    val text_deleted = "text_deleted"
    val text_save = "text_save"
    val text_autosave = "text_autosave"
    val text_button_tool = "text_button_tool"
    val set_privacy = "set_privacy"
    val set_contact = "set_contact"

    val install_event = "install_event"
    val session_event = "session_event"
    val open_ad_impression = "open_ad_impression"
    val home_ad_impression = "home_ad_impression"
    val inter_ad_impression = "inter_ad_impression"


    fun getAndroidID(context: Context): String {
        return Settings.System.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
            .ifEmpty { UUID.randomUUID().toString() }
    }

    fun getVersionName(context: Context): String =
        context.packageManager.getPackageInfo(context.packageName, 0).versionName

    //获取网络运营商
    private fun getYunYSName(context: Context): String {
        val manager =
            context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        val name = manager.simOperatorName
        return name.ifEmpty {
            manager.networkOperatorName
        }
    }

    private fun getUUID(): String {
        return UUID.randomUUID().toString()
    }

    private fun getLanguage(): String {
        return buildString {
            append(Locale.getDefault().language).append("_").append(Locale.getDefault().country)
        }
    }

    fun generalDataJson(context: Context): JSONObject {
        val json = JSONObject()
        //应用的版本
        json.put("nip", getVersionName(context))
        //用户排重字段
        json.put("stale", getAndroidID(context))
        //操作系统版本号
        json.put("lame", Build.VERSION.RELEASE)
        //网络供应商名称，mcc和mnc
        json.put("trigonal", getYunYSName(context))
        //日志唯一id，用于排重日志
        json.put("swart", getUUID())
        //手机厂商
        json.put("july", Build.MANUFACTURER)
        //日志发生的客户端时间，毫秒数
        json.put("fuel", System.currentTimeMillis())
        //手机型号
        json.put("mu", Build.MODEL)
        //gaid 没有开启google广告服务的设备获取不到，但是必须要尝试获取，用于归因，原值，google广告id
        json.put("felony", MmkvU.getStr(MmkvU.GAID))
        //获取language
        json.put("survivor", getLanguage())
        //操作系统；枚举值
        json.put("medici", "chaplain")
        //android App需要有该字段，原值 AndroidId
        json.put("schwab", getAndroidID(context))
        //当前的包名称，a.b.c
        json.put("safety", context.packageName)
        return json
    }

    fun installDataJson(context: Context): JSONObject {
        val genObj = generalDataJson(context)
        val obj = JSONObject()
        //系统构建版本，Build.ID， 以 build/ 开头
        obj.put("handicap", "build/${Build.ID}")
        //google referrer 中的 install_referrer
        obj.put("willow", MmkvU.getStr(MmkvU.INSTALL_REFERER))
        //google referrer 中的 install_version 部分
        obj.put("obstruct", MmkvU.getStr(MmkvU.INSTALL_VERSION))
        //webView中的user_agent, 注意为webView的，android中的userAgent有;wv关键字
        obj.put("annum", WebView(context).settings.userAgentString)
        //用户是否启用了限制跟踪，0：没有限制，1：限制了；映射关系：{“emblem”: 0, “annale”: 1}
        obj.put("siderite", MmkvU.getStr(MmkvU.LIMIT_TRACK).ifEmpty { "bleat" })
        //引荐来源网址点击事件发生时的客户端时间戳（以秒为单位）
        obj.put("punjab", MmkvU.getLong(MmkvU.CLICK_START_MS))
        //应用安装开始时的客户端时间戳（以秒为单位）
        obj.put("passim", MmkvU.getLong(MmkvU.APP_INSTALL_MS))
        //引荐来源网址点击事件发生时的服务器端时间戳（以秒为单位）
        obj.put("erasmus", MmkvU.getLong(MmkvU.CLICK_START_SERVER_MS))
        //应用安装开始时的服务器端时间戳（以秒为单位）
        obj.put("forest", MmkvU.getLong(MmkvU.APP_INSTALL_SERVER_MS))
        //应用首次安装的时间（以秒为单位）
        obj.put(
            "walla",
            context.packageManager.getPackageInfo(context.packageName, 0).firstInstallTime
        )
        //应用最后一次更新的时间（以秒为单位）
        obj.put(
            "voice",
            context.packageManager.getPackageInfo(context.packageName, 0).lastUpdateTime
        )
        //表明应用的免安装体验是否为过去 7 天内发布的
        obj.put("pacific", MmkvU.getBoolean(MmkvU.APP_7_DAY))
        //安装事件参数
        genObj.put("politico", obj)
        return genObj
    }

    fun sessionDataJson(context: Context): JSONObject {
        val genObj = generalDataJson(context)
        genObj.put("collar", JSONObject())
        return genObj
    }

    private fun getEarnType(type: Int): String {
        when (type) {
            0 -> {
                return "UNKNOWN"
            }

            1 -> {
                return "ESTIMATED"
            }

            2 -> {
                return "PUBLISHER_PROVIDED"
            }

            3 -> {
                return "PRECISE"
            }
        }
        return ""
    }

    fun advertiseDataJson(
        context: Context,
        resultBean: ResultBean,
        adValue: AdValue,
        platform: String
    ): JSONObject {
        val genObj = generalDataJson(context)
        val obj = JSONObject()
        //预估收入，需要满足上报结果是收入 * 10^6
        obj.put("mutton", adValue.valueMicros)
        //预估收益的货币单位
        obj.put("hager", adValue.currencyCode)
        //广告网络，广告真实的填充平台，例如admob的bidding，填充了Facebook的广告，此值为Facebook
        obj.put(
            "jet", if (platform.contains("Facebook") || platform.contains("facebook")) {
                "Facebook"
            } else {
                "adMob"
            }
        )
        //广告SDK，admob，max等
        obj.put("cleric", resultBean.adBean.fE2_oesop)
        //广告位id
        obj.put("trite", resultBean.adBean.fE2_crass)
        //广告位逻辑编号，例如：page1_bottom, connect_finished
        obj.put("junk", resultBean.bigType)
        //真实广告网络返回的广告id，海外获取不到，不传递该字段
        obj.put("morphine", "")
        //广告场景，置空
        obj.put("colossal", "")
        //广告类型，插屏，原生，banner，激励视频等
        obj.put("lamb", resultBean.adBean.fE2_anothe)
        //google ltvpingback的预估收益类型
        obj.put("nausea", getEarnType(adValue.precisionType))
        //广告加载时候的ip地址
//        obj.put("league", advertise?.adLoadingIp)
        //广告显示时候的ip地址
//        obj.put("purple", placeholderBean.adShowIp)
        obj.put("empty", "22.2.0")
        genObj.put("hutchins", obj)
        return genObj
    }

    private val tabUrl =
        if (BuildConfig.DEBUG) "https://test-quiz.easymemoconvenient.com/tang/reverent/timex"
        else "https://quiz.easymemoconvenient.com/joggle/brought/rsvp"

    fun reportData(
        jsonString: String,
        onSuccess: (() -> Unit)? = null,
        onFail: (() -> Unit)? = null,
        eventName: String? = null
    ) {
        OkGo.post<String>(tabUrl)
            .upJson(jsonString)
            .headers("safety", getAndroidID(memoApp))
            .headers("survivor", getLanguage())
            .headers("tactile", ip)
            .params("laos", "")
            .params("july", Build.MANUFACTURER)
            .params("sentence", Build.BRAND)
            .tag(this)
            .cacheMode(CacheMode.NO_CACHE)
            .execute(object : StringCallback() {
                override fun onSuccess(response: com.lzy.okgo.model.Response<String>?) {
                    if (response?.isSuccessful == true) {
                        onSuccess?.invoke()
                    }
                }

                override fun onError(response: com.lzy.okgo.model.Response<String>?) {
                    super.onError(response)
                    onFail?.invoke()
                }
            })

    }


    var ip = ""
    fun requestIp(onSuccess: () -> Unit) {
        OkHttpClient().newCall(Request.Builder().url("https://ipinfo.io/json").get().build())
            .enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Log.d("-----", "requestIp---onFailure---e:${e.printStackTrace()}")
                }

                override fun onResponse(call: Call, response: Response) {
                    if (response.isSuccessful) {
                        val string = response.body?.string()
                        if (!string.isNullOrBlank()) {
                            kotlin.runCatching {
                                val jsonObject = JSONObject(string)
                                ip = jsonObject.optString("ip")
                                Log.d("-----", "requestIp---onResponse---ip:$ip")
                            }
                            onSuccess.invoke()
                        }
                    }
                }
            })
    }

    fun reportCustomEvent(eventName: String, bundle: Bundle? = null) {
        val genObj = generalDataJson(memoApp)
        genObj.put("eclipse", eventName)
        if (bundle != null && bundle.getLong("time") != 0L) {
            val jsonObject = JSONObject()
            jsonObject.put("time", bundle.getLong("time") / 1000)
            genObj.put("coconut", jsonObject)
        }
        reportData(genObj.toString(), eventName = eventName)
    }


}