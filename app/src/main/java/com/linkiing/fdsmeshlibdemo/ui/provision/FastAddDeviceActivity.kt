package com.linkiing.fdsmeshlibdemo.ui.provision

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.base.mesh.api.log.LOGUtils
import com.base.mesh.api.utils.ByteUtils
import com.godox.sdk.api.FDSAddOrRemoveDeviceApi
import com.godox.sdk.api.FDSMeshApi
import com.godox.sdk.api.FDSSearchDevicesApi
import com.godox.sdk.api.bean.RenameBean
import com.godox.sdk.callbacks.FDSBleDevCallBack
import com.godox.sdk.callbacks.FDSFastAddNetWorkCallBack
import com.godox.sdk.model.FDSNodeInfo
import com.linkiing.fdsmeshlibdemo.R
import com.linkiing.fdsmeshlibdemo.adapter.AddDeviceAdapter
import com.linkiing.fdsmeshlibdemo.mmkv.MMKVSp
import com.linkiing.fdsmeshlibdemo.ui.base.BaseActivity
import com.linkiing.fdsmeshlibdemo.utils.ConfigPublishUtils
import com.linkiing.fdsmeshlibdemo.utils.ConstantUtils
import com.linkiing.fdsmeshlibdemo.view.dialog.LoadingDialog
import com.telink.ble.mesh.entity.AdvertisingDevice
import kotlinx.android.synthetic.main.activity_add_device.*
import java.util.Locale

