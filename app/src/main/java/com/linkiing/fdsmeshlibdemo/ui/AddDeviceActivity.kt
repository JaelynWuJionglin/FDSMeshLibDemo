package com.linkiing.fdsmeshlibdemo.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.base.mesh.api.listener.ConfigNodePublishStateListener
import com.base.mesh.api.log.LOGUtils
import com.godox.sdk.api.FDSAddOrRemoveDeviceApi
import com.godox.sdk.api.FDSMeshApi
import com.godox.sdk.api.FDSSearchDevicesApi
import com.godox.sdk.callbacks.FDSAddNetWorkCallBack
import com.godox.sdk.callbacks.FDSBleDevCallBack
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

class AddDeviceActivity : BaseActivity() {
    private lateinit var addDevicesAdapter: AddDeviceAdapter
    private lateinit var loadingDialog: LoadingDialog
    private val handler = Handler(Looper.getMainLooper())
    private val searchDevices = FDSSearchDevicesApi()
    private val fdsAddOrRemoveDeviceApi = FDSAddOrRemoveDeviceApi(this)
    private val configPublishUtils = ConfigPublishUtils()
    private var isAllCheck = false
    private var publishFdsNodeInfoList = mutableListOf<FDSNodeInfo>()//保存配置在线状态失败的设备。
    private var isScanning = true
    private var addDeviceSize = 0
    private var addDeviceSusSize = 0
    private var addDeviceFailSize = 0
    private var index = 0

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
        LOGUtils.d("AddDeviceActivity =============> index:$index")
        if (index == 0) {
            finish()
        }

        titleBar?.initTitleBar(true, R.drawable.refresh)
        titleBar?.setTitle("搜索设备")
        titleBar?.setOnEndImageListener {
            addDevicesAdapter.clearList()
            tv_dev_network_equipment?.text = "${getString(R.string.text_dev_number)}:0/0"
            if (isScanning) {
                stopScan()
            }
            scanDevices()
        }

        loadingDialog = LoadingDialog(this)
        loadingDialog.msgClickListener = {
            /**
             * 注意:调用方法后不会立即停止配网，会等待当前正在配网的设备配网完成；
             * 且需要回调onComplete()之后，才可以重新使用deviceAddNetWork()方法。
             */
            fdsAddOrRemoveDeviceApi.stopDeviceAddNetWork(fdeAddNetWorkCallBack)
            loadingDialog.updateLoadingMsg("$addDeviceSusSize/$addDeviceSize 结束中...")
        }
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
                addDevicesAdapter.addDevices(advertisingDevice, deviceName, type, firmwareVersion)
                tv_dev_network_equipment?.text =
                    "${getString(R.string.text_dev_number)}:${addDevicesAdapter.itemCount}/${addDevicesAdapter.getCheckDevices().size}"
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

        loadingDialog.showDialog()

        addDeviceSize = deviceList.size
        addDeviceSusSize = 0
        addDeviceFailSize = 0
        loadingDialog.updateLoadingMsg("$addDeviceSusSize/$addDeviceSize 失败:$addDeviceFailSize")

