package com.momo.builder.conf

import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.momo.builder.BuildConfig
import com.momo.builder.admob.AdInfo

object FireConf {
    fun getFireConf() {
        if (!BuildConfig.DEBUG) {
            runCatching {
                val remoteConfig = Firebase.remoteConfig
                remoteConfig.fetchAndActivate().addOnCompleteListener {
                    if (it.isSuccessful) {
                        AdInfo.setMax(remoteConfig.getString("fE2_epifa"))
                    }
                }
            }
        }
    }
}