class FastAddDeviceActivity : BaseActivity() {
    private lateinit var addDevicesAdapter: AddDeviceAdapter
    private lateinit var loadingDialog: LoadingDialog
    private val handler = Handler(Looper.getMainLooper())
    private val searchDevices = FDSSearchDevicesApi()
    private val fdsAddOrRemoveDeviceApi = FDSAddOrRemoveDeviceApi(this)
    private var isAllCheck = false
    private var isScanning = true
    private var deviceSetSusNumber = 0
    private var deviceSetFailNumber = 0
    private var addDeviceSize = 0
    private var index = 0
    private val configPublishUtils = ConfigPublishUtils()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_device)

        initView()
        initRecyclerView()
        scanDevices()
        initListener()
    }

    @SuppressLint("SetTextI18n")
    private fun initView() {
        index = intent.getIntExtra("index", 0)
        LOGUtils.d("FastAddDeviceActivity =============> index:$index")
        if (index == 0) {
            finish()
        }

        titleBar?.initTitleBar(true, R.drawable.refresh)
        titleBar?.setTitle("搜索设备(Fast)")
        titleBar?.setOnEndImageListener {
            addDevicesAdapter.clearList()
            tv_dev_network_equipment?.text = "${getString(R.string.text_dev_number)}:0/0"
            if (isScanning) {
                stopScan()
            }
            scanDevices()
        }

        loadingDialog = LoadingDialog(this)
    }

    @SuppressLint("SetTextI18n")
    private fun initRecyclerView() {
        addDevicesAdapter = AddDeviceAdapter()
        val manager = LinearLayoutManager(this)
        manager.orientation = LinearLayoutManager.VERTICAL
        recyclerView_devices.layoutManager = manager
        recyclerView_devices.adapter = addDevicesAdapter

        addDevicesAdapter.setIsAllCheckListener {
            setCheck(it)
            tv_dev_network_equipment?.text =
                "${getString(R.string.text_dev_number)}:${addDevicesAdapter.itemCount}/${addDevicesAdapter.getCheckDevices().size}"
        }
    }

    private fun scanDevices() {
        isScanning = true

        progressBar?.visibility = View.VISIBLE

        val filterName = if (MMKVSp.instance.isTestModel()) {
            ""
        } else {
            "GD_LED"
        }
        searchDevices.startScanDevice(this, filterName, 10 * 60 * 1000, object : FDSBleDevCallBack {

            @SuppressLint("SetTextI18n")
            override fun onDeviceSearch(
                advertisingDevice: AdvertisingDevice,
                deviceName: String,//设备名(广播中解析的,有时有些手机从“advertisingDevice.device.name”获取的广播名可能为空或null)
                type: String,
                firmwareVersion: Int
            ) {
                //固件版本 >= 0x55
                if (firmwareVersion >= 0x55 || MMKVSp.instance.isTestModel()) {
                    addDevicesAdapter.addDevices(advertisingDevice, deviceName, type, firmwareVersion)
                    tv_dev_network_equipment?.text =
                        "${getString(R.string.text_dev_number)}:${addDevicesAdapter.itemCount}/${addDevicesAdapter.getCheckDevices().size}"
                }
            }

            override fun onScanTimeOut() {
                isScanning = false
                progressBar?.visibility = View.GONE
            }

            /*
             * 开启搜索设备失败。
             */
            override fun onScanFail() {
                isScanning = false
                progressBar?.visibility = View.GONE
            }
        })
    }

    private fun stopScan() {
        searchDevices.stopScan()
        isScanning = false
        progressBar?.visibility = View.GONE
    }

    private fun addDevice() {
        stopScan()

        //添加设备到mesh
        val deviceList = addDevicesAdapter.getCheckDevices()
        if (deviceList.isEmpty()) {
            return
        }

        deviceSetSusNumber = 0
        deviceSetFailNumber = 0
        addDeviceSize = deviceList.size


        /**
         * Fast配网模式。
         *（注意：固件版本 >= 55 支持
         */
        val isFastStart =
            fdsAddOrRemoveDeviceApi.deviceFastAddNetWork(deviceList, fdeFastAddNetWorkCallBack)
        if (isFastStart) {
            loadingDialog.showDialog()
            loadingDialog.updateLoadingMsg("设备配网中...")
        }
    }

    /**
     * FDSAddNetWorkCallBack
     */
    private val fdeFastAddNetWorkCallBack = object : FDSFastAddNetWorkCallBack {

        //配网完成
        @SuppressLint("SetTextI18n")
        override fun onInNetworkComplete(isSuccess: Boolean, resultList: MutableList<FDSNodeInfo>) {
            LOGUtils.d("FastAddDeviceActivity onSuccess() size:${resultList.size}")

            if (isSuccess) {
                loadingDialog.updateLoadingMsg("配网完成!")
                if (resultList.isEmpty()) {
                    loadingDialog.dismissDialog()
                    return
                }

                //节点设置默认名称
                if (!MMKVSp.instance.isTestModel()) {
                    Thread {
                        val renameList = mutableListOf<RenameBean>()
                        for (fdsNode in resultList) {
                            renameList.add(RenameBean(fdsNode.meshAddress,"GD_LED_${fdsNode.type}"))
                        }
                        FDSMeshApi.instance.renameFDSNodeInfo(renameList)
                    }.start()
                }

                addDevicesAdapter.removeItemAtInNetWork(resultList)

                //配置节点在线状态
                configPublishUtils.startConfigPublish(
                    resultList,
                    handler
                ) { isComplete, allNumber, susNumber, failNumber ->
                    runOnUiThread {
                        loadingDialog.updateLoadingMsg("配置在线:$susNumber/$allNumber 失败:$failNumber")

                        if (isComplete) {
                            ConstantUtils.saveJson(index)
                            loadingDialog.dismissDialog()
                            tv_dev_network_equipment?.text =
                                "${getString(R.string.text_dev_number)}:${addDevicesAdapter.itemCount}/${addDevicesAdapter.getCheckDevices().size}"
                        }
                    }
                }
            } else {
                loadingDialog.updateLoadingMsg("配网失败!")
                ConstantUtils.saveJson(index)
                loadingDialog.dismissDialog()
            }
        }

        override fun onDeviceSetSuccess(macAddress: String) {
            super.onDeviceSetSuccess(macAddress)
            deviceSetSusNumber++
            loadingDialog.updateLoadingMsg("SET:$deviceSetSusNumber/$addDeviceSize 失败:$deviceSetFailNumber")
        }

        override fun onDeviceSetFail(macAddress: String) {
            super.onDeviceSetFail(macAddress)
            deviceSetFailNumber++
            loadingDialog.updateLoadingMsg("SET:$deviceSetSusNumber/$addDeviceSize 失败:$deviceSetFailNumber")
        }
    }

    @SuppressLint("SetTextI18n")
    private fun initListener() {
        iv_check.setOnClickListener {
            //点击全选，停止搜索
            stopScan()

            val isCheck = !isAllCheck
            setCheck(isCheck)
            addDevicesAdapter.allCheck(isCheck)

            tv_dev_network_equipment?.text =
                "${getString(R.string.text_dev_number)}:${addDevicesAdapter.itemCount}/${addDevicesAdapter.getCheckDevices().size}"
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