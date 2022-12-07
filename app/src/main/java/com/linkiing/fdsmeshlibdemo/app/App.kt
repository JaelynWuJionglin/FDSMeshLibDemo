package com.linkiing.fdsmeshlibdemo.app

import com.base.mesh.api.main.MeshConfigure
import com.godox.sdk.MeshApp
import com.godox.sdk.api.FDSMeshApi
import com.linkiing.fdsmeshlibdemo.mmkv.MMKVSp
import com.linkiing.fdsmeshlibdemo.utils.BleUtils
import com.telink.ble.mesh.core.networking.ExtendBearerMode
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

        //MMKV
        MMKVSp.instance.init(this)

        //保存SDK Crash日志
        CrashLogUtil.instance.init(this)

        //输出和保存SDK日志
        LOGUtils.initSaveLog(this,true,true)

        //appId认证
        FDSMeshApi.instance.setWithAppId(appId)

        //设置mesh配置信息
        setMeshConfigure()

        //BleUtils 初始化
        BleUtils.instance.init(this)
    }

    private fun setMeshConfigure(){
        val meshConfigure = MeshConfigure()

        /*
         * 配网过程中连接失败重试次数（建议>=3）
         * 注意：太小的重试次数会影响配网稳定性
         */
        meshConfigure.provisionMaxConnectRetry = 4

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


        /*
         * 设置Mesh发送数据包承载模式（需固件支持）
         * NONE:默认都不使用长包
         * GATT:直连节点长包
         * GATT_ADV:全部长包
         */
        FDSMeshApi.instance.resetExtendBearerMode(ExtendBearerMode.GATT_ADV)
    }
}