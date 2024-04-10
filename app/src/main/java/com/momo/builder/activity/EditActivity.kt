package com.momo.builder.activity

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.addCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.app.ActivityCompat
import androidx.core.view.get
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.jzxiang.pickerview.TimePickerDialog
import com.jzxiang.pickerview.data.Type
import com.jzxiang.pickerview.listener.OnDateSetListener
import com.momo.builder.R
import com.momo.builder.adapter.EditFuncAdapter
import com.momo.builder.admob.LoadAd
import com.momo.builder.admob.ShowAd
import com.momo.builder.baseactivity.BaseViewBinding
import com.momo.builder.bean.MemoBean
import com.momo.builder.broadcast.NotificationBroadcast
import com.momo.builder.conf.LocalConf
import com.momo.builder.databinding.ActivityEditBinding
import com.momo.builder.dialog.ChooseColorDialog
import com.momo.builder.dialog.DeleteDialog
import com.momo.builder.message
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
    private var fontColor = "#575757"
    private val showAd by lazy { ShowAd(LoadAd.SAVE_INTER, this) }
    private val funcAdapter by lazy { EditFuncAdapter(this, fontColor) { clickItem(it) } }
    private var enterTime = System.currentTimeMillis()
    private var hideJob: Job? = null
    override fun initViewBinding(): ActivityEditBinding =
        ActivityEditBinding.inflate(layoutInflater)

    private val optionNotesTextData = arrayListOf(
        "Quick text insertion;Today ${MemoU.obtainDate()};",
        "Today ${MemoU.obtainDate()};Quick insert sequence number;"
    )
    private val optionTodoTextData = arrayListOf(
        "Work List;Today ${MemoU.obtainDate()};"
    )
    private var timeSelDialog: TimePickerDialog? = null
    private var mOldTimeIndex = 0
    private var firstJumpNotificationPermission = false
    private val memoBean = MemoBean(
        SimpleDateFormat("hh:mm:ss", Locale.getDefault()).format(Date()),
        "",
        "",
        "#F36E9D",
        System.currentTimeMillis()
    )
    private var noSetLockTime = true

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
        message.observe(this) {
            if (it == "click_homekey") {
                finishBeforeSave()
                message.value = ""
            }
        }
    }

    private fun initSetBgColor() {
        val colorList = arrayListOf<String>()
        colorList.add("#F36E9D")
        colorList.add("#F3EDEF")
        colorList.add("#E6F2FF")
        colorList.add("#F1EFFF")
        colorList.add("#F8FFE6")
        colorList.add("#FFF2E6")
        memoBean.labelColor = colorList[Random(System.currentTimeMillis()).nextInt(colorList.size)]
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
        updateBtnOptionBg()
        showLock()
        initLockTime()
        lockIvChange()
    }

    private fun showLock() {
        if (memoBean.type == 0) {
            binding.llcLock.visibility = View.GONE
        } else {
            if (MemoU.clickMemo?.isDone == true) binding.llcLock.visibility = View.GONE
            else binding.llcLock.visibility = View.VISIBLE
        }
    }

    private var year = 0
    private var month = 0
    private var day = 0
    private var hour = 0
    private var minute = 0

    private fun initLockTime() {
        val calendar = Calendar.getInstance()
        if (memoBean.isNewCreate) {
            calendar.timeInMillis = System.currentTimeMillis()
            calendar.add(Calendar.MINUTE, 5)
        } else {
            calendar.timeInMillis = memoBean.lockTime
        }
        year = calendar.get(Calendar.YEAR)
        month = calendar.get(Calendar.MONTH)
        day = calendar.get(Calendar.DAY_OF_MONTH)
        hour = calendar.get(Calendar.HOUR_OF_DAY)
        minute = calendar.get(Calendar.MINUTE)
        binding.tvDate.text =
            buildString { append(year).append("-").append(month + 1).append("-").append(day) }
        binding.tvTime.text = buildString { append(hour).append(":").append(minute) }
    }

    private fun setLockTime(mill: Long, type: Int = 1) {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = mill
        if (type == 1) {
            hour = calendar.get(Calendar.HOUR_OF_DAY)
            minute = calendar.get(Calendar.MINUTE)
            binding.tvTime.text = buildString { append(hour).append(":").append(minute) }
        } else {
            year = calendar.get(Calendar.YEAR)
            month = calendar.get(Calendar.MONTH)
            day = calendar.get(Calendar.DAY_OF_MONTH)
            binding.tvDate.text =
                buildString { append(year).append("-").append(month + 1).append("-").append(day) }
        }
    }

    private fun updateOptionText() {
        if (memoBean.type == 0) {
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
        if (memoBean.type == 0) {
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
        val formatCurContent = removeWhiteSpace(memoBean.content)
        if (formatContent.isEmpty() || formatContent == formatCurContent) return
        if (title.isEmpty()) {
            title = SimpleDateFormat("hh:mm:ss", Locale.getDefault()).format(Date())
        }
        Toast.makeText(this, "saving", Toast.LENGTH_SHORT).show()
        saveAndReport(title, content, getHtmlContentText(), EventReportU.text_autosave)
        Log.d(
            "-----",
            "auto save data---title:$title---content:$content---curTitleText:${memoBean.title}---curContentText:${memoBean.content}"
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
            ChooseColorDialog(true, memoBean.labelColor) {
                memoBean.labelColor = it
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
        binding.tvDate.setOnClickListener {
            timePick(0)
            EventReportU.reportCustomEvent(EventReportU.text_notification)
        }
        binding.tvTime.setOnClickListener {
            timePick(0)
            EventReportU.reportCustomEvent(EventReportU.text_notification)
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
            memoBean.type = 0
            showLock()
            updateOptionText()
            updateBtnOptionBg()
            optionHideTime()
        }
        binding.tvTodo.setOnClickListener {
            memoBean.type = 1
            showLock()
            updateOptionText()
            updateBtnOptionBg()
            optionHideTime()
        }
        binding.llcLock.setOnClickListener {
            openLock(!memoBean.isLockOpen)
        }
    }

    private fun notifiPermissionReq() {
        val builder = AlertDialog.Builder(this)
        builder.setIcon(R.mipmap.ic_launcher_round)
        builder.setMessage("The notification function requires the notification right")
        builder.setPositiveButton("ok") { dialog, _ ->
            dialog.dismiss()
            try {
                Intent().apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    action = "android.settings.APP_NOTIFICATION_SETTINGS"
                    putExtra("app_package", packageName)
                    putExtra("android.provider.extra.APP_PACKAGE", packageName)
                    putExtra("app_uid", applicationInfo.uid)
                    startActivity(this)
                    firstJumpNotificationPermission = true
                }
            } catch (e: Exception) {
                Intent().apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    action = "android.settings.APPLICATION_DETAILS_SETTINGS"
                    data = Uri.fromParts("package", packageName, null)
                    startActivity(this)
                    firstJumpNotificationPermission = true
                }
            }
        }.setNegativeButton("cancel") { dialog, _ ->
            dialog.dismiss()
        }
        builder.show()
    }

    private fun openLock(isOpen: Boolean) {
        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            notifiPermissionReq()
        } else {
            if (memoBean.timeType == 0 && !memoBean.isLockOpen) {
                val setTimeMillis = buildCalender().timeInMillis
                if (setTimeMillis < System.currentTimeMillis()) {
                    Toast.makeText(
                        this, "The set time must be later than the current time", Toast.LENGTH_SHORT
                    ).show()
                    return
                }
            }
            memoBean.isLockOpen = isOpen
            lockIvChange()
        }
    }

    private fun lockIvChange() {
        if (memoBean.isLockOpen) {
            binding.llcLock.setBackgroundResource(R.drawable.shape_round8_2)
            binding.ivLock.setImageResource(R.mipmap.ic_lock_yellow)
            EventReportU.reportCustomEvent(EventReportU.text_notification)
        } else {
            binding.llcLock.setBackgroundResource(R.drawable.shape_round8)
            binding.ivLock.setImageResource(R.mipmap.ic_clock_icon)
        }
    }

    private val timeSetLaunch =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == 100) {
                it.data?.apply {
                    mOldTimeIndex = memoBean.timeType
                    memoBean.timeType = getIntExtra("index", 0)
                    val text = getStringExtra("text")
                    if (!text.isNullOrBlank()) {
                        timeSelDialog?.setRepeatTimeText(text)
                    }
                }
            }
        }

    private fun timePick(type: Int) {
        timeSelDialog = TimePickerDialog.Builder()
            .setType(if (type == 0) Type.YEAR_MONTH_DAY else Type.HOURS_MINS)
            .setCancelStringId("Cancel").setSureStringId(if (type == 0) "Next" else "Apply")
            .setWheelItemTextNormalColor(Color.parseColor("#1C259C"))
            .setTimeRepeatVisible(type != 0).setDialogType(type)
            .setWheelItemTextSelectorColor(Color.BLACK).setWheelItemTextSize(14).setCyclic(true)
            .setTimeType(memoBean.timeType)
            .setCurrentMillseconds(buildCalender().timeInMillis).setCallBack(onDataListener).build()
        timeSelDialog!!.show(supportFragmentManager, "timePick")
        timeSelDialog!!.onRepeat = {
            val intent = Intent(this, TimeSetActivity::class.java)
            intent.putExtra("timeType", memoBean.timeType)
            timeSetLaunch.launch(intent)
        }
        timeSelDialog!!.onApply = {
            if (it == 0) {
                timePick(1)
            }
        }
    }

    private val onDataListener = OnDateSetListener { _, mill, type ->
        setLockTime(mill, type)
        if (type == 1) {
            openLock(true)
        }
    }

    private fun buildCalender(): Calendar {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.YEAR, year)
        calendar.set(Calendar.MONTH, month)
        calendar.set(Calendar.DAY_OF_MONTH, day)
        calendar.set(Calendar.HOUR_OF_DAY, hour)
        calendar.set(Calendar.MINUTE, minute)
        calendar.set(Calendar.SECOND, 0)
        return calendar
    }

    private fun isSameTime(): Boolean {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = memoBean.lockTime
        val mYear = calendar.get(Calendar.YEAR)
        val mMonth = calendar.get(Calendar.MONTH)
        val mDay = calendar.get(Calendar.DAY_OF_MONTH)
        val mHour = calendar.get(Calendar.HOUR_OF_DAY)
        val mMinute = calendar.get(Calendar.MINUTE)
        return mYear == year && mMonth == month && mDay == day && mHour == hour && mMinute == minute && memoBean.timeType == mOldTimeIndex
    }

    private fun getLockIntent(action: String = NotificationBroadcast.actionClock): Intent {
        val intent = Intent(action)
        intent.putExtra("title", memoBean.title)
        intent.putExtra("content", memoBean.content)
        return intent
    }

    @SuppressLint("ScheduleExactAlarm")
    private fun timeLock() {
        val systemTime = getFormatTime(System.currentTimeMillis())
        Log.d("----", "timeLock---clockId:${memoBean.clockId}")
        EventReportU.reportCustomEvent(EventReportU.text_notification_done)

        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        //取消之前的设置
        cancelAlarm()
        val calendar = buildCalender()
        when (memoBean.timeType) {
            0 -> {
                val intent = getLockIntent()
                val pendingIntent = PendingIntent.getBroadcast(
                    this, memoBean.clockId, intent, PendingIntent.FLAG_IMMUTABLE
                )
                try {
                    memoBean.lockTime = calendar.timeInMillis
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP, memoBean.lockTime, pendingIntent
                    )
                    Log.d("----", "timeLock---111")
                } catch (e: Exception) {
                    alarmManager.set(AlarmManager.RTC_WAKEUP, memoBean.lockTime, pendingIntent)
                    Log.d("----", "timeLock---222")
                }
                Log.d(
                    "----",
                    "timeLock---selectTime:${getFormatTime(memoBean.lockTime)}---systemTime:$systemTime---clockId:${memoBean.clockId}"
                )
            }

            1 -> {//每天
                val cal = Calendar.getInstance()
                cal.set(Calendar.HOUR_OF_DAY, hour)
                cal.set(Calendar.MINUTE, minute)
                cal.set(Calendar.SECOND, 0)
                if (System.currentTimeMillis() > cal.timeInMillis) {
                    cal.add(Calendar.DAY_OF_MONTH, 1)
                }
                val intent = getRepeatIntent()
                val pendingIntent = PendingIntent.getBroadcast(
                    this, memoBean.clockId, intent, PendingIntent.FLAG_IMMUTABLE
                )
                try {
                    memoBean.lockTime = calendar.timeInMillis
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP, memoBean.lockTime, pendingIntent
                    )
                } catch (e: Exception) {
                    alarmManager.set(AlarmManager.RTC_WAKEUP, memoBean.lockTime, pendingIntent)
                }
                Log.d(
                    "----",
                    "timeLock---every day---selectTime:${getFormatTime(cal.timeInMillis)}---systemTime:$systemTime---clockId:${memoBean.clockId}"
                )
            }

            2 -> {//明天
                calendar.add(Calendar.DAY_OF_MONTH, 1)
                memoBean.lockTime = calendar.timeInMillis
                val intent = getLockIntent()
                val pendingIntent = PendingIntent.getBroadcast(
                    this, memoBean.clockId, intent, PendingIntent.FLAG_IMMUTABLE
                )
                try {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP, memoBean.lockTime, pendingIntent
                    )
                } catch (e: Exception) {
                    alarmManager.set(AlarmManager.RTC_WAKEUP, memoBean.lockTime, pendingIntent)
                }
                Log.d(
                    "----",
                    "timeLock---tomorrow---selectTime:${getFormatTime(memoBean.lockTime)}---systemTime:$systemTime---clockId:${memoBean.clockId}"
                )
            }

            3 -> {//周一到周五
                val setHour = calendar.get(Calendar.HOUR_OF_DAY)
                val setMinute = calendar.get(Calendar.MINUTE)
                periodAlarm(arrayOf("2", "3", "4", "5", "6"), setHour, setMinute)
                memoBean.lockTime = calendar.timeInMillis
            }

            4 -> {//周六，周末
                val setHour = calendar.get(Calendar.HOUR_OF_DAY)
                val setMinute = calendar.get(Calendar.MINUTE)
                periodAlarm(arrayOf("7", "1"), setHour, setMinute)
                memoBean.lockTime = calendar.timeInMillis
            }
        }
    }

    private fun getRepeatIntent(): Intent {
        val intent = getLockIntent(NotificationBroadcast.actionRepeatClock)
        intent.putExtra("hour", hour)
        intent.putExtra("minute", minute)
        intent.putExtra("clockId", memoBean.clockId)
        return intent
    }

    private fun getPeriodIntent(week: Int): Intent {
        val intent = getLockIntent(NotificationBroadcast.actionPeriodClock)
        intent.putExtra("hour", hour)
        intent.putExtra("minute", minute)
        intent.putExtra("clockId", memoBean.clockId)
        intent.putExtra("week", week)
        return intent
    }

    private fun getFormatTime(l: Long): String {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = l
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        val second = calendar.get(Calendar.SECOND)
        val builder = StringBuilder()
        builder.append(year).append("-").append(month).append("-").append(day).append(" ")
            .append(hour).append(":").append(minute).append(":").append(second)
        return builder.toString()
    }

    private fun periodAlarm(periodList: Array<String>, hour: Int, minute: Int) {
        for (i in periodList.indices) {
            setPeriodAlarm(periodList[i].toInt(), hour, minute)
        }
    }

    private fun setPeriodAlarm(week: Int, hour: Int, minute: Int) {
        val calendar = setCommonAlarmCalender(hour, minute)
        calendar.set(Calendar.DAY_OF_WEEK, week)
        val alarmId = memoBean.clockId + week
        val intent = getPeriodIntent(week)
        val pendingIntent = PendingIntent.getBroadcast(
            this, alarmId, intent, PendingIntent.FLAG_IMMUTABLE
        )
        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        if (System.currentTimeMillis() > calendar.timeInMillis) {
            calendar.add(Calendar.DAY_OF_MONTH, 7)
        }
        try {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent
            )
        } catch (e: Exception) {
            alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
        }
        Log.d(
            "----",
            "timeLock---period---selectTime:${getFormatTime(calendar.timeInMillis)}---systemTime:${
                getFormatTime(
                    System.currentTimeMillis()
                )
            }---clockId:${alarmId}"
        )
    }

    private fun setCommonAlarmCalender(hour: Int, minute: Int): Calendar {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, hour)
        calendar.set(Calendar.MINUTE, minute)
        calendar.set(Calendar.SECOND, 0)
        return calendar
    }

    private fun cancelAlarm() {
        Log.d("----", "cancelAlarm---clockId:${memoBean.clockId}---timeType:${memoBean.timeType}")
        when (memoBean.timeType) {
            0, 2 -> realCancelAlarm(NotificationBroadcast.actionClock)
            1 -> realCancelAlarm(NotificationBroadcast.actionRepeatClock)
            3 -> periodCancelAlarm(arrayOf("2", "3", "4", "5", "6"))
            4 -> periodCancelAlarm(arrayOf("7", "1"))
        }
    }

    private fun realCancelAlarm(action: String) {
        val intent = when (action) {
            NotificationBroadcast.actionClock -> {
                getLockIntent(action)
            }

            else -> {
                getRepeatIntent()
            }
        }
        val pendingIntent = PendingIntent.getBroadcast(
            this, memoBean.clockId, intent, PendingIntent.FLAG_IMMUTABLE
        )
        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(pendingIntent)
    }

    private fun periodCancelAlarm(periodList: Array<String>) {
        for (i in periodList) {
            realCancelPeriodAlarm(i.toInt())
        }
    }

    private fun realCancelPeriodAlarm(week: Int) {
        val intent = getPeriodIntent(week)
        val pendingIntent = PendingIntent.getBroadcast(
            this, memoBean.clockId + week, intent, PendingIntent.FLAG_IMMUTABLE
        )
        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(pendingIntent)
    }

    private fun parseTitleAndText(pos: Int) {
        val str = if (memoBean.type == 0) {
            optionNotesTextData[pos]
        } else {
            optionTodoTextData[pos]
        }
        val split = str.split(";")
        if (split.size > 1) {
            val title = split[0]
            val content = split[1]
            if (title.isNotEmpty()) binding.editTitle.setText(title)
            if (content.isNotEmpty()) {
                if (memoBean.type == 0) {
                    if (content.contains("insert sequence number")) {
                        val changeContent = "$content<br>1.<br>2.<br>3.<br>4.<br>5.<br>"
                        binding.arEditor.fromHtml(changeContent)
                    } else binding.arEditor.fromHtml(content)
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
        binding.editTitle.setBackgroundColor(Color.parseColor(memoBean.labelColor))
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
        saveAndReport(title, content, getHtmlContentText(), EventReportU.text_save)
        back()
    }

    private fun saveAndReport(
        title: String, content: String, htmlContent: String, reportParam: String
    ) {
        memoBean.title = title
        memoBean.content = content
        memoBean.htmlContent = htmlContent
        if (memoBean.type == 1) {
            //是否开启lock
            if (memoBean.isLockOpen) {
                if (noSetLockTime) {
                    noSetLockTime = false
                    timeLock()
                } else {
                    if (!isSameTime()) {
                        timeLock()
                    }
                }
            } else {
                val calender = buildCalender()
                memoBean.lockTime = calender.timeInMillis
                cancelAlarm()
            }
        }
        memoBean.isNewCreate = MemoU.clickMemo == null
        MemoU.saveMemo(memoBean)
        EventReportU.reportCustomEvent(reportParam)
    }

    private fun back() {
        setResult(100)
        finish()
    }

    private fun getMemoBean() {
        MemoU.clickMemo?.let {
            memoBean.labelColor = it.labelColor
            memoBean.title = it.title
            memoBean.htmlContent = it.htmlContent
            memoBean.content = it.content
            memoBean.type = it.type
            memoBean.lockTime = it.lockTime
            memoBean.isLockOpen = it.isLockOpen
            memoBean.isNewCreate = false
            memoBean.clockId = it.clockId
            memoBean.timeType = it.timeType
            binding.editTitle.setText(it.title)
            binding.arEditor.fromHtml(it.htmlContent)
            binding.llcBtnOption.visibility = View.GONE
            //解决：设置时间后编辑页面再次进来，返回保存又会触发自动设置时间，导致弹窗
            if (memoBean.isLockOpen && isSameTime()) {
                noSetLockTime = false
            }
        }
        binding.iconDelete.show(null != MemoU.clickMemo)
    }

    private fun finishPage() {
        finishBeforeSave()
        back()
    }

    private fun finishBeforeSave() {
        val content = getContentText()
        if (content.isNotEmpty()) {
            var title = getTitleText()
            if (title.isEmpty()) {
                title = SimpleDateFormat("hh:mm:ss", Locale.getDefault()).format(Date())
            }
            saveAndReport(title, content, getHtmlContentText(), EventReportU.text_autosave)
        }
    }

    override fun onResume() {
        super.onResume()
        enterTime = System.currentTimeMillis()
        if (firstJumpNotificationPermission && ActivityCompat.checkSelfPermission(
                this, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            if (memoBean.timeType == 0 && !memoBean.isLockOpen) {
                val setTimeMillis = buildCalender().timeInMillis
                if (setTimeMillis < System.currentTimeMillis()) {
                    Toast.makeText(
                        this, "The set time must be later than the current time", Toast.LENGTH_SHORT
                    ).show()
                    return
                }
            }
            memoBean.isLockOpen = true
            lockIvChange()
            firstJumpNotificationPermission = false
        }
    }

    override fun onPause() {
        super.onPause()
        EventReportU.reportCustomEvent(EventReportU.text_stay, Bundle().apply {
            putLong("time", System.currentTimeMillis() - enterTime)
        })
    }
}