package com.linkiing.fdsmeshlibdemo.mmkv

import android.content.Context
import android.text.TextUtils
import com.base.mesh.api.log.LOGUtils
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.linkiing.fdsmeshlibdemo.bean.StudioListBean
import com.tencent.mmkv.MMKV

/**
 * mmkv数据保存
 */
class MMKVSp {
    private lateinit var kv: MMKV
    private val gson = GsonBuilder().setPrettyPrinting().create()
    private val KV_StudioList = "KV_StudioList"
    private val KV_AppKeyEn = "KV_AppKeyEn"
    private val KV_ProvisionModel = "KV_ProvisionModel"
    private val KV_SaveFmPath = "KV_SaveFmPath"

    companion object {
        const val PROVISION_MODEL_DEF = 0
        const val PROVISION_MODEL_FAST = 1
        const val PROVISION_MODEL_AUTO = 2

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
            val type = object : TypeToken<MutableList<StudioListBean>>() {}.type
            gson.fromJson(str, type)
        }
    }

    fun setAppKeyEn(isTestModel: Boolean) {
        kv.encode(KV_AppKeyEn, isTestModel)
    }

    fun isAppKeyEn(): Boolean {
        return kv.decodeBool(KV_AppKeyEn, false)
    }

    fun setProvisionModel(provisionModel: Int) {
        kv.encode(KV_ProvisionModel, provisionModel)
    }

    fun getProvisionModel(): Int {
        return kv.decodeInt(KV_ProvisionModel, 0)
    }

    fun setFmPath(path: String) {
        kv.encode(
            KV_SaveFmPath, if (TextUtils.isEmpty(path)) {
                ""
            } else {
                path
            }
        )
    }

    fun getFmPath(): String {
        return kv.decodeString(KV_SaveFmPath) ?: ""
    }
}