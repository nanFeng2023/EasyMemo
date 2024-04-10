package com.momo.builder.u

import com.tencent.mmkv.MMKV

object MmkvU {
    const val INSTALL_REFERER = "install_referer"
    const val INSTALL_VERSION = "install_version"
    const val LIMIT_TRACK = "limit_track"
    const val CLICK_START_MS = "click_start_ms"
    const val APP_INSTALL_MS = "app_install_ms"
    const val CLICK_START_SERVER_MS = "click_start_server_ms"
    const val APP_INSTALL_SERVER_MS = "app_install_server_ms"
    const val APP_7_DAY = "app_7_day"
    const val INSTALL_EVENT_FAIL = "install_event_fail"

    const val FIRST_INSTALL = "first_install"
    const val GAID = "gaid"

    const val IS_APPRAISE = "isAppraise"
    const val CLOCK_ID="clock_id"
    fun saveStr(key: String, value: String) {
        MMKV.defaultMMKV().encode(key, value)
    }

    fun getStr(key: String) = MMKV.defaultMMKV().decodeString(key) ?: ""

    fun saveInt(key: String, value: Int) {
        MMKV.defaultMMKV().encode(key, value)
    }

    fun getInt(key: String) = MMKV.defaultMMKV().decodeInt(key, 0)

    fun saveLong(key: String, value: Long) {
        MMKV.defaultMMKV().encode(key, value)
    }

    fun getLong(key: String) = MMKV.defaultMMKV().decodeLong(key, 0)

    fun saveBoolean(key: String, value: Boolean) {
        MMKV.defaultMMKV().encode(key, value)
    }

    fun getBoolean(key: String) = MMKV.defaultMMKV().decodeBool(key, false)
}