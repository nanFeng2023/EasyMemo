package com.momo.builder.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.View.OnFocusChangeListener
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.momo.builder.adapter.MemoListAdapter
import com.momo.builder.admob.AdInfo
import com.momo.builder.admob.LoadAd
import com.momo.builder.admob.ShowAd
import com.momo.builder.baseactivity.BaseViewBinding
import com.momo.builder.bean.MemoBean
import com.momo.builder.databinding.ActivityMemoListBinding
import com.momo.builder.memoApp
import com.momo.builder.u.EventReportU
import com.momo.builder.u.MemoU
import com.momo.builder.u.MmkvU
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MemoListActivity : BaseViewBinding<ActivityMemoListBinding>() {
    private val showNativeAd by lazy { ShowAd(LoadAd.HOME_BOTTOM, this) }
    private lateinit var memoListAdapter: MemoListAdapter
    private var sessionSuccessMs = 0L
    override fun initViewBinding(): ActivityMemoListBinding =
        ActivityMemoListBinding.inflate(layoutInflater)

    private var enterTime = System.currentTimeMillis()

    override fun onView() {
        setAdapter()
        lifecycleScope.launch {
            while (MmkvU.getStr(MmkvU.INSTALL_EVENT_FAIL).isNotEmpty()) {
                EventReportU.reportData(
                    EventReportU.installDataJson(memoApp).toString(),
                    onSuccess = {
                        MmkvU.saveStr(MmkvU.INSTALL_EVENT_FAIL, "")
                    }, eventName = EventReportU.install_event
                )
                delay(10000)
            }
        }
        EventReportU.reportCustomEvent(EventReportU.home_show)
    }

    override fun setClickListener() {
        binding.layoutContent.iconAdd.setOnClickListener {
            if (!binding.drawer.isOpen) {
                EventReportU.reportCustomEvent(EventReportU.home_add)
                toEdit(null)
            }
        }
        binding.layoutContent.editSearch.onFocusChangeListener =
            OnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    EventReportU.reportCustomEvent(EventReportU.home_search)
                }
            }
        binding.layoutContent.editSearch.setOnClickListener {
            EventReportU.reportCustomEvent(EventReportU.home_search)
        }
        binding.layoutContent.editSearch.addTextChangedListener { search(it?.toString() ?: "") }
        binding.layoutContent.iconSet.setOnClickListener {
            if (!binding.drawer.isOpen) {
                EventReportU.reportCustomEvent(EventReportU.home_set)
                binding.drawer.open()
            }
        }
        binding.layoutMenu.llcPrivacy.setOnClickListener {
            EventReportU.reportCustomEvent(EventReportU.set_privacy)
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.baidu.com")).apply {
                addCategory(Intent.CATEGORY_BROWSABLE)
            })
        }
    }

    private fun setAdapter() {
        memoListAdapter = MemoListAdapter(this) {
            if (!binding.drawer.isOpen) {
                EventReportU.reportCustomEvent(EventReportU.home_note)
                toEdit(it)
            }
        }
        binding.layoutContent.rvMemo.apply {
            layoutManager = LinearLayoutManager(this@MemoListActivity)
            adapter = memoListAdapter
        }
    }

    private fun toEdit(memoBean: MemoBean?) {
        MemoU.clickMemo = memoBean
        editLaunch.launch(Intent(this, EditActivity::class.java))
    }

    private val editLaunch =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == 100) {
                memoListAdapter.setList(MemoU.list)
            }
        }

    private fun search(content: String) {
        memoListAdapter.setList(MemoU.search(content))
    }

    override fun onResume() {
        super.onResume()
        showNativeAd.showNativeAd()
        if (System.currentTimeMillis() - sessionSuccessMs > 30000) {
            EventReportU.reportData(EventReportU.sessionDataJson(this).toString(), onSuccess = {
                sessionSuccessMs = System.currentTimeMillis()
            }, eventName = EventReportU.session_event)
        }
        enterTime = System.currentTimeMillis()
    }

    override fun onPause() {
        super.onPause()
        EventReportU.reportCustomEvent(EventReportU.home_stay, Bundle().apply {
            putLong("time", System.currentTimeMillis() - enterTime)
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        AdInfo.setNativeAdBool(LoadAd.HOME_BOTTOM, true)
        showNativeAd.stopShowNativeAd()
    }
}