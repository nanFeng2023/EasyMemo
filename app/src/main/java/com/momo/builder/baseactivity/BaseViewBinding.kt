package com.momo.builder.baseactivity

import android.os.Bundle
import androidx.viewbinding.ViewBinding

abstract class BaseViewBinding<T : ViewBinding>:BaseActivity() {
    protected lateinit var binding: T

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=initViewBinding()
        setContentView(binding.root)
        onView()
        setClickListener()
    }

    abstract fun initViewBinding():T

    abstract fun onView()

    open fun setClickListener(){}
}