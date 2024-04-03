package com.momo.builder.activity

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.addCallback
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.view.get
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.momo.builder.R
import com.momo.builder.adapter.EditFuncAdapter
import com.momo.builder.admob.LoadAd
import com.momo.builder.admob.ShowAd
import com.momo.builder.baseactivity.BaseViewBinding
import com.momo.builder.broadcast.NotifacationBroadcast
import com.momo.builder.conf.LocalConf
import com.momo.builder.databinding.ActivityEditBinding
import com.momo.builder.dialog.ChooseColorDialog
import com.momo.builder.dialog.DeleteDialog
import com.momo.builder.u.EventReportU
import com.momo.builder.u.MemoU
import com.momo.builder.u.show
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.random.Random

class EditActivity : BaseViewBinding<ActivityEditBinding>() {
    private var labelColor = "#F36E9D"
    private var fontColor = "#575757"
    private val showAd by lazy { ShowAd(LoadAd.SAVE_INTER, this) }
    private val funcAdapter by lazy { EditFuncAdapter(this, fontColor) { clickItem(it) } }
    private var enterTime = System.currentTimeMillis()
    private var hideJob: Job? = null
    override fun initViewBinding(): ActivityEditBinding =
        ActivityEditBinding.inflate(layoutInflater)

    private var curTitleText =
        SimpleDateFormat("hh:mm:ss", Locale.getDefault()).format(Date())
    private var curContentText = ""
    private var memoType = 0
    private val optionNotesTextData = arrayListOf(
        "Quick text insertion;Today ${MemoU.obtainDate()};",
        "Today ${MemoU.obtainDate()};Quick insert sequence number;"
    )
    private val optionTodoTextData = arrayListOf(
        "Work List;Today ${MemoU.obtainDate()};"
    )
    private var mLockJob: Job? = null

    @SuppressLint("SetTextI18n")
    override fun onView() {
        setStatus(binding.statusView)
        initSetBgColor()
        getMemoBean()
        updateLabelColor()
        updateFontColor()
        setAdapter()
        EventReportU.reportCustomEvent(EventReportU.text_show)
        loopCheckAutoSave()
        initPageData()
        optionHideTime()
    }

    private fun initSetBgColor() {
        val colorList = arrayListOf<String>()
        colorList.add("#F36E9D")
        colorList.add("#F3EDEF")
        colorList.add("#E6F2FF")
        colorList.add("#F1EFFF")
        colorList.add("#F8FFE6")
        colorList.add("#FFF2E6")
        labelColor = colorList[Random(System.currentTimeMillis()).nextInt(colorList.size)]
    }

    private fun optionHideTime() {
        hideJob?.cancel()
        hideJob = lifecycleScope.launch {
            binding.llcOptionSelectText.visibility = View.VISIBLE
            delay(5000)
            binding.llcOptionSelectText.visibility = View.GONE
        }
    }

    private fun initPageData() {
        updateOptionText()
        showLock()
        updateBtnOptionBg()
    }

    private fun showLock() {
        if (memoType == 0) {
            binding.llcLock.visibility = View.GONE
        } else {
//            binding.llcLock.visibility = View.VISIBLE
            if (mLockJob != null) return
            mLockJob = lifecycleScope.launch {
                while (true) {
                    val format =
                        SimpleDateFormat("yyyy-MM-dd/hh:mm", Locale.getDefault()).format(Date())
                    val split = format.split("/")
                    if (split.size > 1) {
                        val date = split[0]
                        val time = split[1]
                        if (date.isNotBlank())
                            binding.tvDate.text = date
                        if (time.isNotBlank())
                            binding.tvTime.text = time
                    }
                    delay(800)
                }
            }
        }
    }

    private fun updateOptionText() {
        if (memoType == 0) {
            optionText(optionNotesTextData)
        } else {
            optionText(optionTodoTextData)
        }
    }

    private fun optionText(data: ArrayList<String>) {
        val childCount = binding.llcText.childCount
        for (i in 0 until binding.llcText.childCount) {
            if (binding.llcText[i] is AppCompatTextView) {
                val textView = binding.llcText[i] as AppCompatTextView
                textView.visibility = View.GONE
            }
        }
        if (childCount >= data.size) {
            for (i in 0 until data.size) {
                if (binding.llcText[i] is AppCompatTextView) {
                    val textView = binding.llcText[i] as AppCompatTextView
                    textView.visibility = View.VISIBLE
                    textView.text = data[i]
                }
            }
        } else {
            for (i in 0 until binding.llcText.childCount) {
                if (binding.llcText[i] is AppCompatTextView) {
                    val textView = binding.llcText[i] as AppCompatTextView
                    textView.visibility = View.VISIBLE
                    textView.text = data[i]
                }
            }
        }
    }

