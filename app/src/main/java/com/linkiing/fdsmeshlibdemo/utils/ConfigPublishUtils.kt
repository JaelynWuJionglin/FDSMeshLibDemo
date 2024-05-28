package com.linkiing.fdsmeshlibdemo.utils

import android.os.Handler
import com.base.mesh.api.listener.ConfigNodePublishStateListener
import com.base.mesh.api.log.LOGUtils
import com.base.mesh.api.main.MeshLogin
import com.godox.sdk.api.FDSMeshApi
import com.godox.sdk.model.FDSNodeInfo
import com.linkiing.fdsmeshlibdemo.bean.RetryFDSNodeInfo
import java.util.concurrent.CopyOnWriteArrayList

class ConfigPublishUtils : ConfigNodePublishStateListener {
    private var retryFDSNodeInfo: RetryFDSNodeInfo? = null
    private val publishNodeList = CopyOnWriteArrayList<RetryFDSNodeInfo>()
    private var handler: Handler? = null
    private var listenerComplete: (Boolean) -> Unit = {}

    /**
     * 批量配置设备在线状态
     */
    fun startConfigPublish(
        fdsNodes: MutableList<FDSNodeInfo>,
        handler: Handler?,
        listenerComplete: (Boolean) -> Unit
    ) {
        this.handler = handler
        this.listenerComplete = listenerComplete

        publishNodeList.clear()
        for (fdsNode in fdsNodes) {
            publishNodeList.add(RetryFDSNodeInfo(fdsNode, 2))
        }
        retryFDSNodeInfo = null
        nextConfigPublish()
    }

    private fun nextConfigPublish() {
        if (!MeshLogin.instance.isLogin()) {
            LOGUtils.e("nextConfigPublish() Error! isLogin false.")
            listenerComplete(false)
            return
        }
        if (publishNodeList.isEmpty()) {
            listenerComplete(true)
        } else {
            retryFDSNodeInfo = publishNodeList[0]
            if (retryFDSNodeInfo != null) {
                val isOk = FDSMeshApi.instance.configFDSNodePublishState(
                    true,
                    retryFDSNodeInfo!!.fdsNodeInfo,
                    this
                )
                LOGUtils.d(
                    "nextConfigPublish() =====> " +
                            "macAddress:${retryFDSNodeInfo!!.fdsNodeInfo.macAddress} " +
                            "meshAddress:${retryFDSNodeInfo!!.fdsNodeInfo.meshAddress} " +
                            "retryIndex:${retryFDSNodeInfo!!.retryIndex} " +
                            "isOk:$isOk"
                )

                publishNodeList.removeAt(0)

                if (!isOk) {
                    retryConfigPublish()
                }
            } else {
                publishNodeList.removeAt(0)
                nextConfigPublish()
            }
        }
    }

    private fun retryConfigPublish() {
        if (retryFDSNodeInfo != null) {
            retryFDSNodeInfo!!.retryIndex--
            //重试总共两次
            if (retryFDSNodeInfo!!.retryIndex > 0) {
                publishNodeList.add(retryFDSNodeInfo)
            }
        }
        handler?.postDelayed({
            nextConfigPublish()
        }, 500)
    }

    override fun onComplete(success: Boolean) {
        if (!success) {
            retryConfigPublish()
        } else {
            nextConfigPublish()
        }
    }
}