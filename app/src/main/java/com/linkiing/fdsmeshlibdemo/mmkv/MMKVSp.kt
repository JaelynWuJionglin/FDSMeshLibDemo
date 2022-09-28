package com.linkiing.fdsmeshlibdemo.mmkv

import android.content.Context
import android.text.TextUtils
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.telink.ble.mesh.util.LOGUtils
import com.tencent.mmkv.MMKV

/**
 * mmkv数据缓存
 */
class MMKVSp {
    private lateinit var kv: MMKV
    private val gson = GsonBuilder().setPrettyPrinting().create()
    private val KV_StudioList = "KV_StudioList"

    companion object {
        val instance: MMKVSp by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            MMKVSp()
        }
    }

    fun init(context: Context) {
        val rootDir = MMKV.initialize(context)
        LOGUtils.d("MMKV root: $rootDir")

        kv = MMKV.defaultMMKV()
    }

    fun setStudioList(studioList: MutableList<String>) {
        val str = gson.toJson(studioList)
        kv.encode(KV_StudioList,str)
    }

    fun getStudioList(): MutableList<String> {
        val str = kv.decodeString(KV_StudioList)
        return if (TextUtils.isEmpty(str)) {
            mutableListOf()
        } else {
            val type = object : TypeToken<List<String>>() {}.type
            gson.fromJson(str, type)
        }
    }
}