    private fun updateBtnOptionBg() {
        if (memoType == 0) {
            binding.tvNotes.setBackgroundResource(R.drawable.shape_round16_2)
            binding.tvTodo.setBackgroundResource(R.drawable.shape_round16)
        } else {
            binding.tvNotes.setBackgroundResource(R.drawable.shape_round16)
            binding.tvTodo.setBackgroundResource(R.drawable.shape_round16_2)
        }
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
                delay(10000)
                save()
            }
        }
    }

    private fun removeWhiteSpace(inputStr: String?): String {
        var outputStr = ""
        if (!inputStr.isNullOrEmpty()) {
            //移除所有换行和空格
            outputStr = inputStr.replace("\\s+".toRegex(), "")
        }
        return outputStr
    }

    private fun save() {
        var title = getTitleText()
        val content = getContentText()
        val formatContent = removeWhiteSpace(content)
        val formatCurContent = removeWhiteSpace(curContentText)
        if (formatContent.isEmpty() || formatContent == formatCurContent) return
        if (title.isEmpty()) {
            title = SimpleDateFormat("hh:mm:ss", Locale.getDefault()).format(Date())
        }
        curTitleText = title
        Toast.makeText(this, "saving", Toast.LENGTH_SHORT).show()
        curContentText = content
        MemoU.saveMemo(curTitleText, getHtmlContentText(), content, labelColor, memoType)
        EventReportU.reportCustomEvent(EventReportU.text_autosave)
        Log.d(
            "-----",
            "auto save data---title:$title---content:$content---curTitleText:$curTitleText---curContentText:$curContentText"
        )
        binding.tvTodo.postDelayed(
            { Toast.makeText(this, "saved", Toast.LENGTH_SHORT).show() }, 2000
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
        }
        binding.tvDate.setOnClickListener {
//            val timeDialog = BottomTimeDialog(BottomTimeDialog.DATE_STR)
//            timeDialog.show(supportFragmentManager, "date_select")
            test()
        }
        binding.tvTime.setOnClickListener {
//            val timeDialog = BottomTimeDialog(BottomTimeDialog.TIME_STR)
//            timeDialog.show(supportFragmentManager, "time_select")
        }
        binding.icClose.setOnClickListener {
            binding.llcOptionSelectText.visibility = View.GONE
        }
        binding.tvText1.setOnClickListener {
            parseTitleAndText(0)
        }
        binding.tvText2.setOnClickListener {
            parseTitleAndText(1)
        }
        binding.tvNotes.setOnClickListener {
            memoType = 0
            showLock()
            updateOptionText()
            updateBtnOptionBg()
            optionHideTime()
        }
        binding.tvTodo.setOnClickListener {
            memoType = 1
            showLock()
            updateOptionText()
            updateBtnOptionBg()
            optionHideTime()
        }
    }

    private fun test() {
        val instance = Calendar.getInstance()
        instance.set(Calendar.YEAR, 2024)
        instance.set(Calendar.MONTH, Calendar.APRIL)
        instance.set(Calendar.DAY_OF_MONTH, 3)
        instance.set(Calendar.HOUR_OF_DAY, 10)
        instance.set(Calendar.MINUTE, 2)
        instance.set(Calendar.SECOND, 0)

        val intent = Intent(this, NotifacationBroadcast::class.java)
        val pendingIntent =
            PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        alarmManager.set(AlarmManager.RTC_WAKEUP, instance.timeInMillis, pendingIntent)
    }

    private fun parseTitleAndText(pos: Int) {
        val str = if (memoType == 0) {
            optionNotesTextData[pos]
        } else {
            optionTodoTextData[pos]
        }
        val split = str.split(";")
        if (split.size > 1) {
            val title = split[0]
            val content = split[1]
            if (title.isNotEmpty())
                binding.editTitle.setText(title)
            if (content.isNotEmpty()) {
                if (memoType == 0) {
                    if (content.contains("insert sequence number")) {
                        val changeContent = "$content<br>1.<br>2.<br>3.<br>4.<br>5.<br>"
                        binding.arEditor.fromHtml(changeContent)
                    } else
                        binding.arEditor.fromHtml(content)
                } else {
                    binding.arEditor.fromHtml(content)
                }
            }

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
        val title = getTitleText()
        val content = getContentText()
        if (content.isEmpty() || title.isEmpty()) {
            Toast.makeText(this, "The title or content cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }
        showAd.showOpenAd(emptyBack = true, showed = {}, close = {
            MemoU.saveMemo(title, getHtmlContentText(), content, labelColor, memoType)
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
            memoType = it.type
        }
        binding.iconDelete.show(null != MemoU.clickMemo)
    }

    private fun finishPage() {
        val content = getContentText()
        if (content.isNotEmpty()) {
            var title = getTitleText()
            if (title.isEmpty()) {
                title = SimpleDateFormat("hh:mm:ss", Locale.getDefault()).format(Date())
            }
            MemoU.saveMemo(title, getHtmlContentText(), content, labelColor, memoType)
            EventReportU.reportCustomEvent(EventReportU.text_autosave)
        }
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