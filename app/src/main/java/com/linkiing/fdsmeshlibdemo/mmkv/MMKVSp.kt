package com.linkiing.fdsmeshlibdemo.mmkv

import android.content.Context
import android.text.TextUtils
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.linkiing.fdsmeshlibdemo.bean.StudioListBean
import com.telink.ble.mesh.util.LOGUtils
import com.tencent.mmkv.MMKV

/**
 * mmkv数据保存
 */
class MMKVSp {
    private lateinit var kv: MMKV
    private val gson = GsonBuilder().setPrettyPrinting().create()
    private val KV_StudioList = "KV_StudioList"
    private val KV_IsTestModel = "KV_IsTestModel"

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

    fun setStudioList(studioList: MutableList<StudioListBean>) {
        val str = gson.toJson(studioList)
        kv.encode(KV_StudioList, str)
    }

    fun getStudioList(): MutableList<StudioListBean> {
        val str = kv.decodeString(KV_StudioList)
        return if (TextUtils.isEmpty(str)) {
            mutableListOf()
        } else {
            val type = object : TypeToken<List<StudioListBean>>() {}.type
            gson.fromJson(str, type)
        }
    }

    fun setTestModel(isTestModel: Boolean) {
        kv.encode(KV_IsTestModel, isTestModel)
    }

    fun isTestModel(): Boolean {
        return kv.decodeBool(KV_IsTestModel,false)
    }
}