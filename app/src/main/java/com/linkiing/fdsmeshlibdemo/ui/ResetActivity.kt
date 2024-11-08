package com.linkiing.fdsmeshlibdemo.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.godox.sdk.api.FDSResetDeviceApi
import com.godox.sdk.api.FDSSearchDevicesApi
import com.godox.sdk.callbacks.FDSBleDevCallBack
import com.godox.sdk.tool.DevicesUtils
import com.linkiing.fdsmeshlibdemo.R
import com.linkiing.fdsmeshlibdemo.adapter.ResetDeviceAdapter
import com.linkiing.fdsmeshlibdemo.mmkv.MMKVSp
import com.linkiing.fdsmeshlibdemo.ui.base.BaseActivity
import com.linkiing.fdsmeshlibdemo.view.dialog.LoadingDialog
import com.telink.ble.mesh.entity.AdvertisingDevice
import kotlinx.android.synthetic.main.activity_reset.*

class ResetActivity : BaseActivity() {
    private val handler = Handler(Looper.myLooper()!!)
    private lateinit var loadingDialog: LoadingDialog
    private lateinit var resetDeviceAdapter: ResetDeviceAdapter
    private val searchDevices = FDSSearchDevicesApi()
    private var isAllCheck = false
    private var isScanning = true
    private var resetSize = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reset)

        initView()
        initRecyclerView()
        scanDevices()
        initListener()
    }

    private fun initView() {
        titleBar?.initTitleBar(true, R.drawable.refresh)
        titleBar?.setOnEndImageListener {
            resetDeviceAdapter.clearList()
            if (isScanning) {
                stopScan()
            }
            scanDevices()
        }

        loadingDialog = LoadingDialog(this)

    }

    private fun initListener() {
        iv_check.setOnClickListener {
            val isCheck = !isAllCheck
            setCheck(isCheck)
            resetDeviceAdapter.allCheck(isCheck)
        }

        bt_reset_device?.setOnClickListener {
            resetDevice()
        }
    }

    private fun initRecyclerView() {
        resetDeviceAdapter = ResetDeviceAdapter()
        val manager = LinearLayoutManager(this)
        manager.orientation = LinearLayoutManager.VERTICAL
        recyclerView_devices.layoutManager = manager
        recyclerView_devices.adapter = resetDeviceAdapter

        resetDeviceAdapter.setIsAllCheckListener {
            setCheck(it)
        }
    }

    private fun scanDevices() {
        isScanning = true

        progressBar.visibility = View.VISIBLE

        val filterName = if (MMKVSp.instance.isTestModel()) {
            ""
        } else {
            "GD_LED"
        }

        /**
         * 搜索已入网的设备
         */
        searchDevices.startScanProvisionedDevice(
            this,
            filterName,
            60 * 1000,
            object : FDSBleDevCallBack {
                @SuppressLint("SetTextI18n")
                override fun onDeviceSearch(
                    advertisingDevice: AdvertisingDevice,
                    deviceName: String,//设备名(广播中解析的,有时有些手机从“advertisingDevice.device.name”获取的广播名可能为空或null)
                    type: String,
                    firmwareVersion: Int
                ) {
                    if (firmwareVersion >= 0x48) {
                        resetDeviceAdapter.addDevices(advertisingDevice, deviceName, type, firmwareVersion)
                        tv_dev_network_equipment?.text =
                            "${getString(R.string.text_dev_number)}:${resetDeviceAdapter.itemCount}"
                    }
                }

                override fun onScanTimeOut() {
                    isScanning = false
                    progressBar.visibility = View.GONE
                }

                /*
                 * 开启搜索设备失败。
                 */
                override fun onScanFail() {
                    isScanning = false
                    progressBar.visibility = View.GONE
                }
            })
    }

    private fun stopScan() {
        searchDevices.stopScan()
        isScanning = false
        progressBar.visibility = View.GONE
    }

    private fun resetDevice() {
        stopScan()

        val list = resetDeviceAdapter.getCheckDevices()
        if (list.isEmpty()) {
            return
        }

        resetSize = list.size

        loadingDialog.showDialog()
        loadingDialog.updateLoadingMsg("重置中...0/$resetSize")

        /**
         * 重置设备入网状态
         * (注：1，本质是通过蓝牙Advertise的方式，给特定设备发特定的广播。
         *     2，使用这个方法注意程序是否已经开启了其他Advertise，避免广播的占用导致调用方法的失败。
         *     3，此方式适用于丢失了mesh的网络数据，又需要重置设备的时候使用。
         *     4，此方式禁止用于恶意破坏其他的mesh网络。)
         *  @param deviceList 需要重置的设备列表
         *  @param advertiseTime 单个设备重置广播的时长
         *  @param fixedKey 密钥，和固件端协定
         */
        val fixedKey = resources.getInteger(R.integer.reset_private_key_fixed)
        FDSResetDeviceApi.instance.startResetAdvertise(list, 2000L, fixedKey) { isOk, number ->
            runOnUiThread {
                loadingDialog.updateLoadingMsg("重置中...$number/$resetSize")

                if (isOk) {
                    resetDeviceAdapter.clearList()
                    scanDevices()
                    loadingDialog.dismissDialog()
                }
            }
        }
    }

    private fun setCheck(isCheck: Boolean) {
        isAllCheck = isCheck
        if (isAllCheck) {
            iv_check.setBackgroundResource(R.drawable.checked_image_on)
        } else {
            iv_check.setBackgroundResource(R.drawable.checked_image_off)
        }
    }

    override fun finish() {
        super.finish()

        //停止搜索，释放资源
        searchDevices.destroy()
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
    }
}