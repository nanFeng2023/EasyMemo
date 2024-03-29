package com.momo.builder.admob

import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.momo.builder.bean.AdBean
import com.momo.builder.bean.ResultBean
import com.momo.builder.memoApp
import com.momo.builder.u.logMemo

object LoadAd {
    const val OPEN = "fE2_pens"
    const val HOME_BOTTOM = "fE2_miasmry"
    const val SAVE_INTER = "fE2_parsure"

    private val loadingList = arrayListOf<String>()
    private val adResult = hashMapOf<String, ResultBean>()

    fun loadAllAd() {
        loadAd(OPEN, tryAgain = true)
        loadAd(HOME_BOTTOM)
        loadAd(SAVE_INTER)
    }

    fun loadAd(type: String, tryAgain: Boolean = false) {
        if (!canLoad(type)) {
            return
        }
        val adList = AdInfo.getAdList(type)
        if (adList.isNotEmpty()) {
            loadingList.add(type)
            loopLoad(type, adList.iterator(), tryAgain)
        }
    }

    private fun loopLoad(type: String, iterator: Iterator<AdBean>, tryAgain: Boolean) {
        load(type, iterator.next()) {
            if (null != it) {
                loadingList.remove(type)
                adResult[type] = it
            } else {
                if (iterator.hasNext()) {
                    loopLoad(type, iterator, tryAgain)
                } else {
                    loadingList.remove(type)
                    if (tryAgain) {
                        loadAd(type, tryAgain = false)
                    }
                }
            }
        }
    }

    private fun load(type: String, adBean: AdBean, result: (bean: ResultBean?) -> Unit) {
        logMemo("start load $type ad --->$adBean")
        when (adBean.fE2_anothe) {
            "bitia" -> {
                AppOpenAd.load(
                    memoApp,
                    adBean.fE2_crass,
                    AdRequest.Builder().build(),
                    AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT,
                    object : AppOpenAd.AppOpenAdLoadCallback() {
                        override fun onAdLoaded(p0: AppOpenAd) {
                            logMemo("load ad success----$type")
                            result.invoke(
                                ResultBean(
                                    time = System.currentTimeMillis(),
                                    ad = p0,
                                    adBean = adBean, bigType = type
                                )
                            )
                        }

                        override fun onAdFailedToLoad(p0: LoadAdError) {
                            super.onAdFailedToLoad(p0)
                            logMemo("load ad fail----$type---${p0.message}")
                            result.invoke(null)
                        }
                    }
                )
            }

            "surfa" -> {
                AdLoader.Builder(
                    memoApp,
                    adBean.fE2_crass,
                ).forNativeAd { p0 ->
                    logMemo("load ad success----$type")
                    result.invoke(
                        ResultBean(
                            time = System.currentTimeMillis(),
                            ad = p0,
                            adBean = adBean, bigType = type
                        )
                    )
                }
                    .withAdListener(object : AdListener() {
                        override fun onAdFailedToLoad(p0: LoadAdError) {
                            super.onAdFailedToLoad(p0)
                            logMemo("load ad fail----$type---${p0.message}")
                            result.invoke(null)
                        }

                        override fun onAdClicked() {
                            super.onAdClicked()
                            AdInfo.addNum(true)
                        }
                    })
                    .withNativeAdOptions(
                        NativeAdOptions.Builder()
                            .setAdChoicesPlacement(
                                NativeAdOptions.ADCHOICES_BOTTOM_LEFT
                            )
                            .build()
                    )
                    .build()
                    .loadAd(AdRequest.Builder().build())
            }

            "varicu" -> {
                InterstitialAd.load(
                    memoApp,
                    adBean.fE2_crass,
                    AdRequest.Builder().build(),
                    object : InterstitialAdLoadCallback() {
                        override fun onAdFailedToLoad(p0: LoadAdError) {
                            super.onAdFailedToLoad(p0)
                            logMemo("load ad fail----$type---${p0.message}")
                            result.invoke(null)
                        }

                        override fun onAdLoaded(p0: InterstitialAd) {
                            logMemo("load ad success----$type")
                            result.invoke(
                                ResultBean(
                                    time = System.currentTimeMillis(),
                                    ad = p0,
                                    adBean = adBean, bigType = type
                                )
                            )
                        }
                    }
                )
            }
        }
    }

    private fun canLoad(type: String): Boolean {
        if (AdInfo.isLimit()) {
            logMemo("limit num")
            return false
        }
        if (loadingList.contains(type)) {
            logMemo("$type loading")
            return false
        }
        if (adResult.containsKey(type)) {
            val resultBean = adResult[type]
            if (null != resultBean?.ad) {
                if (resultBean.isEx()) {
                    removeAd(type)
                } else {
                    logMemo("$type has cache")
                    return false
                }
            }
        }
        return true
    }

    fun removeAd(type: String) {
        adResult.remove(type)
    }

    fun getAd(type: String) = adResult[type]?.ad

    fun getResultAd(type: String): ResultBean? = adResult[type]
}