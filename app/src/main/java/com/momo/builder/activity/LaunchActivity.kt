package com.momo.builder.activity

import android.animation.ValueAnimator
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.animation.LinearInterpolator
import com.momo.builder.admob.AdInfo
import com.momo.builder.admob.LoadAd
import com.momo.builder.admob.ShowAd
import com.momo.builder.baseactivity.BaseViewBinding
import com.momo.builder.databinding.ActivityLaunchBinding
import com.momo.builder.u.EventReportU
import com.momo.builder.u.MmkvU
import com.momo.builder.u.UmU
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class LaunchActivity : BaseViewBinding<ActivityLaunchBinding>() {
    private var valueAnimator: ValueAnimator? = null
    private val showAd by lazy { ShowAd(LoadAd.OPEN, this) }
    private var duration = 0L
    private val enterTime = System.currentTimeMillis()

    override fun initViewBinding(): ActivityLaunchBinding =
        ActivityLaunchBinding.inflate(layoutInflater)

    override fun onView() {
        GlobalScope.launch {
            EventReportU.ip = ""
            while (EventReportU.ip.isNotEmpty()) {
                EventReportU.requestIp {
                    cancel()
                }
                delay(5000)
            }
        }
        AdInfo.clearNativeMap()
        AdInfo.getInt()
        if (!MmkvU.getBoolean(MmkvU.FIRST_INSTALL)) {
            UmU.reqConsentInfo(this) {
                LoadAd.loadAllAd()
                initAnimator()
                MmkvU.saveBoolean(MmkvU.FIRST_INSTALL, true)
            }
        } else {
            LoadAd.loadAllAd()
            initAnimator()
        }
        EventReportU.reportCustomEvent(EventReportU.land_show)
        val fromNotiClick = intent.getBooleanExtra("isFromNotificationClick", false)
        if (fromNotiClick) {
            Log.d("----", "launch report notice_click")
            EventReportU.reportCustomEvent(EventReportU.notice_click)
        }
    }

    private fun initAnimator() {
        valueAnimator = ValueAnimator.ofInt(1, 100).apply {
            duration = 6000L
            interpolator = LinearInterpolator()
            addUpdateListener {
                val pro = it.animatedValue as Int
                binding.launchPro.progress = pro
                if (pro in 20..99) {
                    showAd.showOpenAd(
                        showed = {
                            binding.launchPro.progress = 100
                            stopAnimator()
                        },
                        close = {
                            toMemoList()
                        }
                    )
                } else if (pro >= 100) {
                    toMemoList()
                }
            }
            start()
        }
    }

    private fun toMemoList() {
        duration = System.currentTimeMillis() - enterTime
        EventReportU.reportCustomEvent(EventReportU.land_stay, Bundle().apply {
            putLong("time", duration)
        })
        startActivity(Intent(this, MemoListActivity::class.java))
        finish()
    }

    private fun stopAnimator() {
        valueAnimator?.removeAllUpdateListeners()
        valueAnimator?.cancel()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopAnimator()
    }
}