package com.momo.builder.baseactivity

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity

abstract class BaseActivity:AppCompatActivity() {
    var resume=false
//    private lateinit var immersionBar: ImmersionBar
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        initImmersionBar()
    }

//    private fun initImmersionBar(){
//        immersionBar=ImmersionBar.with(this).apply {
//            statusBarAlpha(0f)
//            autoDarkModeEnable(true)
//            statusBarDarkFont(true)
//            init()
//        }
//    }

    fun setStatus(view:View){
//        immersionBar.statusBarView(view).init()
    }

    override fun onResume() {
        super.onResume()
        resume=true
    }

    override fun onPause() {
        super.onPause()
        resume=false
    }

    override fun onStop() {
        super.onStop()
        resume=false
    }
}