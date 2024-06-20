package com.linkiing.fdsmeshlibdemo.utils

import android.os.Handler
import com.base.mesh.api.listener.ConfigNodePublishStateListener
import com.base.mesh.api.log.LOGUtils
import com.base.mesh.api.main.MeshLogin
import com.godox.sdk.api.FDSMeshApi
import com.godox.sdk.model.FDSNodeInfo
import java.util.concurrent.CopyOnWriteArrayList

class ConfigPublishUtils : ConfigNodePublishStateListener {
    private val tag = "ConfigPublishUtils"
    private val publishNodeList = CopyOnWriteArrayList<SetPublishNodeInfo>()
    private var handler: Handler? = null
    private var listenerComplete: (Boolean, Int, Int, Int) -> Unit = { _, _, _, _ -> }
    private var allNumber = 0
    private var successNumber = 0
    private var failNumber = 0
    private var nowPublishNodeInfo: SetPublishNodeInfo? = null

    /**
     * 批量配置设备在线状态
     */
    fun startConfigPublish(
        fdsNodes: MutableList<FDSNodeInfo>,
        handler: Handler?,
        listenerComplete: (Boolean, Int, Int, Int) -> Unit
    ) {
        this.handler = handler
        this.listenerComplete = listenerComplete
        this.successNumber = 0
        this.failNumber = 0
        this.publishNodeList.clear()

        //连接mesh
        MeshLogin.instance.autoConnect(15 * 1000L) {
            if (it) {
                for (fdsNode in fdsNodes) {
                    if (fdsNode.firmwareVersion >= 0x49) {
                        FDSMeshApi.instance.setFDSNodePublishModel(true, fdsNode)
                    } else {
                        publishNodeList.add(SetPublishNodeInfo(fdsNode))
                    }
                }
                this.allNumber = publishNodeList.size

                if (publishNodeList.isEmpty()) {
                    listenerComplete(true, allNumber, successNumber, failNumber)
                    return@autoConnect
                }

                handler?.removeCallbacks(nextConfigRunnable)
                handler?.postDelayed(nextConfigRunnable, 500)
            } else {
                listenerComplete(true, allNumber, successNumber, failNumber)
            }
        }
    }

    private fun nextConfigPublish() {
        if (!MeshLogin.instance.isLogin()) {
            //mesh连接断开，重新连接
            MeshLogin.instance.autoConnect(15 * 1000) {
                if (it) {
                    nextConfigPublish()
                } else {
                    LOGUtils.e("$tag nextConfigPublish() Error! isLogin false.")

                    //连接失败，配置在线状态失败
                    listenerComplete(true, allNumber, successNumber, failNumber)
                }
            }
            return
        }
        if (publishNodeList.isEmpty()) {
            listenerComplete(true, allNumber, successNumber, failNumber)
        } else {
            if (publishNodeList[0] != null) {
                nowPublishNodeInfo = publishNodeList[0]
                val isOk = FDSMeshApi.instance.configFDSNodePublishState(
                    true,
                    nowPublishNodeInfo!!.fdsNodeInfo,
                    this
                )
                LOGUtils.d(
                    "$tag nextConfigPublish() =====> " +
                            "macAddress:${nowPublishNodeInfo!!.fdsNodeInfo.macAddress} " +
                            "meshAddress:${nowPublishNodeInfo!!.fdsNodeInfo.meshAddress} " +
                            "isOk:$isOk"
                )

                publishNodeList.removeAt(0)

                if (!isOk) {
                    handler?.removeCallbacks(nextConfigRunnable)
                    handler?.postDelayed(nextConfigRunnable, 600)
                }
            } else {
                publishNodeList.removeAt(0)
                nextConfigPublish()
            }
        }
    }

    private val nextConfigRunnable = Runnable {
        LOGUtils.d("$tag nextConfigRunnable =====>")
        nextConfigPublish()
    }

    override fun onComplete(success: Boolean, meshAddress: Int) {
        LOGUtils.d("$tag onComplete() =====> success:$success  meshAddress:$meshAddress")
        if (!success && nowPublishNodeInfo != null) {
            nowPublishNodeInfo!!.retryIndex++
            if (nowPublishNodeInfo!!.retryIndex < 3) {
                publishNodeList.add(nowPublishNodeInfo)
            } else {
                //失败一个
                failNumber++
            }
        }
        if (success) {
            successNumber++
        }

        listenerComplete(false, allNumber, successNumber, failNumber)

        handler?.removeCallbacks(nextConfigRunnable)
        handler?.postDelayed(nextConfigRunnable, 300)
    }

    data class SetPublishNodeInfo constructor(val fdsNodeInfo: FDSNodeInfo) {
        var retryIndex = 0
    }
}