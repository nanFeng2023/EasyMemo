package com.jzxiang.pickerview

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.opengl.Visibility
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import com.jzxiang.pickerview.config.PickerConfig
import com.jzxiang.pickerview.data.Type
import com.jzxiang.pickerview.data.WheelCalendar
import com.jzxiang.pickerview.databinding.TimepickerLayoutBinding
import com.jzxiang.pickerview.listener.OnDateSetListener
import java.util.Calendar

/**
 * Created by jzxiang on 16/4/19.
 */
class TimePickerDialog : DialogFragment(), View.OnClickListener {
    private var mPickerConfig: PickerConfig? = null
    private var mTimeWheel: TimeWheel? = null
    private var mCurrentMillSeconds: Long = 0
    lateinit var onRepeat: () -> Unit
    lateinit var onApply: (type: Int) -> Unit
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val activity: Activity = requireActivity()
        activity.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
    }

    override fun onResume() {
        super.onResume()
        val width =
            getScreenWidth(requireContext()) - resources.getDimensionPixelSize(R.dimen.margin_width)
        val height = resources.getDimensionPixelSize(R.dimen.picker_height)
        val window = dialog?.window!!
        window.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
        window.setGravity(Gravity.BOTTOM)
        window.attributes.y = resources.getDimensionPixelSize(R.dimen.margin_width)
        window.setBackgroundDrawableResource(R.drawable.shape_round24)
    }

    private fun getScreenWidth(context: Context): Int {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val metrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(metrics)
        return metrics.widthPixels
    }

    private fun initialize(pickerConfig: PickerConfig) {
        mPickerConfig = pickerConfig
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = Dialog(requireActivity(), R.style.Dialog_NoTitle)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setCanceledOnTouchOutside(true)
        dialog.setContentView(initView())
        return dialog
    }

    private lateinit var mBinding: TimepickerLayoutBinding
    private fun initView(): View {
        val view = LayoutInflater.from(context).inflate(R.layout.timepicker_layout, null)
        mBinding = TimepickerLayoutBinding.bind(view)
        mBinding.cancel.setOnClickListener(this)
        mBinding.apply.setOnClickListener(this)
        mBinding.llcRepeatTime.setOnClickListener(this)
        mBinding.cancel.text = mPickerConfig?.mCancelString
        mBinding.apply.text = mPickerConfig?.mSureString
        if (mPickerConfig?.repeatTimeVisible == true)
            mBinding.llcRepeatTime.visibility = View.VISIBLE
        else mBinding.llcRepeatTime.visibility = View.GONE
        repeatTimeSetText()
        mTimeWheel = TimeWheel(view, mPickerConfig)
        return view
    }


    override fun onClick(v: View) {
        when (v.id) {
            R.id.apply -> {
                sureClicked()
                onApply.invoke(mPickerConfig?.dialogType ?: 0)
            }

            R.id.cancel -> {
                dismiss()
            }

            R.id.llc_repeat_time -> {
                onRepeat.invoke()
            }
        }
    }

    private fun repeatTimeSetText() {
        val text = when (mPickerConfig?.timeType) {
            0 -> "One-time event"
            1 -> "Daily"
            2 -> "Tomorrow"
            3 -> "Weekday"
            4 -> "Weekend"
            else -> {
                "One-time event"
            }
        }
        setRepeatTimeText(text)
    }

    fun setRepeatTimeText(text: String) {
        mBinding.atvTime.text = text
    }

    val currentMillSeconds: Long
        get() = if (mCurrentMillSeconds == 0L) System.currentTimeMillis() else mCurrentMillSeconds

    /*
     * @desc This method is called when onClick method is invoked by sure button. A Calendar instance is created and
     *       initialized.
     * @param none
     * @return none
     */
    private fun sureClicked() {
        val calendar = Calendar.getInstance()
        calendar.clear()
        calendar[Calendar.YEAR] = mTimeWheel!!.currentYear
        calendar[Calendar.MONTH] = mTimeWheel!!.currentMonth - 1
        calendar[Calendar.DAY_OF_MONTH] = mTimeWheel!!.currentDay
        calendar[Calendar.HOUR_OF_DAY] = mTimeWheel!!.currentHour
        calendar[Calendar.MINUTE] = mTimeWheel!!.currentMinute
        mCurrentMillSeconds = calendar.timeInMillis
        if (mPickerConfig!!.mCallBack != null) {
            mPickerConfig!!.mCallBack.onDateSet(
                this,
                mCurrentMillSeconds,
                mPickerConfig?.dialogType
            )
        }
        dismiss()
    }

    class Builder {
        private var mPickerConfig: PickerConfig = PickerConfig()

        fun setType(type: Type?): Builder {
            mPickerConfig.mType = type
            return this
        }

        fun setThemeColor(color: Int): Builder {
            mPickerConfig.mThemeColor = color
            return this
        }

        fun setCancelStringId(left: String?): Builder {
            mPickerConfig.mCancelString = left
            return this
        }

        fun setSureStringId(right: String?): Builder {
            mPickerConfig.mSureString = right
            return this
        }

        fun setTitleStringId(title: String?): Builder {
            mPickerConfig.mTitleString = title
            return this
        }

        fun setToolBarTextColor(color: Int): Builder {
            mPickerConfig.mToolBarTVColor = color
            return this
        }

        fun setWheelItemTextNormalColor(color: Int): Builder {
            mPickerConfig.mWheelTVNormalColor = color
            return this
        }

        fun setWheelItemTextSelectorColor(color: Int): Builder {
            mPickerConfig.mWheelTVSelectorColor = color
            return this
        }

        fun setWheelItemTextSize(size: Int): Builder {
            mPickerConfig.mWheelTVSize = size
            return this
        }

        fun setCyclic(cyclic: Boolean): Builder {
            mPickerConfig.cyclic = cyclic
            return this
        }

        fun setMinMillseconds(millseconds: Long): Builder {
            mPickerConfig.mMinCalendar = WheelCalendar(millseconds)
            return this
        }

        fun setMaxMillseconds(millseconds: Long): Builder {
            mPickerConfig.mMaxCalendar = WheelCalendar(millseconds)
            return this
        }

        fun setCurrentMillseconds(millseconds: Long): Builder {
            mPickerConfig.mCurrentCalendar = WheelCalendar(millseconds)
            return this
        }

        fun setYearText(year: String?): Builder {
            mPickerConfig.mYear = year
            return this
        }

        fun setMonthText(month: String?): Builder {
            mPickerConfig.mMonth = month
            return this
        }

        fun setDayText(day: String?): Builder {
            mPickerConfig.mDay = day
            return this
        }

        fun setHourText(hour: String?): Builder {
            mPickerConfig.mHour = hour
            return this
        }

        fun setMinuteText(minute: String?): Builder {
            mPickerConfig.mMinute = minute
            return this
        }

        fun setCallBack(listener: OnDateSetListener?): Builder {
            mPickerConfig.mCallBack = listener
            return this
        }

        fun setTimeRepeatVisible(visibility: Boolean): Builder {
            mPickerConfig.repeatTimeVisible = visibility
            return this
        }

        fun setDialogType(type: Int): Builder {
            mPickerConfig.dialogType = type
            return this
        }

        fun setTimeType(type: Int): Builder {
            mPickerConfig.timeType = type
            return this
        }

        fun build(): TimePickerDialog {
            return newInstance(mPickerConfig)
        }
    }

    companion object {
        private fun newInstance(pickerConfig: PickerConfig): TimePickerDialog {
            val timePickerDialog = TimePickerDialog()
            timePickerDialog.initialize(pickerConfig)
            return timePickerDialog
        }
    }
}
