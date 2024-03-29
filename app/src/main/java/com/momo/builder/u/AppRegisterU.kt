package com.momo.builder.u

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.os.Bundle
import com.blankj.utilcode.util.ActivityUtils
import com.google.android.gms.ads.AdActivity
import com.momo.builder.activity.LaunchActivity
import com.momo.builder.activity.MemoListActivity
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

object AppRegisterU : Application.ActivityLifecycleCallbacks {
    private var pages=0
    private var toLaunch=false
    private var job: Job?=null

    fun register(application: Application){
        application.registerActivityLifecycleCallbacks(this)
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        
    }

    override fun onActivityStarted(activity: Activity) {
        pages++
        job?.cancel()
        job=null
        if (pages==1){
            if (toLaunch){
                if (ActivityUtils.isActivityExistsInStack(MemoListActivity::class.java)){
                    activity.startActivity(Intent(activity, LaunchActivity::class.java))
                }
            }
            toLaunch=false
        }
    }

    override fun onActivityResumed(activity: Activity) {
        
    }

    override fun onActivityPaused(activity: Activity) {
        
    }

    override fun onActivityStopped(activity: Activity) {
        pages--
        if (pages<=0){
            job= GlobalScope.launch {
                delay(3000L)
                toLaunch=true
                ActivityUtils.finishActivity(LaunchActivity::class.java)
                ActivityUtils.finishActivity(AdActivity::class.java)
            }
        }
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
        
    }

    override fun onActivityDestroyed(activity: Activity) {
        
    }
}