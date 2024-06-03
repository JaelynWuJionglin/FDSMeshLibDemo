package com.linkiing.fdsmeshlibdemo.utils

import android.os.Handler
import com.base.mesh.api.listener.ConfigNodePublishStateListener
import com.base.mesh.api.log.LOGUtils
import com.base.mesh.api.main.MeshLogin
import com.godox.sdk.api.FDSMeshApi
import com.godox.sdk.model.FDSNodeInfo
import java.util.concurrent.CopyOnWriteArrayList

class ConfigPublishUtils : ConfigNodePublishStateListener {
    private val publishNodeList = CopyOnWriteArrayList<FDSNodeInfo>()
    private var handler: Handler? = null
    private var listenerComplete: (Boolean, Int) -> Unit = { _, _ -> }
    private var successNumber = 0

    /**
     * 批量配置设备在线状态
     */
    fun startConfigPublish(
        fdsNodes: MutableList<FDSNodeInfo>,
        handler: Handler?,
        listenerComplete: (Boolean, Int) -> Unit
    ) {
        this.handler = handler
        this.listenerComplete = listenerComplete
        this.successNumber = 0

        publishNodeList.clear()
        for (fdsNode in fdsNodes) {
            publishNodeList.add(fdsNode)
        }
        nextConfigPublish()
    }

    private fun nextConfigPublish() {
        if (!MeshLogin.instance.isLogin()) {
            LOGUtils.e("nextConfigPublish() Error! isLogin false.")
            listenerComplete(false, successNumber)
            return
        }
        if (publishNodeList.isEmpty()) {
            listenerComplete(true, successNumber)
        } else {
            if (publishNodeList[0] != null) {
                val isOk = FDSMeshApi.instance.configFDSNodePublishState(
                    true,
                    publishNodeList[0],
                    this
                )
                LOGUtils.d(
                    "nextConfigPublish() =====> " +
                            "macAddress:${publishNodeList[0]!!.macAddress} " +
                            "meshAddress:${publishNodeList[0]!!.meshAddress} " +
                            "isOk:$isOk"
                )

                publishNodeList.removeAt(0)

                if (!isOk) {
                    handler?.postDelayed({
                        nextConfigPublish()
                    }, 500)
                }
                successNumber ++
                listenerComplete(false, successNumber)
            } else {
                publishNodeList.removeAt(0)
                nextConfigPublish()
            }
        }
    }

    override fun onComplete(success: Boolean) {
        LOGUtils.d("onComplete() =====> success:$success")
        nextConfigPublish()
    }
}