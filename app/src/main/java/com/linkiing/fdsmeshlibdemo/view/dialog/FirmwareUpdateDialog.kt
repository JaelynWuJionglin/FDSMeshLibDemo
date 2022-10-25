package com.linkiing.fdsmeshlibdemo.view.dialog

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import com.base.mesh.api.listener.MeshOtaListener
import com.base.mesh.api.main.MeshLogin
import com.godox.sdk.api.FDSMeshApi
import com.godox.sdk.model.FDSNodeInfo
import com.linkiing.fdsmeshlibdemo.R
import com.linkiing.fdsmeshlibdemo.utils.ConstantUtils
import kotlinx.android.synthetic.main.layout_dialog_fm.*
import java.io.IOException
import java.io.InputStream

@SuppressLint("SetTextI18n")
class FirmwareUpdateDialog(private val activity: Activity) :
    BaseFullDialog(activity, R.layout.layout_dialog_fm), MeshOtaListener {
    private var mFirmware = ByteArray(0)
    private var fdsNodeInfo: FDSNodeInfo? = null

    fun showDialog(fdsNodeInfo: FDSNodeInfo) {
        this.fdsNodeInfo = fdsNodeInfo
        if (isShowing) {
            dismiss()
        }
        show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setCanceledOnTouchOutside(false)
        readFirmware()
    }

    override fun onStart() {
        super.onStart()

        tv_progress?.text = "0%"
        progressBar.progress = 0

        if (mFirmware.isNotEmpty() && fdsNodeInfo != null) {
            FDSMeshApi.instance.startOTAWithOtaData(mFirmware,fdsNodeInfo!!,this)
        } else {
            ConstantUtils.toast(activity, "Error！未识别到固件或设备。")
        }
    }

    override fun onStop() {
        super.onStop()

        /**
         * 此方法会断开设备连接并停止自动连接。
         * 所以需要保证之后有调用MeshLogin.instance.autoConnect()启动自动连接mesh网络。
         */
        FDSMeshApi.instance.stopOTA()
        MeshLogin.instance.autoConnect()
    }

    override fun onProgress(progress: Int) {
        activity.runOnUiThread {
            tv_progress?.text = "$progress%"
            progressBar.progress = progress
        }
    }

    override fun onSuccess() {
        activity.runOnUiThread {
            dismiss()
            ConstantUtils.toast(activity, "升级成功！")
        }
    }

    override fun onFailed() {
        activity.runOnUiThread {
            dismiss()
            ConstantUtils.toast(activity, "升级失败！")
        }
    }

    private fun readFirmware() {
        try {
            val inputStream: InputStream = activity.assets.open("LK8620_mesh_GD_v000037_20221024.bin")
            val length = inputStream.available()
            mFirmware = ByteArray(length)
            inputStream.read(mFirmware)
            inputStream.close()
        } catch (e: IOException) {
            e.printStackTrace()
            mFirmware = ByteArray(0)
        }
    }
}