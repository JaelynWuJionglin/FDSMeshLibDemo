package com.linkiing.fdsmeshlibdemo.app

import com.base.mesh.api.log.LOGUtils
import com.base.mesh.api.main.MeshConfigure
import com.godox.sdk.MeshApp
import com.godox.sdk.api.FDSMeshApi
import com.linkiing.fdsmeshlibdemo.mmkv.MMKVSp
import com.linkiing.fdsmeshlibdemo.utils.BleUtils
import com.telink.ble.mesh.core.networking.ExtendBearerMode

class App : MeshApp() {
    private val appId = "185BD3FB2532A7CE6BF4B2C15B8C27F06E0554779140BF726A929128FD0514BE"

    companion object {
        private lateinit var mThis: App
        fun getInstance(): App {
            return mThis
        }
    }

    override fun onCreate() {
        super.onCreate()
        mThis = this

        //MMKV
        MMKVSp.instance.init(this)

        //输出和保存SDK日志
        LOGUtils.init(this, true, true)

        //appId认证
        FDSMeshApi.instance.setWithAppId(appId)

        //BleUtils 初始化
        BleUtils.instance.init(this)

        /*
        * 配置mesh参数
        */
        if (!MMKVSp.instance.isTestModel()) {
            setMeshConfigure()
        }

        /**
         * 初始化mesh数据
         *（会获取Android_ID等设备信息，生成唯一ID，故首次启动app，需要在用户同意隐私政策后调用）
         */
        initMeshData()
    }

    fun setMeshConfigure() {
        val meshConfigure = MeshConfigure()

        /*
         * 设备配网连接设备超时时长。(秒)
         * 注意：太短的时长会影响配网成功率，建议 >= 45s
         */
        meshConfigure.provisionMaxConnectOutTime = 45

        /*
         * 配网连接设备失败，等待重试下一次的等待时间（建议500ms - 3000ms）
         * 此参数只在配网过程中，连接设备失败的时候生效。
         * 注意：会影响配网成功率
         */
        meshConfigure.provisionDisconnectDelayed = 500L

        /*
         * 是否使用默认绑定的组网方式，可加快配网速度
         * 需要固件支持。
         */
        meshConfigure.isPrivateMode = true


        FDSMeshApi.instance.setMeshConfigure(meshConfigure)
    }

    fun defMeshConfigure() {
        FDSMeshApi.instance.setMeshConfigure(MeshConfigure())
        FDSMeshApi.instance.resetExtendBearerMode(ExtendBearerMode.NONE)
    }
}