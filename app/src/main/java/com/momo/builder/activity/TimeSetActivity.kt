package com.momo.builder.activity

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import com.momo.builder.R
import com.momo.builder.databinding.ActivityTimeSetBinding
import kotlin.math.min

class TimeSetActivity : AppCompatActivity() {
    private lateinit var mBinding: ActivityTimeSetBinding
    private lateinit var ivSelList: List<AppCompatImageView>
    private lateinit var tvList: List<AppCompatTextView>
    private var mIndex = 0
    private var mText = "One-time event"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityTimeSetBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        ivSelList = arrayListOf(
            mBinding.ivOneSel,
            mBinding.ivDailySel,
            mBinding.ivTomorrowSel,
            mBinding.ivWeekdaySel,
            mBinding.ivWeekendSel
        )
        tvList = arrayListOf(
            mBinding.tvOne,
            mBinding.tvDaily,
            mBinding.tvTomorrow,
            mBinding.tvWeekday,
            mBinding.tvWeekday
        )
        mBinding.ivReturn.setOnClickListener { finishResult() }
        onBackPressedDispatcher.addCallback { finishResult() }
        mBinding.llcOne.setOnClickListener {
            selectRepeat(0)
        }
        mBinding.llcDaily.setOnClickListener {
            selectRepeat(1)
        }
        mBinding.llcTomorrow.setOnClickListener {
            selectRepeat(2)
        }
        mBinding.llcWeekday.setOnClickListener {
            selectRepeat(3)
        }
        mBinding.llcWeekend.setOnClickListener {
            selectRepeat(4)
        }
        selectRepeat(intent.getIntExtra("timeType", 0), false)
    }

    private fun selectRepeat(index: Int, isClosePage: Boolean = true) {
        for (aiv in ivSelList) {
            aiv.visibility = View.INVISIBLE
        }
        for (atv in tvList) {
            atv.setTextColor(Color.BLACK)
        }
        ivSelList[index].visibility = View.VISIBLE
        tvList[index].setTextColor(Color.WHITE)
        mIndex = index
        mText = tvList[mIndex].text.toString()
        if (isClosePage)
            finishResult()
    }

    private fun finishResult() {
        setResult(100, Intent().apply {
            putExtra("index", mIndex)
            putExtra("text", mText)
        })
        finish()
    }

}