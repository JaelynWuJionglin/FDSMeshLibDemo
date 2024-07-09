package com.linkiing.fdsmeshlibdemo.view.dialog

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import com.base.mesh.api.listener.MeshOtaListener
import com.base.mesh.api.log.LOGUtils
import com.base.mesh.api.main.MeshLogin
import com.godox.sdk.api.FDSMeshApi
import com.godox.sdk.model.FDSNodeInfo
import com.linkiing.fdsmeshlibdemo.R
import com.linkiing.fdsmeshlibdemo.utils.ConstantUtils
import kotlinx.android.synthetic.main.layout_dialog_fm.*
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream

@SuppressLint("SetTextI18n")
class MeshOtaDialog(private val activity: Activity, private val isMcuUpgrade: Boolean) :
    BaseFullDialog(activity, R.layout.layout_dialog_fm), MeshOtaListener {
    private var mFirmware = ByteArray(0)
    private var fdsNodeInfo: FDSNodeInfo? = null
    private var isPa = false
    private var listener: (Boolean) -> Unit = {}

    fun showDialog(fdsNodeInfo: FDSNodeInfo, path: String) {
        this.fdsNodeInfo = fdsNodeInfo
        if (isShowing) {
            dismiss()
        }
        readFirmwarePath(path)
        //readFirmwareAssets(path)
        show()
    }

    fun setListener(listener: (Boolean) -> Unit){
        this.listener = listener
    }

    fun setOldFirmwareInfo(isPa: Boolean) {
        this.isPa = isPa
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setCanceledOnTouchOutside(false)
    }

    override fun onStart() {
        super.onStart()

        tv_progress?.text = "0%"
        progressBar.progress = 0

        if (mFirmware.isNotEmpty() && fdsNodeInfo != null) {
            if (isMcuUpgrade) {

                /**
                 * 开启MCU OTA升级
                 * @param otaData 固件数据
                 * @param version 固件版本
                 * @param fdsNodeInfo 节点
                 * @param listener MCU OTA升级回调
                 * @return  true表示开启成功，false表示开启失败
                 */
                FDSMeshApi.instance.startMcuOTAWithOtaData(mFirmware, 0, fdsNodeInfo!!, this)

            } else {

                /**
                 * 开启OTA升级
                 * @param otaData 固件数据
                 * @param fdsNodeInfo 节点
                 * @param listener OTA升级回调
                 * @return true表示开启成功，false表示开启失败
                 */
                FDSMeshApi.instance.startOTAWithOtaData(mFirmware, fdsNodeInfo!!, this)
            }
        } else {
            ConstantUtils.toast(activity, "Error！未识别到固件或设备。")
        }
    }

    override fun onStop() {
        super.onStop()

        if (isMcuUpgrade) {
            FDSMeshApi.instance.stopMcuOTA()
        } else {
            FDSMeshApi.instance.stopOTA()
        }

        /**
         * 升级过程会停止mesh网络。
         * 如果需要升级完成自动重连mesh,需要调用MeshLogin.instance.autoConnect()。
         */
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
            listener(true)
        }
    }

    override fun onFailed(errorCode: Int) {
        activity.runOnUiThread {
            dismiss()
            ConstantUtils.toast(activity, "升级失败！errorCode：$errorCode")
            listener(false)
        }
    }

    // 从sdcard获取固件
    private fun readFirmwarePath(path: String) {
        try {
            val inputStream: InputStream = FileInputStream(path)
            val length = inputStream.available()
            mFirmware = ByteArray(length)
            inputStream.read(mFirmware)
            inputStream.close()
        } catch (e: IOException) {
            e.printStackTrace()
            mFirmware = ByteArray(0)
            dismiss()
        }
    }

    //从assets获取固件
    private fun readFirmwareAssets(path: String) {
        try {
            val inputStream: InputStream = activity.assets.open(path)
            val length = inputStream.available()
            mFirmware = ByteArray(length)
            inputStream.read(mFirmware)
            inputStream.close()
        } catch (e: IOException) {
            e.printStackTrace()
            mFirmware = ByteArray(0)
            dismiss()
        }
    }
}