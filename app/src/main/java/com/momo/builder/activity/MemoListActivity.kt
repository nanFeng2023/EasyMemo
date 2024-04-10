package com.momo.builder.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.View.OnFocusChangeListener
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import com.momo.builder.admob.AdInfo
import com.momo.builder.admob.LoadAd
import com.momo.builder.admob.ShowAd
import com.momo.builder.baseactivity.BaseViewBinding
import com.momo.builder.bean.MemoBean
import com.momo.builder.broadcast.NotificationBroadcast
import com.momo.builder.databinding.ActivityMemoListBinding
import com.momo.builder.dialog.BottomAppraiseDialog
import com.momo.builder.fragment.MemoFragment
import com.momo.builder.memoApp
import com.momo.builder.u.BadgeUtils
import com.momo.builder.u.Constant
import com.momo.builder.u.EventReportU
import com.momo.builder.u.MemoU
import com.momo.builder.u.MmkvU
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

class MemoListActivity : BaseViewBinding<ActivityMemoListBinding>() {
    private val showNativeAd by lazy { ShowAd(LoadAd.HOME_BOTTOM, this) }
    private var sessionSuccessMs = 0L
    override fun initViewBinding(): ActivityMemoListBinding =
        ActivityMemoListBinding.inflate(layoutInflater)

    private var enterTime = System.currentTimeMillis()
    private lateinit var mNotifyCast: NotificationBroadcast
    private val fragments = arrayListOf(
        MemoFragment.newInstance(NOTES),
        MemoFragment.newInstance(TODO),
        MemoFragment.newInstance(DONE),
        MemoFragment.newInstance(ALL)
    )
    private val tabs = arrayListOf(NOTES, TODO, DONE, ALL)
    private var isTodoHint = true

    companion object {
        const val NOTES = "Notes"
        const val TODO = "Todo"
        const val DONE = "Done"
        const val ALL = "All"
        var mCurrentPos = 1
        var todoItemPos = 0
    }

    override fun onView() {
        lifecycleScope.launch {
            while (MmkvU.getStr(MmkvU.INSTALL_EVENT_FAIL).isNotEmpty()) {
                EventReportU.reportData(
                    EventReportU.installDataJson(memoApp).toString(), onSuccess = {
                        MmkvU.saveStr(MmkvU.INSTALL_EVENT_FAIL, "")
                    }, eventName = EventReportU.install_event
                )
                delay(10000)
            }
        }
        EventReportU.reportCustomEvent(EventReportU.home_show)
        initTodoCount()
        registerNotifyCast()
        initViewPager()
        binding.layoutContent.viewPager.currentItem = mCurrentPos
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        binding.layoutContent.viewPager.currentItem = mCurrentPos
    }

    private fun reportTabEvent(pos: Int) {
        mCurrentPos = pos
        when (mCurrentPos) {
            0 -> EventReportU.reportCustomEvent(EventReportU.home_tab_click, Bundle().apply {
                putString("value", NOTES.lowercase(Locale.getDefault()))
            })

            1 -> EventReportU.reportCustomEvent(EventReportU.home_tab_click, Bundle().apply {
                putString("value", TODO.lowercase(Locale.getDefault()))
            })

            2 -> EventReportU.reportCustomEvent(EventReportU.home_tab_click, Bundle().apply {
                putString("value", DONE.lowercase(Locale.getDefault()))
            })

            3 -> EventReportU.reportCustomEvent(EventReportU.home_tab_click, Bundle().apply {
                putString("value", ALL.lowercase(Locale.getDefault()))
            })
        }
    }

    private fun initViewPager() {
        binding.layoutContent.viewPager.adapter =
            object : FragmentStateAdapter(supportFragmentManager, lifecycle) {
                override fun getItemCount(): Int {
                    return tabs.size
                }

                override fun createFragment(position: Int): Fragment {
                    return fragments[position]
                }
            }

        binding.layoutContent.viewPager.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                val tabCount = binding.layoutContent.tabLayout.tabCount
                for (i in 0 until tabCount) {
                    val tab = binding.layoutContent.tabLayout.getTabAt(i)
                    if (tab != null) {
                        val textView = tab.customView as TextView
                        if (tab.position == position) {
                            textView.setTextColor(Color.parseColor("#1C259C"))
                        } else {
                            textView.setTextColor(Color.parseColor("#C7C7C7"))
                        }
                    }
                }
                //改变数据
                fragments[position].setData()
                reportTabEvent(position)
            }
        })

        val tabLayoutMediator = TabLayoutMediator(
            binding.layoutContent.tabLayout, binding.layoutContent.viewPager
        ) { tab, position ->
            val textView = TextView(this)
            textView.text = tabs[position]
            textView.textSize = 16f
            textView.setSingleLine()
            textView.gravity = Gravity.CENTER
            tab.setCustomView(textView)
        }
        tabLayoutMediator.attach()
    }

    fun initTodoCount() {
        var todoCount = 0
        for (memo in MemoU.list) {
            if (memo.type == 1 && !memo.isDone) {
                todoCount++
            }
        }
        if (todoCount > 0) {
            binding.layoutContent.tvTodoCount.text = todoCount.toString()
            binding.layoutContent.tvTodoCount.visibility = View.VISIBLE
            if (isTodoHint) {
                try {
                    isTodoHint = false
                    if (BadgeUtils.setCount(todoCount, this)) {
//                    Toast.makeText(this, "Successful setting", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Setup failure", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(this, "Setup failure", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            binding.layoutContent.tvTodoCount.visibility = View.GONE
        }
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
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(Constant.URL)).apply {
                addCategory(Intent.CATEGORY_BROWSABLE)
            })
        }
    }

    fun toEdit(memoBean: MemoBean?) {
        MemoU.clickMemo = memoBean
        editLaunch.launch(Intent(this, EditActivity::class.java))
    }

    private fun appraisePage() {
        if (MmkvU.getBoolean(MmkvU.IS_APPRAISE)) return
        var createMemoCount = 0
        for (memo in MemoU.list) {
            if (memo.isNewCreate) createMemoCount++
        }
        if (createMemoCount >= 5) {
            val dialog = BottomAppraiseDialog()
            dialog.show(supportFragmentManager, "appraise")
            MmkvU.saveBoolean(MmkvU.IS_APPRAISE, true)
        }
    }

    private val editLaunch =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == 100) {
                fragments[mCurrentPos].memoListAdapter?.setList(MemoU.list)
                initTodoCount()
                appraisePage()
            }
        }

    private fun search(content: String) {
        val searchList = MemoU.search(content)
        if (searchList.isNotEmpty()) fragments[mCurrentPos].memoListAdapter?.searchList(searchList)
        else fragments[mCurrentPos].memoListAdapter?.setList(MemoU.list)
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

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    private fun registerNotifyCast() {
        mNotifyCast = NotificationBroadcast()
        val intentFilter = IntentFilter()
        intentFilter.addAction(NotificationBroadcast.actionClock)
        intentFilter.addAction(NotificationBroadcast.actionRepeatClock)
        intentFilter.addAction(NotificationBroadcast.actionPeriodClock)
        registerReceiver(mNotifyCast, intentFilter)
    }

    override fun onDestroy() {
        super.onDestroy()
        AdInfo.setNativeAdBool(LoadAd.HOME_BOTTOM, true)
        showNativeAd.stopShowNativeAd()
        if (this::mNotifyCast.isInitialized) unregisterReceiver(mNotifyCast)
    }
}