        publishFdsNodeInfoList.clear()
        fdsAddOrRemoveDeviceApi.deviceAddNetWork(deviceList, fdeAddNetWorkCallBack)
    }

    /**
     * FDSAddNetWorkCallBack
     */
    private val fdeAddNetWorkCallBack = object : FDSAddNetWorkCallBack {
        /*
         * 入网完成回调
         * isAllSuccess 是否全部入网成功
         * fdsNodes 入网成功的节点
         */
        override fun onComplete(
            isAllSuccess: Boolean,
            fdsNodes: MutableList<FDSNodeInfo>
        ) {
            LOGUtils.d("AddDeviceActivity isAllSuccess:$isAllSuccess size:${fdsNodes.size}")

            addDeviceSusSize = fdsNodes.size
            addDeviceFailSize = addDeviceSize - addDeviceSusSize
            loadingDialog.updateLoadingMsg("$addDeviceSusSize/$addDeviceSize 失败:$addDeviceFailSize")

            //节点设置默认名称
            if (!MMKVSp.instance.isTestModel()) {
                for (fdsNode in fdsNodes) {
                    FDSMeshApi.instance.renameFDSNodeInfo(fdsNode, "GD_LED_${fdsNode.type}", "")
                }
            }

            addDevicesAdapter.removeItemAtInNetWork(fdsNodes)


            if (publishFdsNodeInfoList.isEmpty()) {
                onAddDeviceComplete()
            } else {
                //配置未配置成功的节点在线状态
                configPublishUtils.startConfigPublish(
                    publishFdsNodeInfoList,
                    handler
                ) { isComplete, allNumber, susNumber, failNumber ->
                    runOnUiThread {
                        loadingDialog.updateLoadingMsg("配置在线:$susNumber/$allNumber 失败:$failNumber")

                        if (isComplete) {
                            onAddDeviceComplete()
                        }
                    }
                }
            }
        }

        /*
         * 单个设备入网成功返回
         */
        override fun onFDSNodeSuccess(fdsNodeInfo: FDSNodeInfo) {
            super.onFDSNodeSuccess(fdsNodeInfo)
            addDeviceSusSize++
            loadingDialog.updateLoadingMsg("$addDeviceSusSize/$addDeviceSize 失败:$addDeviceFailSize")

            if (!MMKVSp.instance.isTestModel()) {

                /**
                 * 配置节点主动上报在线状态
                 */
                if (fdsNodeInfo.firmwareVersion >= 0x49) {
                    FDSMeshApi.instance.setFDSNodePublishModel(true, fdsNodeInfo)
                    LOGUtils.i("setFDSNodePublishModel() =====> true")
                } else {
                    val isOk = FDSMeshApi.instance.configFDSNodePublishState(
                        true,
                        fdsNodeInfo,
                        configNodePublishStateListener
                    )
                    publishFdsNodeInfoList.add(fdsNodeInfo)

                    LOGUtils.i("configFDSNodePublishState() =====> isOk:$isOk")
                }
            }
        }

        /*
         * 单个设备入网失败返回
         */
        override fun onFDSNodeFail(fdsNodeInfo: FDSNodeInfo) {
            super.onFDSNodeFail(fdsNodeInfo)
            addDeviceFailSize++
            loadingDialog.updateLoadingMsg("$addDeviceSusSize/$addDeviceSize 失败:$addDeviceFailSize")
        }
    }

    @SuppressLint("SetTextI18n")
    private fun onAddDeviceComplete() {
        //主动查询在线状态
        val isOk = FDSMeshApi.instance.refreshFDSNodeInfoState()
        LOGUtils.v("refreshFDSNodeInfoState() =====> isOk:$isOk")

        ConstantUtils.saveJson(index)
        loadingDialog.dismissDialog()
        tv_dev_network_equipment?.text =
            "${getString(R.string.text_dev_number)}:${addDevicesAdapter.itemCount}/${addDevicesAdapter.getCheckDevices().size}"
    }

    /**
     * 配置节点主动上报在线状态 结果回调
     */
    private val configNodePublishStateListener = object : ConfigNodePublishStateListener {
        override fun onComplete(success: Boolean, meshAddress: Int) {
            LOGUtils.d("configFDSNodePublishState onComplete() =====> success:$success  meshAddress:$meshAddress")
            if (success) {
                val iterator = publishFdsNodeInfoList.iterator()
                while (iterator.hasNext()) {
                    val fdsNodeInfo = iterator.next()
                    if (fdsNodeInfo.meshAddress == meshAddress) {
                        iterator.remove()
                    }
                }
            }
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

        //停止搜索，释放资源
        searchDevices.destroy()

        fdsAddOrRemoveDeviceApi.destroy()
    }
}