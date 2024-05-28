package com.linkiing.fdsmeshlibdemo.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.recyclerview.widget.LinearLayoutManager
import com.base.mesh.api.log.LOGUtils
import com.base.mesh.api.main.MeshLogin
import com.godox.sdk.api.FDSAddOrRemoveDeviceApi
import com.godox.sdk.api.FDSMeshApi
import com.godox.sdk.api.FDSSearchDevicesApi
import com.godox.sdk.callbacks.FDSBleDevCallBack
import com.godox.sdk.callbacks.FDSFastAddNetWorkCallBack
import com.godox.sdk.model.FDSNodeInfo
import com.godox.sdk.tool.DevicesUtils
import com.linkiing.fdsmeshlibdemo.R
import com.linkiing.fdsmeshlibdemo.adapter.AddDeviceAdapter
import com.linkiing.fdsmeshlibdemo.mmkv.MMKVSp
import com.linkiing.fdsmeshlibdemo.ui.base.BaseActivity
import com.linkiing.fdsmeshlibdemo.utils.ConfigPublishUtils
import com.linkiing.fdsmeshlibdemo.view.dialog.LoadingDialog
import com.telink.ble.mesh.entity.AdvertisingDevice
import kotlinx.android.synthetic.main.activity_add_device.*

class FastAddDeviceActivity : BaseActivity() {
    private lateinit var addDevicesAdapter: AddDeviceAdapter
    private lateinit var loadingDialog: LoadingDialog
    private val handler = Handler(Looper.myLooper()!!)
    private val searchDevices = FDSSearchDevicesApi()
    private val fdsAddOrRemoveDeviceApi = FDSAddOrRemoveDeviceApi(this)
    private var isAllCheck = false
    private var isScanning = true
    private var fastFoundDeviceSize = 0
    private var addDeviceSize = 0
    private val configPublishUtils = ConfigPublishUtils()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_device)

        initView()
        initRecyclerView()
        scanDevices()
        initListener()
    }

    private fun initView() {
        titleBar?.initTitleBar(true, R.drawable.refresh)
        titleBar?.setTitle("搜索设备(Fast)")
        titleBar?.setOnEndImageListener {
            addDevicesAdapter.clearList()
            if (isScanning) {
                stopScan()
            }
            scanDevices()
        }

        loadingDialog = LoadingDialog(this)
    }

    private fun initRecyclerView() {
        addDevicesAdapter = AddDeviceAdapter()
        val manager = LinearLayoutManager(this)
        manager.orientation = LinearLayoutManager.VERTICAL
        recyclerView_devices.layoutManager = manager
        recyclerView_devices.adapter = addDevicesAdapter

        addDevicesAdapter.setIsAllCheckListener {
            setCheck(it)
        }
    }

    private fun scanDevices() {
        isScanning = true

        val filterName = if (MMKVSp.instance.isTestModel()) {
            ""
        } else {
            "GD_LED"
        }
        searchDevices.startScanDevice(this, filterName, 30 * 1000, object : FDSBleDevCallBack {
            @SuppressLint("SetTextI18n")
            override fun onDeviceSearch(advertisingDevice: AdvertisingDevice, type: String) {

                //固件版本 >= 39 才支持Fast模式
                val fv = DevicesUtils.getFirmwareVersion(advertisingDevice.scanRecord)
                if (fv >= 39) {
                    addDevicesAdapter.addDevices(advertisingDevice, type)
                    tv_dev_network_equipment?.text =
                        "${getString(R.string.text_dev_network_equipment)}:${addDevicesAdapter.itemCount}"
                }
            }

            override fun onScanTimeOut() {
                isScanning = false
            }

            /*
             * 开启搜索设备失败。
             */
            override fun onScanFail() {
                isScanning = false
            }
        })
    }

    private fun stopScan() {
        searchDevices.stopScan()
        isScanning = false
    }

    private fun addDevice() {
        stopScan()

        //添加设备到mesh
        val deviceList = addDevicesAdapter.getCheckDevices()
        if (deviceList.isEmpty()) {
            return
        }

        fastFoundDeviceSize = 0
        addDeviceSize = deviceList.size

        loadingDialog.showDialog()
        loadingDialog.updateLoadingMsg("设备配网中...")

        /**
         * Fast配网模式。
         *（注意：固件版本 >= 39 支持）
         */
        fdsAddOrRemoveDeviceApi.deviceFastAddNetWork(deviceList, fdeFastAddNetWorkCallBack)
    }

    /**
     * FDSAddNetWorkCallBack
     */
    private val fdeFastAddNetWorkCallBack = object : FDSFastAddNetWorkCallBack {

        //配网成功
        override fun onInNetworkSuccess(isAllSuccess: Boolean, fdsNodes: MutableList<FDSNodeInfo>) {
            LOGUtils.d("FastAddDeviceActivity onSuccess() size:${fdsNodes.size}")

            loadingDialog.updateLoadingMsg("配网成功!")

            //节点设置默认名称
            if (!MMKVSp.instance.isTestModel()) {
                for (fdsNode in fdsNodes) {
                    FDSMeshApi.instance.renameFDSNodeInfo(fdsNode, "GD_LED_${fdsNode.type}", "")
                }
            }

            addDevicesAdapter.removeItemAtInNetWork(fdsNodes)

            //连接mesh
            MeshLogin.instance.autoConnect(10 * 1000L) {
                if (it) {
                    //配置节点在线状态
                    configPublishUtils.startConfigPublish(fdsNodes, handler) {
                        loadingDialog.dismissDialog()
                    }
                } else {
                    loadingDialog.dismissDialog()
                }
            }
        }

        override fun onDeviceFound(macAddress: String) {
            super.onDeviceFound(macAddress)
            fastFoundDeviceSize++
            loadingDialog.updateLoadingMsg("找到设备:$fastFoundDeviceSize/$addDeviceSize")
        }

        override fun onInNetworkAllFail() {
            loadingDialog.updateLoadingMsg("配网失败!")
            loadingDialog.dismissDialog()
        }
    }

    private fun initListener() {
        iv_check.setOnClickListener {
            val isCheck = !isAllCheck
            setCheck(isCheck)
            addDevicesAdapter.allCheck(isCheck)
        }

        bt_add_device.setOnClickListener {
            addDevice()
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


    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)

        //停止搜索，释放资源
        searchDevices.destroy()

        fdsAddOrRemoveDeviceApi.destroy()
    }
}