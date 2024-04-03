package com.momo.builder.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.momo.builder.R
import com.momo.builder.databinding.FragmentBottomTimeBinding

class BottomTimeDialog(timeShowType: String) : BottomSheetDialogFragment() {
    companion object {
        const val DATE_STR = "dateStr"
        const val TIME_STR = "timeStr"
    }

    private var mTimeType = timeShowType
    private lateinit var binding: FragmentBottomTimeBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.CusBottomDialogTheme)
        dialog?.window?.attributes?.apply {
            width = WindowManager.LayoutParams.MATCH_PARENT
            height = WindowManager.LayoutParams.WRAP_CONTENT
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val inflate = inflater.inflate(R.layout.fragment_bottom_time, container, false)
        binding = FragmentBottomTimeBinding.bind(inflate)
        onClickSet()
        return inflate
    }

    private fun onClickSet() {
        binding.cancel.setOnClickListener { dismiss() }
        binding.apply.setOnClickListener { dismiss() }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }
}