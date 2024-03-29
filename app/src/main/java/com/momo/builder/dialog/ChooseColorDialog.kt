package com.momo.builder.dialog

import android.view.LayoutInflater
import androidx.recyclerview.widget.GridLayoutManager
import com.momo.builder.adapter.ColorAdapter
import com.momo.builder.baseactivity.BaseDialog
import com.momo.builder.databinding.DialogChooseColorBinding

class ChooseColorDialog (
    private val chooseLabel:Boolean,
    private val choosedColor:String,
    private val clickColor:(color:String)->Unit
): BaseDialog<DialogChooseColorBinding>() {

    override fun initBindingView(inflater: LayoutInflater): DialogChooseColorBinding = DialogChooseColorBinding.inflate(inflater)

    override fun onView() {
        binding.rvColor.apply {
            layoutManager=GridLayoutManager(context,3)
            adapter=ColorAdapter(context,chooseLabel,choosedColor){
                dismiss()
                clickColor.invoke(it)
            }
        }
        binding.iconCancel.setOnClickListener { dismiss() }
    }
}