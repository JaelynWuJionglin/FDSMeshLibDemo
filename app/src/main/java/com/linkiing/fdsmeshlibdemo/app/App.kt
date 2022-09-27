package com.linkiing.fdsmeshlibdemo.app

import com.godox.sdk.MeshApp
import com.godox.sdk.api.FDSMeshApi
import com.linkiing.fdsmeshlibdemo.utils.BleUtils
import com.telink.ble.mesh.util.CrashLogUtil
import com.telink.ble.mesh.util.LOGUtils

class App: MeshApp() {
    private val appId = "185BD3FB2532A7CE6BF4B2C15B8C27F06E0554779140BF726A929128FD0514BE"

    companion object{
        private lateinit var mThis: MeshApp
        fun getInstance(): MeshApp {
            return mThis
        }
    }

    override fun onCreate() {
        super.onCreate()
        mThis = this

        //保存SDK Crash日志
        CrashLogUtil.instance.init(this)

        //输出和保存SDK日志
        LOGUtils.initSaveLog(this,true,true)

        //appId认证
        FDSMeshApi.instance.setWithAppId(appId)

        //BleUtils 初始化
        BleUtils.instance.init(this)
    }
}