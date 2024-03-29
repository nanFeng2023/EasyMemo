package com.momo.builder.admob

import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.FullScreenContentCallback
import com.momo.builder.baseactivity.BaseActivity
import com.momo.builder.u.logMemo
import kotlinx.coroutines.*

class FullAdCallBack(
    private val type:String,
    private val baseActivity: BaseActivity,
    private val showed:()->Unit,
    private val close:()->Unit,
): FullScreenContentCallback() {

    override fun onAdDismissedFullScreenContent() {
        super.onAdDismissedFullScreenContent()
        AdInfo.openAdShowing =false
        clickCloseAd()
    }

    override fun onAdShowedFullScreenContent() {
        super.onAdShowedFullScreenContent()
        AdInfo.openAdShowing  =true
        logMemo("$type ad showing ")
//        showed.invoke()
        AdInfo.addNum(false)
        LoadAd.removeAd(type)
    }

    override fun onAdFailedToShowFullScreenContent(p0: AdError) {
        super.onAdFailedToShowFullScreenContent(p0)
        AdInfo.openAdShowing =false
        LoadAd.removeAd(type)
        clickCloseAd()
    }


    override fun onAdClicked() {
        super.onAdClicked()
        AdInfo.addNum(true)
    }

    private fun clickCloseAd(){
        GlobalScope.launch(Dispatchers.Main) {
            delay(200L)
            withContext(Dispatchers.Main){
                if (baseActivity.resume){
                    close.invoke()
                }
            }
        }
    }
}