package com.momo.builder.dialog

import android.view.LayoutInflater
import com.momo.builder.baseactivity.BaseDialog
import com.momo.builder.databinding.DialogDeleteBinding

class DeleteDialog(
    private val contentStr:String,
    private val sure:()->Unit
):BaseDialog<DialogDeleteBinding>() {

    override fun initBindingView(inflater: LayoutInflater): DialogDeleteBinding = DialogDeleteBinding.inflate(inflater)

    override fun onView() {
        binding.tv.text=contentStr
        binding.tvCancel.setOnClickListener { dismiss() }
        binding.tvSure.setOnClickListener {
            dismiss()
            sure.invoke()
        }
    }
}