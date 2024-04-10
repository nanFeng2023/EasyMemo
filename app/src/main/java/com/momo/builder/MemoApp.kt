package com.momo.builder

import android.annotation.SuppressLint
import android.app.Application
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import androidx.lifecycle.MutableLiveData
import com.adjust.sdk.Adjust
import com.adjust.sdk.AdjustConfig
import com.adjust.sdk.LogLevel
import com.android.installreferrer.api.InstallReferrerClient
import com.android.installreferrer.api.InstallReferrerStateListener
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.identifier.AdvertisingIdClient
import com.google.firebase.ktx.Firebase
import com.google.firebase.ktx.initialize
import com.momo.builder.broadcast.HomeEventBroadcast
import com.tencent.mmkv.MMKV
import com.momo.builder.conf.FireConf
import com.momo.builder.u.AppRegisterU
import com.momo.builder.u.Constant
import com.momo.builder.u.MemoU
import com.momo.builder.u.MmkvU
import com.momo.builder.u.EventReportU
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.util.Date

lateinit var memoApp: MemoApp
val message = MutableLiveData<String>()

class MemoApp : Application() {

    override fun onCreate() {
        super.onCreate()
        memoApp = this
        Firebase.initialize(this)
        MobileAds.initialize(this)
        MMKV.initialize(this)
        AppRegisterU.register(this)
        MemoU.getLocalMemoList()
        getFirebaseData()
        reqCloak()
        refererReq()
        adLimitTrack()
        initAdjust()
        registerHomeListener()
    }

    private fun getFirebaseData() {
        kotlin.runCatching {
            if (packageName == "com.easymemo.convenient.app") {
                FireConf.getFireConf()
            }
        }
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    private fun registerHomeListener() {
        val intentFilter = IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(HomeEventBroadcast(), intentFilter, RECEIVER_NOT_EXPORTED)
        } else registerReceiver(HomeEventBroadcast(), intentFilter)
    }

    private fun reqCloak() {
        val baseUrl = "https://album.easymemoconvenient.com/stanton/chemic?"
        val requestUrl = "${baseUrl}stale=${EventReportU.getAndroidID(this)}&fuel=${Date().time}" +
                "&mu=${Build.MODEL}&safety=$packageName&lame=${Build.VERSION.RELEASE}&braniff=" +
                "&felony=${MmkvU.getStr(MmkvU.GAID)}&schwab=${EventReportU.getAndroidID(this)}&medici=chaplain&laos=" +
                "&nip=${EventReportU.getVersionName(this)}"
        GlobalScope.launch {
            while (MmkvU.getStr("cloakStr").isEmpty()) {
                OkHttpClient().newCall(Request.Builder().url(requestUrl).get().build())
                    .enqueue(object : Callback {
                        override fun onFailure(call: Call, e: IOException) {}

                        override fun onResponse(call: Call, response: Response) {
                            if (response.isSuccessful) {
                                val string = response.body?.string()
                                if (!string.isNullOrBlank()) {
                                    MmkvU.saveStr("cloakStr", string)
                                    cancel()
                                }
                            }
                        }
                    })
                delay(10000)
            }
        }
    }

    private fun refererReq() {
        GlobalScope.launch(Dispatchers.IO) {
            while (MmkvU.getStr(MmkvU.INSTALL_REFERER).isEmpty()) {
                val referrerClient = InstallReferrerClient.newBuilder(memoApp).build()
                referrerClient.startConnection(object : InstallReferrerStateListener {
                    override fun onInstallReferrerSetupFinished(p0: Int) {
                        when (p0) {
                            InstallReferrerClient.InstallReferrerResponse.OK -> {
                                kotlin.runCatching {
                                    val referrer = referrerClient.installReferrer ?: return
                                    if (!referrer.installReferrer.isNullOrBlank())
                                        MmkvU.saveStr(
                                            MmkvU.INSTALL_REFERER,
                                            referrer.installReferrer
                                        )

                                    MmkvU.saveLong(
                                        MmkvU.CLICK_START_MS,
                                        referrer.referrerClickTimestampSeconds
                                    )
                                    MmkvU.saveLong(
                                        MmkvU.APP_INSTALL_MS,
                                        referrer.installBeginTimestampSeconds
                                    )
                                    MmkvU.saveLong(
                                        MmkvU.CLICK_START_SERVER_MS,
                                        referrer.referrerClickTimestampServerSeconds
                                    )
                                    MmkvU.saveLong(
                                        MmkvU.APP_INSTALL_SERVER_MS,
                                        referrer.installBeginTimestampServerSeconds
                                    )

                                    if (!referrer.installVersion.isNullOrBlank())
                                        MmkvU.saveStr(
                                            MmkvU.INSTALL_VERSION,
                                            referrer.installVersion
                                        )

                                    MmkvU.saveBoolean(
                                        MmkvU.APP_7_DAY,
                                        referrer.googlePlayInstantParam
                                    )

                                    //上报install事件
                                    val jsonStr = EventReportU.installDataJson(memoApp).toString()
                                    EventReportU.reportData(jsonStr, onFail = {
                                        MmkvU.saveStr(MmkvU.INSTALL_EVENT_FAIL, jsonStr)
                                    }, eventName = EventReportU.install_event)
                                    cancel()
                                }
                            }

                            InstallReferrerClient.InstallReferrerResponse.DEVELOPER_ERROR -> {}

                            InstallReferrerClient.InstallReferrerResponse.FEATURE_NOT_SUPPORTED -> {}

                            InstallReferrerClient.InstallReferrerResponse.SERVICE_DISCONNECTED -> {}

                            InstallReferrerClient.InstallReferrerResponse.SERVICE_UNAVAILABLE -> {}
                        }
                        referrerClient.endConnection()
                    }

                    override fun onInstallReferrerServiceDisconnected() {}
                })
                delay(10000)
            }
        }
    }

    private fun adLimitTrack() {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                AdvertisingIdClient.getAdvertisingIdInfo(memoApp).apply {
                    val str = if (isLimitAdTrackingEnabled)
                        "occurred"
                    else "bleat"
                    MmkvU.saveStr(MmkvU.LIMIT_TRACK, str)
                    id?.let { MmkvU.saveStr(MmkvU.GAID, it) }
                }
                cancel()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun initAdjust() {
        val config = if (BuildConfig.DEBUG)
            AdjustConfig(this, Constant.ADJUST_KEY, AdjustConfig.ENVIRONMENT_SANDBOX)
        else AdjustConfig(this, Constant.ADJUST_KEY, AdjustConfig.ENVIRONMENT_PRODUCTION, true)
        Adjust.addSessionCallbackParameter("customer_user_id", EventReportU.getAndroidID(this))
//        config.setOnAttributionChangedListener {
//            Log.d("----", "adjust network:${it.network}")
//            if (MmkvU.getStr("user_adjust").isNotEmpty())
//                return@setOnAttributionChangedListener
//            val network = it.network
//            if (network.isNotEmpty() && !network.contains("organic", ignoreCase = true)) {
//                //买量用户,保存到本地
//                MmkvU.saveStr("user_adjust", network)
//                Log.d("----", "save adjust network")
//            }
//        }
        config.setLogLevel(if (BuildConfig.DEBUG) LogLevel.DEBUG else LogLevel.SUPRESS)//设置日志级别 suppress禁用日志
        config.setDelayStart(5.5)
        Adjust.onCreate(config)
    }

}