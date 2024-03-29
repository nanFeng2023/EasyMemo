package com.momo.builder.activity

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.addCallback
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.momo.builder.adapter.EditFuncAdapter
import com.momo.builder.admob.LoadAd
import com.momo.builder.admob.ShowAd
import com.momo.builder.baseactivity.BaseViewBinding
import com.momo.builder.bean.MemoBean
import com.momo.builder.conf.LocalConf
import com.momo.builder.databinding.ActivityEditBinding
import com.momo.builder.dialog.ChooseColorDialog
import com.momo.builder.dialog.DeleteDialog
import com.momo.builder.u.EventReportU
import com.momo.builder.u.MemoU
import com.momo.builder.u.show
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class EditActivity : BaseViewBinding<ActivityEditBinding>() {
    private var labelColor = "#F36E9D"
    private var fontColor = "#575757"
    private val showAd by lazy { ShowAd(LoadAd.SAVE_INTER, this) }
    private val funcAdapter by lazy { EditFuncAdapter(this, fontColor) { clickItem(it) } }
    private var enterTime = System.currentTimeMillis()
    override fun initViewBinding(): ActivityEditBinding =
        ActivityEditBinding.inflate(layoutInflater)

    private var curTitleText =
        SimpleDateFormat("hh:mm:ss", Locale.getDefault()).format(Date())
    private var curContentText = "<html><body></body></html>"
    private var isEditTitle = false

    override fun onView() {
        setStatus(binding.statusView)
        getMemoBean()
        updateLabelColor()
        updateFontColor()
        setAdapter()
        EventReportU.reportCustomEvent(EventReportU.text_show)
        loopCheckAutoSave()
    }

    private fun getTitleText(): String {
        return binding.editTitle.text.toString()
    }

    private fun getContentText(): String {
        return binding.arEditor.mAre.text.toString()
    }

    private fun getHtmlContentText(): String {
        return binding.arEditor.html
    }

    private fun loopCheckAutoSave() {
        lifecycleScope.launch {
            while (true) {
                delay(3000)
                autoSaveData()
            }
        }
    }

    private fun autoSaveData() {
        val title = getTitleText()
        val content = getContentText()
        if (content.isEmpty() || content == curContentText && title == curTitleText) return
        curTitleText = if (isEditTitle) {
            title
        } else {
            SimpleDateFormat("hh:mm:ss", Locale.getDefault()).format(Date())
        }
        curContentText = content
        MemoU.saveMemo(curTitleText, getHtmlContentText(), content, labelColor)
        EventReportU.reportCustomEvent(EventReportU.text_autosave)
        Toast.makeText(this, "saving-saved", Toast.LENGTH_SHORT).show()
        Log.d(
            "-----",
            "auto save data---title:$title---content:$content---curTitleText:$curTitleText---curContentText:$curContentText"
        )
    }

    override fun setClickListener() {
        binding.iconBack.setOnClickListener { finishPage() }
        binding.iconSave.setOnClickListener { saveMemo() }
        onBackPressedDispatcher.addCallback {
            finishPage()
        }
        binding.iconColor.setOnClickListener {
            EventReportU.reportCustomEvent(EventReportU.text_theme)
            ChooseColorDialog(true, labelColor) {
                labelColor = it
                updateLabelColor()
            }.show(supportFragmentManager, "ChooseColorDialog")
        }
        binding.iconDelete.setOnClickListener {
            EventReportU.reportCustomEvent(EventReportU.text_delete)
            DeleteDialog(LocalConf.deleteStr) {
                EventReportU.reportCustomEvent(EventReportU.text_deleted)
                MemoU.delete()
                back()
            }.show(supportFragmentManager, "DeleteDialog")
        }
        binding.editTitle.addTextChangedListener {
            val text = it?.toString().toString()
            isEditTitle = text.isNotEmpty()
        }
    }

    private fun setAdapter() {
        binding.rvBottomFunc.apply {
            layoutManager = GridLayoutManager(this@EditActivity, 5)
            adapter = funcAdapter
        }
    }

    private fun clickItem(index: Int) {
        when (index) {
            3 -> {
                ChooseColorDialog(false, fontColor) {
                    fontColor = it
                    updateFontColor()
                    funcAdapter.setFontColor(it)
                }.show(supportFragmentManager, "ChooseColorDialog")
            }

            else -> binding.arEditor.clickFunc(index)
        }
        EventReportU.reportCustomEvent(EventReportU.text_button_tool)
    }

    private fun updateLabelColor() {
        binding.editTitle.setBackgroundColor(Color.parseColor(labelColor))
    }

    private fun updateFontColor() {
        binding.arEditor.setFontColor(Color.parseColor(fontColor))
    }

    private fun saveMemo() {
        val html = getHtmlContentText()
        val title = binding.editTitle.text.toString().trim()
        if (html.isEmpty() || title.isEmpty()) {
            Toast.makeText(this, "The title or content cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }
        showAd.showOpenAd(emptyBack = true, showed = {}, close = {
            MemoU.saveMemo(title, html, getContentText(), labelColor)
            EventReportU.reportCustomEvent(EventReportU.text_save)
            back()
        })
    }

    private fun back() {
        setResult(100)
        finish()
    }

    private fun getMemoBean() {
        MemoU.clickMemo?.let {
            labelColor = it.labelColor
            binding.editTitle.setText(it.title)
            binding.arEditor.fromHtml(it.htmlContent)
            curTitleText = it.title
            curContentText = it.content
            isEditTitle = true
        }
        binding.iconDelete.show(null != MemoU.clickMemo)
    }

    private fun finishPage() {
        autoSaveData()
        back()
    }

    override fun onResume() {
        super.onResume()
        enterTime = System.currentTimeMillis()
    }

    override fun onPause() {
        super.onPause()
        EventReportU.reportCustomEvent(EventReportU.text_stay, Bundle().apply {
            putLong("time", System.currentTimeMillis() - enterTime)
        })
    }
}