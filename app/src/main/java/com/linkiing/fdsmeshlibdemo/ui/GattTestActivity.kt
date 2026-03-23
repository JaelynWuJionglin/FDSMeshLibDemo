package com.linkiing.fdsmeshlibdemo.ui

import android.bluetooth.BluetoothGatt
import android.os.Bundle
import com.base.mesh.api.listener.GattNotifyListener
import com.base.mesh.api.listener.MeshLoginListener
import com.base.mesh.api.log.LOGUtils
import com.base.mesh.api.main.MeshLogin
import com.base.mesh.api.utils.ByteUtils
import com.godox.sdk.api.FDSMeshApi
import com.linkiing.fdsmeshlibdemo.databinding.ActivityGattTestBinding
import com.linkiing.fdsmeshlibdemo.ui.base.BaseActivity
import com.linkiing.fdsmeshlibdemo.utils.ConstantUtils
import com.telink.ble.mesh.core.ble.GattRequest
import java.util.UUID

class GattTestActivity : BaseActivity<ActivityGattTestBinding>(), GattNotifyListener,
    MeshLoginListener {
    private val tag = "GattTestActivity"
    private var sendTestNumber = 0
    private var sendTestTimeMi = 0L

    //    private val MCU_UUID_SERVER = "0000fff0-0000-1000-8000-00805f9b34fb"
    //    private val MCU_UUID_CHAR = "0000fff3-0000-1000-8000-00805f9b34fb"

    private val SERVICE_TEST2 = "0000fff0-0000-1000-8000-00805f9b34fb" // 服务
    private val SEND_UUID_TEST2 = "0000fff2-0000-1000-8000-00805f9b34fb" // 通道
    private val NOTIF_UUID_TEST2 = "0000fff1-0000-1000-8000-00805f9b34fb" // 通道

    override fun initBind(): ActivityGattTestBinding {
        return ActivityGattTestBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initView()

        //mesh连接断开监听
        MeshLogin.instance.addLoginListener(this)

        //GattNotify监听
        FDSMeshApi.instance.addGattNotifyListener(this)

        //开启Notify，设备连接之后。
        if (MeshLogin.instance.isLogin()) {
            enableNotify()
        }
    }

    private fun initView() {
        binding.titleBar.initTitleBar("MeshGattTest", "")

        binding.btSend.setOnClickListener {
            val sendStr = binding.etSend.text?.toString()?.trim() ?: ""
            if (sendStr.isEmpty()) {
                ConstantUtils.toast(this, "请输入数据!")
                return@setOnClickListener
            }
            if (sendStr.length < 2 || sendStr.length % 2 != 0) {
                ConstantUtils.toast(this, "输入错误!")
                return@setOnClickListener
            }

            val sendBytes = ByteUtils.hexStringToBytes(sendStr)
            if (sendBytes.isEmpty()) {
                ConstantUtils.toast(this, "请输入16进制字符串数据!")
                return@setOnClickListener
            }

            val gattRequest = GattRequest.newInstance()
            gattRequest.serviceUUID = UUID.fromString(SERVICE_TEST2)
            gattRequest.characteristicUUID = UUID.fromString(SEND_UUID_TEST2)
            gattRequest.data = sendBytes
            gattRequest.type = GattRequest.RequestType.WRITE_NO_RESPONSE
            gattRequest.callback = sendCallBack
            FDSMeshApi.instance.sendGattRequest(gattRequest)
        }

        binding.btCheckConnect.setOnClickListener {
            checkConnectDevice()
        }

        binding.btSendTest.setOnClickListener {
            sendTestNumber = 0
            sendTest()
        }
    }

    private fun sendTest() {
        sendTestTimeMi = System.currentTimeMillis()

        val gattRequest = GattRequest.newInstance()
        gattRequest.serviceUUID = UUID.fromString(SERVICE_TEST2)
        gattRequest.characteristicUUID = UUID.fromString(SEND_UUID_TEST2)
        gattRequest.data = "SWSON".toByteArray(Charsets.UTF_8)
        gattRequest.type = GattRequest.RequestType.WRITE_NO_RESPONSE
        gattRequest.callback = sendCallBack
        FDSMeshApi.instance.sendGattRequest(gattRequest)
        LOGUtils.i("$tag sendTest success!")
    }

    //开启Notify
    private fun enableNotify() {
        /*
         * 此方法会自动去重，已经enable过的不会再重复enable。需要开启多个不同通道Notify，可分别多次调用。
         */
        FDSMeshApi.instance.enableNotify(SERVICE_TEST2, NOTIF_UUID_TEST2)
    }

    /**
     * 切换BLE直连设备。
     */
    private fun checkConnectDevice() {
        MeshLogin.instance.checkConnectDevice("A4:C1:38:C5:8C:F5", 30 * 1000L) {
            if (it) {
                LOGUtils.d("$tag 连接成功!")
            } else {
                LOGUtils.d("$tag 连接超时!")
            }
        }
    }

    //GattRequest发送回调
    private val sendCallBack = object : GattRequest.Callback {
        override fun success(request: GattRequest?, obj: Any?) {
            LOGUtils.d("$tag sendCallBack success")
        }

        override fun error(
            request: GattRequest?,
            errorMsg: String?,
        ) {
            LOGUtils.e("$tag sendCallBack error $errorMsg")
        }

        override fun timeout(request: GattRequest?): Boolean {
            LOGUtils.e("$tag sendCallBack timeout")
            return false
        }
    }

    override fun onMeshConnected() {
        super.onMeshConnected()
        enableNotify()
    }

    override fun onNotify(
        macAddress: String,
        serviceUUID: UUID,
        charUUID: UUID,
        data: ByteArray,
    ) {
        LOGUtils.d(
            "$tag onNotify macAddress:$macAddress " +
                    "serviceUUID:$serviceUUID " +
                    "charUUID:$charUUID " +
                    "data:${ByteUtils.toHexString(data)}"
        )
        LOGUtils.v("$tag onNotify sendTest 间隔ms:${System.currentTimeMillis() - sendTestTimeMi}")

        if (charUUID == UUID.fromString(NOTIF_UUID_TEST2)) {
            if (sendTestNumber < 10) {
                sendTestNumber++
                sendTest()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        MeshLogin.instance.removeLoginListener(this)
        FDSMeshApi.instance.removeGattNotifyListener(this)
    }
}