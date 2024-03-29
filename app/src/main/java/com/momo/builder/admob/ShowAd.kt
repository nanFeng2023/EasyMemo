package com.momo.builder.admob

import android.graphics.Outline
import android.view.View
import android.view.ViewOutlineProvider
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.utils.widget.ImageFilterView
import com.facebook.appevents.AppEventsLogger
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import com.momo.builder.R
import com.momo.builder.baseactivity.BaseActivity
import com.momo.builder.bean.ResultBean
import com.momo.builder.conf.LocalConf.ad
import com.momo.builder.memoApp
import com.momo.builder.u.EventReportU
import com.momo.builder.u.logMemo
import com.momo.builder.u.show
import kotlinx.coroutines.*
import java.util.Currency

class ShowAd(
    private val type: String,
    private val baseActivity: BaseActivity
) {
    private var lastNativeAd: NativeAd? = null
    private var showJob: Job? = null

    fun showOpenAd(emptyBack: Boolean = false, showed: () -> Unit, close: () -> Unit) {
        val resultBean = LoadAd.getResultAd(type)
        val ad = resultBean?.ad
        if (ad == null && emptyBack) {
            if (type == LoadAd.SAVE_INTER) {
                LoadAd.loadAd(type)
            }
            close.invoke()
            return
        }
        ad?.let {
            if (AdInfo.openAdShowing || !baseActivity.resume) {
                close.invoke()
                return
            }
            showed.invoke()
            when (ad) {
                is InterstitialAd -> {
                    ad.fullScreenContentCallback = FullAdCallBack(type, baseActivity, showed, close)
                    ad.show(baseActivity)
                    reportAdEvent(ad, resultBean)
                }

                is AppOpenAd -> {
                    ad.fullScreenContentCallback = FullAdCallBack(type, baseActivity, showed, close)
                    ad.show(baseActivity)
                    reportAdEvent(ad, resultBean)
                }
            }
        }
    }

    private fun reportAdEvent(ad: Any, resultBean: ResultBean) {
        when (ad) {
            is AppOpenAd -> {
                ad.setOnPaidEventListener { adValue ->
                    EventReportU.reportData(
                        EventReportU.advertiseDataJson(
                            memoApp,
                            resultBean,
                            adValue,
                            ad.responseInfo.mediationAdapterClassName as String
                        ).toString(), eventName = EventReportU.open_ad_impression
                    )
                    AppEventsLogger.newLogger(memoApp).logPurchase(
                        (adValue.valueMicros / 1000000.0).toBigDecimal(),
                        Currency.getInstance("USD")
                    )
                }
            }

            is InterstitialAd -> {
                ad.setOnPaidEventListener { adValue ->
                    EventReportU.reportData(
                        EventReportU.advertiseDataJson(
                            memoApp,
                            resultBean,
                            adValue,
                            ad.responseInfo.mediationAdapterClassName as String
                        ).toString(), eventName = EventReportU.inter_ad_impression
                    )
                    AppEventsLogger.newLogger(memoApp).logPurchase(
                        (adValue.valueMicros / 1000000.0).toBigDecimal(),
                        Currency.getInstance("USD")
                    )
                }
            }

            is NativeAd -> {
                ad.setOnPaidEventListener { adValue ->
                    EventReportU.reportData(
                        EventReportU.advertiseDataJson(
                            memoApp,
                            resultBean,
                            adValue,
                            ad.responseInfo?.mediationAdapterClassName as String
                        ).toString(), eventName = EventReportU.home_ad_impression
                    )
                    AppEventsLogger.newLogger(memoApp).logPurchase(
                        (adValue.valueMicros / 1000000.0).toBigDecimal(),
                        Currency.getInstance("USD")
                    )
                }
            }
        }
    }

    fun showNativeAd() {
        if (!AdInfo.canLoadNativeAd(type)) {
            return
        }
        LoadAd.loadAd(type)
        stopShowNativeAd()
        showJob = GlobalScope.launch(Dispatchers.Main) {
            delay(300L)
            if (!baseActivity.resume) {
                return@launch
            }
            while (true) {
                if (!isActive) {
                    break
                }
                val resultBean = LoadAd.getResultAd(type)
                val ad = resultBean?.ad
                ad?.let {
                    if (baseActivity.resume && ad is NativeAd) {
                        cancel()
                        lastNativeAd?.destroy()
                        lastNativeAd = ad
                        showNativeAd(ad, resultBean)
                    }
                }
                delay(1000L)
            }
        }
    }

    private fun showNativeAd(ad: NativeAd, resultBean: ResultBean) {
        logMemo("show $type ad ")
        val viewNative = baseActivity.findViewById<NativeAdView>(R.id.ad_view)
        setLogo(viewNative, ad)
        setInstall(viewNative, ad)
        setMedia(viewNative, ad)
        setTitle(viewNative, ad)
        viewNative.setNativeAd(ad)
        baseActivity.findViewById<AppCompatImageView>(R.id.ad_cover).show(false)
        reportAdEvent(ad, resultBean)

        AdInfo.addNum(false)
        LoadAd.removeAd(type)
        LoadAd.loadAd(type)
        AdInfo.setNativeAdBool(type, false)
    }

    private fun setLogo(viewNative: NativeAdView, ad: NativeAd) {
        viewNative.iconView = baseActivity.findViewById(R.id.ad_logo)
        (viewNative.iconView as ImageFilterView).setImageDrawable(ad.icon?.drawable)
    }

    private fun setInstall(viewNative: NativeAdView, ad: NativeAd) {
        viewNative.callToActionView = baseActivity.findViewById(R.id.ad_install)
        (viewNative.callToActionView as AppCompatTextView).text = ad.callToAction

    }

    private fun setMedia(viewNative: NativeAdView, ad: NativeAd) {
        viewNative.mediaView = baseActivity.findViewById(R.id.ad_media)
        ad.mediaContent?.let {
            viewNative.mediaView?.apply {
                mediaContent = it
                setImageScaleType(ImageView.ScaleType.CENTER_CROP)
                outlineProvider = object : ViewOutlineProvider() {
                    override fun getOutline(view: View?, outline: Outline?) {
                        if (view == null || outline == null) return
                        outline.setRoundRect(
                            0,
                            0,
                            view.width,
                            view.height,
                            12F
                        )
                        view.clipToOutline = true
                    }
                }
            }
        }
    }

    private fun setTitle(viewNative: NativeAdView, ad: NativeAd) {
        viewNative.headlineView = baseActivity.findViewById(R.id.ad_title)
        (viewNative.headlineView as AppCompatTextView).text = ad.headline
    }

    fun stopShowNativeAd() {
        showJob?.cancel()
        showJob = null
    }
}