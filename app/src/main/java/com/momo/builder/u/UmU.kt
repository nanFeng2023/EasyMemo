package com.momo.builder.u

import android.app.Activity
import com.google.android.ump.ConsentDebugSettings
import com.google.android.ump.ConsentForm
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform

object UmU {
    private var consentInformation: ConsentInformation? = null

    fun reqConsentInfo(context: Activity, callBack: (Boolean) -> Unit) {

        val debugSettings = ConsentDebugSettings.Builder(context)
            .setDebugGeography(ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_EEA)
            .build()
        val params = ConsentRequestParameters
            .Builder()
            .setConsentDebugSettings(debugSettings)
            .build()

        consentInformation = UserMessagingPlatform.getConsentInformation(context)
        consentInformation?.requestConsentInfoUpdate(context,
            params,
            {
                UserMessagingPlatform.loadAndShowConsentFormIfRequired(context,
                    ConsentForm.OnConsentFormDismissedListener { loadAndShowError ->
                        if (loadAndShowError != null || consentInformation?.canRequestAds() == false) {
                            callBack.invoke(false)
                            return@OnConsentFormDismissedListener
                        }
                        if (consentInformation?.canRequestAds() == true) {
                            callBack.invoke(true)
                        }
                    }
                )
            },
            {
                callBack.invoke(false)
            })
    }
}