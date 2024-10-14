package com.linkiing.fdsmeshlibdemo.ui

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
import com.godox.sdk.callbacks.FDSBleDevCallBack
import com.godox.sdk.callbacks.FDSFastAddNetWorkCallBack
import com.godox.sdk.model.FDSNodeInfo
import com.godox.sdk.tool.DevicesUtils
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
                val isFilterDev = true//isFilterDev(advertisingDevice)
                //LOGUtils.e("FDSSearchDevicesApi ${advertisingDevice.device.address} type:$type  fv:$fv  isFilterDev:$isFilterDev")
                if (isFilterDev /*&& deviceMacNumber(advertisingDevice) >= 0x7e86*/) {
                    //固件版本 >= 0x55
                    if (firmwareVersion >= 0x55) {
                        addDevicesAdapter.addDevices(advertisingDevice, deviceName, type, firmwareVersion)
                        tv_dev_network_equipment?.text =
                            "${getString(R.string.text_dev_number)}:${addDevicesAdapter.itemCount}/${addDevicesAdapter.getCheckDevices().size}"
                    }
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
         *（注意：固件版本 >= 52 支持
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
                        for (fdsNode in resultList) {
                            FDSMeshApi.instance.renameFDSNodeInfo(
                                fdsNode,
                                "GD_LED_${fdsNode.type}",
                                ""
                            )
                        }
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


    //=============================================================================================

    private fun deviceMacNumber(advertisingDevice: AdvertisingDevice): Int {
        val mac = advertisingDevice.device.address.replace(":", "")
        val macBytes = ByteUtils.hexStringToBytes(mac)
        if (macBytes.size == 6) {
            val num = ByteUtils.byteArrayToInt(byteArrayOf(macBytes[4], macBytes[5]))
            LOGUtils.e("TAGGG ====> num:${num.toString(16)}  $num")
            return num
        }
        return 0
    }

    private fun isFilterDev(advertisingDevice: AdvertisingDevice): Boolean {
        for (mac in macList) {
            val devMac = advertisingDevice.device?.address?.uppercase(Locale.ENGLISH)?.trim() ?: ""
            if (devMac != "") {
                if (devMac == mac.uppercase(Locale.ENGLISH).trim()) {
                    return true
                }
            }

        }
        return false
    }

    private val macList = mutableListOf(
        /*"A4:C1:38:E4:14:13",
        "A4:C1:38:88:55:96",
        "A4:C1:38:58:3C:FE",
        "A4:C1:38:60:32:75",
        "A4:C1:38:25:D8:36",
        "A4:C1:38:9A:AE:F6",
        "A4:C1:38:75:52:1E",
        "A4:C1:38:36:BD:C2",
        "A4:C1:38:16:5C:FF",
        "A4:C1:38:D6:0C:FB",
        "A4:C1:38:FF:85:DF",
        "A4:C1:38:CD:C1:8F",
        "A4:C1:38:66:50:46",
        "A4:C1:38:13:E8:A8",
        "A4:C1:38:0C:DB:10",
        "A4:C1:38:1C:31:C1",
        "A4:C1:38:3D:4E:CC",
        "A4:C1:38:DB:0A:52",
        "A4:C1:38:FF:DC:D6",
        "A4:C1:38:44:FA:7F",
        "A4:C1:38:F3:F5:F7",
        "A4:C1:38:C7:CA:4F",
        "A4:C1:38:75:8C:E7",
        "A4:C1:38:D0:61:B0",
        "A4:C1:38:BC:43:4A",
        "A4:C1:38:17:A3:DE",
        "A4:C1:38:80:A6:A3",
        "A4:C1:38:65:88:9B",
        "A4:C1:38:93:7D:A8",
        "A4:C1:38:B4:A8:25",
        "A4:C1:38:D3:CB:4B",
        "A4:C1:38:E0:EE:51",
        "A4:C1:38:E9:EB:03",
        "A4:C1:38:E8:17:9B",
        "A4:C1:38:66:2F:64",
        "A4:C1:38:70:F5:84",
        "A4:C1:38:96:DA:24",
        "A4:C1:38:97:E8:F2",
        "A4:C1:38:17:FD:AE",
        "A4:C1:38:D0:DD:EA",
        "A4:C1:38:D2:88:CD",
        "A4:C1:38:74:F5:14",
        "A4:C1:38:10:40:F2",
        "A4:C1:38:BA:53:F5",
        "A4:C1:38:E7:0E:6C",
        "A4:C1:38:B3:3A:08",
        "A4:C1:38:E5:89:30",
        "A4:C1:38:46:D8:B0",
        "A4:C1:38:54:AF:73",
        "A4:C1:38:CB:1C:18",
        "A4:C1:38:24:50:CE",
        "A4:C1:38:E0:57:D6",
        "A4:C1:38:68:0C:3E",
        "A4:C1:38:B2:B8:96",
        "A4:C1:38:4C:33:E2",
        "A4:C1:38:3B:AC:00",
        "A4:C1:38:76:C7:94",
        "A4:C1:38:B1:B9:1A",
        "A4:C1:38:59:DE:FE",
        "A4:C1:38:F3:E6:FE",
        "A4:C1:38:D6:F3:C0",
        "A4:C1:38:45:90:03",
        "A4:C1:38:DD:C7:3A",
        "A4:C1:38:01:B5:15",
        "A4:C1:38:17:79:7A",
        "A4:C1:38:11:35:7D",
        "A4:C1:38:B3:8F:F4",
        "A4:C1:38:6C:2B:54",
        "A4:C1:38:4B:1B:83",
        "A4:C1:38:80:9A:EF",
        "A4:C1:38:F2:7A:9C",
        "A4:C1:38:CE:7F:79",
        "A4:C1:38:2B:7A:72",
        "A4:C1:38:ED:50:E1",
        "A4:C1:38:2B:14:4E",
        "A4:C1:38:1A:45:13",
        "A4:C1:38:EF:0B:9E",
        "A4:C1:38:95:70:4B",
        "A4:C1:38:8E:A2:32",
        "A4:C1:38:CD:EE:D0",
        "A4:C1:38:FD:CD:4E",
        "A4:C1:38:E6:A8:85",
        "A4:C1:38:3A:1D:AC",
        "A4:C1:38:65:32:5D",
        "A4:C1:38:B1:DF:C0",
        "A4:C1:38:27:8B:B7",
        "A4:C1:38:CD:8C:65",
        "A4:C1:38:01:6E:35",
        "A4:C1:38:B8:F0:03",
        "A4:C1:38:E5:1F:1D",
        "A4:C1:38:75:75:A1",
        "A4:C1:38:17:A2:75",
        "A4:C1:38:15:B9:01",
        "A4:C1:38:EB:57:36",
        "A4:C1:38:F5:45:02",
        "A4:C1:38:8D:C3:75",
        "A4:C1:38:07:C3:75",*/

        //48
        "A4:C1:38:08:7E:6C",
        "A4:C1:38:08:7E:80",
        "A4:C1:38:08:7E:7C",
        "A4:C1:38:08:7E:85",
        "A4:C1:38:08:7E:81",
        "A4:C1:38:08:7E:84",
        "A4:C1:38:08:7E:7E",
        "A4:C1:38:08:7E:68",
        "A4:C1:38:08:7E:70",
        "A4:C1:38:08:7E:76",
        "A4:C1:38:08:7E:79",
        "A4:C1:38:08:7E:7B",
        "A4:C1:38:08:7E:66",
        "A4:C1:38:08:7E:6D",
        "A4:C1:38:08:7E:6F",
        "A4:C1:38:08:7E:5D",
        "A4:C1:38:08:7E:8B",
        "A4:C1:38:08:7E:73",
        "A4:C1:38:08:7E:67",
        "A4:C1:38:08:7E:71",
        "A4:C1:38:08:7E:5C",
        "A4:C1:38:08:7E:64",
        "A4:C1:38:08:7E:87",
        "A4:C1:38:08:7E:75",
        "A4:C1:38:08:7E:7D",
        "A4:C1:38:08:7E:69",
        "A4:C1:38:08:7E:6B",
        "A4:C1:38:08:7E:77",
        "A4:C1:38:08:7E:6E",
        "A4:C1:38:08:7E:83",
        "A4:C1:38:08:7E:63",
        "A4:C1:38:08:7E:61",
        "A4:C1:38:08:7E:5E",
        "A4:C1:38:07:C3:75",
        "A4:C1:38:08:7E:82",
        "A4:C1:38:08:7E:5F",
        "A4:C1:38:08:7E:65",
        "A4:C1:38:08:7E:60",
        "A4:C1:38:08:7E:7F",
        "A4:C1:38:08:7E:6A",
        "A4:C1:38:08:7E:78",
        "A4:C1:38:08:7E:62",
        "A4:C1:38:08:7E:89",
        "A4:C1:38:08:7E:72",
        "A4:C1:38:08:7E:86",
        "A4:C1:38:08:7E:74",
        "A4:C1:38:08:7E:7A",
        "A4:C1:38:08:7E:8A",
        "A4:C1:38:08:7E:88",

        //30
        "A4:C1:38:08:7E:8C",
        "A4:C1:38:08:7E:A8",
        "A4:C1:38:08:7E:8F",
        "A4:C1:38:08:7E:A9",
        "A4:C1:38:08:7E:90",
        "A4:C1:38:08:7E:A6",
        "A4:C1:38:08:7E:8E",
        "A4:C1:38:08:7E:A7",
        "A4:C1:38:08:7E:9F",
        "A4:C1:38:08:7E:94",
        "A4:C1:38:08:7E:9B",
        "A4:C1:38:08:7E:8D",
        "A4:C1:38:08:7E:A5",
        "A4:C1:38:08:7E:A4",
        "A4:C1:38:08:7E:A3",
        "A4:C1:38:08:7E:9D",
        "A4:C1:38:08:7E:A2",
        "A4:C1:38:08:7E:99",
        "A4:C1:38:08:7E:93",
        "A4:C1:38:08:7E:9C",
        "A4:C1:38:08:7E:91",
        "A4:C1:38:08:7E:9A",
        "A4:C1:38:08:7E:96",
        "A4:C1:38:08:7E:92",
        "A4:C1:38:08:7E:98",
        "A4:C1:38:08:7E:A0",
        "A4:C1:38:08:7E:9E",
        "A4:C1:38:08:7E:A1",
        "A4:C1:38:08:7E:95",
        "A4:C1:38:08:7E:97"
    )
}