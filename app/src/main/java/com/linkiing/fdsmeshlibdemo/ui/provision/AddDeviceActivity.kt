package com.linkiing.fdsmeshlibdemo.ui.provision

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.base.mesh.api.bean.MeshCode
import com.base.mesh.api.listener.ConfigNodePublishStateListener
import com.base.mesh.api.log.LOGUtils
import com.base.mesh.api.main.MeshLogin
import com.godox.sdk.api.FDSAddOrRemoveDeviceApi
import com.godox.sdk.api.FDSMeshApi
import com.godox.sdk.api.FDSSearchDevicesApi
import com.godox.sdk.callbacks.FDSAddNetWorkCallBack
import com.godox.sdk.callbacks.FDSBleDevCallBack
import com.godox.sdk.model.FDSNodeInfo
import com.linkiing.fdsmeshlibdemo.databinding.ActivityAddDeviceBinding
import com.linkiing.fdsmeshlibdemo.ui.base.BaseActivity
import com.linkiing.fdsmeshlibdemo.R
import com.linkiing.fdsmeshlibdemo.adapter.AddDeviceAdapter
import com.linkiing.fdsmeshlibdemo.app.App
import com.linkiing.fdsmeshlibdemo.mmkv.MMKVSp
import com.linkiing.fdsmeshlibdemo.utils.ConstantUtils
import com.linkiing.fdsmeshlibdemo.view.dialog.LoadingDialog
import com.telink.ble.mesh.entity.AdvertisingDevice

class AddDeviceActivity : BaseActivity<ActivityAddDeviceBinding>() {
    private lateinit var addDevicesAdapter: AddDeviceAdapter
    private lateinit var loadingDialog: LoadingDialog
    private val searchDevices = FDSSearchDevicesApi()
    private val fdsAddOrRemoveDeviceApi = FDSAddOrRemoveDeviceApi(this)
    private var isAllCheck = false
    private var publishFdsNodeInfoList = mutableListOf<FDSNodeInfo>()//保存配置在线状态失败的设备。
    private var isScanning = true
    private var addDeviceSize = 0
    private var addDeviceSusSize = 0
    private var addDeviceFailSize = 0
    private var index = 0
    private var allNumber = 0
    private var susNumber = 0
    private var failNumber = 0

    override fun initBind(): ActivityAddDeviceBinding {
        return ActivityAddDeviceBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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

        binding.titleBar.initTitleBar(true, R.drawable.refresh)
        binding.titleBar.setTitle("搜索设备")
        binding.titleBar.setOnEndImageListener {
            addDevicesAdapter.clearList()
            binding.tvDevNetworkEquipment.text = "${getString(R.string.text_dev_number)}:0/0"
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
        binding.recyclerViewDevices.layoutManager = manager
        binding.recyclerViewDevices.adapter = addDevicesAdapter

        addDevicesAdapter.setIsAllCheckListener {
            setCheck(it)
            binding.tvDevNetworkEquipment.text =
                "${getString(R.string.text_dev_number)}:${addDevicesAdapter.itemCount}/${addDevicesAdapter.getCheckDevices().size}"
        }
    }

    private fun scanDevices() {
        isScanning = true

        binding.progressBar.visibility = View.VISIBLE

        val filterName = "GD_LED"
        searchDevices.startScanDevice(
            this,
            filterName,
            10 * 60 * 1000,
            object : FDSBleDevCallBack {
                @SuppressLint("SetTextI18n")
                override fun onDeviceSearch(
                    advertisingDevice: AdvertisingDevice,
                    deviceName: String,//设备名(广播中解析的,有时有些手机从“advertisingDevice.device.name”获取的广播名可能为空或null)
                    type: String,
                    firmwareVersion: Int,
                ) {
                    addDevicesAdapter.addDevices(
                        advertisingDevice,
                        deviceName,
                        type,
                        firmwareVersion
                    )
                    binding.tvDevNetworkEquipment.text =
                        "${getString(R.string.text_dev_number)}:${addDevicesAdapter.itemCount}/${addDevicesAdapter.getCheckDevices().size}"
                }

                override fun onScanTimeOut() {
                    isScanning = false
                    binding.progressBar.visibility = View.GONE
                }

                /*
                 * 开启搜索设备失败。
                 */
                override fun onScanFail() {
                    isScanning = false
                    binding.progressBar.visibility = View.GONE
                }
            })
    }

    private fun stopScan() {
        searchDevices.stopScan()
        isScanning = false
        binding.progressBar.visibility = View.GONE
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
         * meshCode 是否成功
         * fdsNodes 入网成功的节点
         */
        override fun onComplete(
            meshCode: MeshCode,
            fdsNodes: MutableList<FDSNodeInfo>,
        ) {
            LOGUtils.d("AddDeviceActivity meshCode:$meshCode size:${fdsNodes.size}")

            addDeviceSusSize = fdsNodes.size
            addDeviceFailSize = addDeviceSize - addDeviceSusSize
            loadingDialog.updateLoadingMsg("$addDeviceSusSize/$addDeviceSize 失败:$addDeviceFailSize")

            addDevicesAdapter.removeItemAtInNetWork(fdsNodes)

            //在线状态配置失败的设备，重新配置设备在线状态上报。
            //版本>=0x49的设备不会在publishFdsNodeInfoList内，所有这里不用区分是那种配置方式。
            configFDSNodePublishState(publishFdsNodeInfoList)
        }

        /*
         * 单个设备入网成功返回
         */
        override fun onFDSNodeSuccess(fdsNodeInfo: FDSNodeInfo) {
            super.onFDSNodeSuccess(fdsNodeInfo)
            addDeviceSusSize++
            loadingDialog.updateLoadingMsg("$addDeviceSusSize/$addDeviceSize 失败:$addDeviceFailSize")

            //配置设备在线状态上报(这里有500ms的BLE直连时间发送指令)
            configFDSNodePublishState(fdsNodeInfo)
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

    /**
     * 配置节点主动上报在线状态
     */
    private fun configFDSNodePublishState(fdsNodeInfo: FDSNodeInfo) {
        if (fdsNodeInfo.firmwareVersion < 0x49) {
            //设备需要sdk发指令配置
            publishFdsNodeInfoList.add(fdsNodeInfo)
            val isOk = FDSMeshApi.instance.configFDSNodePublishState(
                true,
                fdsNodeInfo,
                object : ConfigNodePublishStateListener {

                    // 当前设备配置结果回调
                    override fun onComplete(
                        meshCode: MeshCode,
                        meshAddress: Int,
                    ) {
                        LOGUtils.d("configFDSNodePublishState onComplete() =====> meshCode:$meshCode  meshAddress:$meshAddress")

                        //配置成功从失败列表删除
                        if (meshCode == MeshCode.Success) {
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
            )
            LOGUtils.i("configFDSNodePublishState() =====> isOk:$isOk")
        } else {
            //设备不需要sdk发指令配置，sdk只需要配置本地
            val isOk = FDSMeshApi.instance.setFDSNodePublishModel(true, fdsNodeInfo)
            LOGUtils.i("setFDSNodePublishModel() =====> isOk:$isOk")
        }
    }

    /**
     * 批量配置设备在线状态
     */
    private fun configFDSNodePublishState(fdsNodeInfoList: MutableList<FDSNodeInfo>) {
        //设备需要sdk发指令配置
        if (fdsNodeInfoList.isEmpty()) {
            onAddDeviceComplete()
            return
        }
        MeshLogin.instance.autoConnect(30 * 1000L) {
            if (it) {
                allNumber = fdsNodeInfoList.size
                val isOk = FDSMeshApi.instance.configFDSNodePublishState(
                    true,
                    fdsNodeInfoList,
                    object : ConfigNodePublishStateListener {

                        // 当前设备配置结果回调
                        override fun onComplete(
                            meshCode: MeshCode,
                            meshAddress: Int,
                        ) {
                            LOGUtils.d("configFDSNodePublishState onComplete() =====> meshCode:$meshCode  meshAddress:$meshAddress")
                            if (meshCode == MeshCode.Success) {
                                susNumber++
                            } else {
                                failNumber++
                            }
                            runOnUiThread {
                                loadingDialog.updateLoadingMsg("配置在线:$susNumber/$allNumber 失败:$failNumber")
                            }
                        }

                        //全部结果回调
                        override fun onAllComplete(meshCode: MeshCode, failedList: MutableList<Int>) {
                            onAddDeviceComplete()
                        }
                    }
                )
                LOGUtils.i("configFDSNodePublishState() =====> isOk:$isOk")
            } else {
                onAddDeviceComplete()
                ConstantUtils.toast(this,"配置在线状态失败!")
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun onAddDeviceComplete() {
        ConstantUtils.saveJson(index)
        runOnUiThread {
            loadingDialog.dismissDialog()
            binding.tvDevNetworkEquipment.text =
                "${getString(R.string.text_dev_number)}:${addDevicesAdapter.itemCount}/${addDevicesAdapter.getCheckDevices().size}"
        }
    }

    @SuppressLint("SetTextI18n")
    private fun initListener() {
        binding.ivCheck.setOnClickListener {
            //点击全选，停止搜索
            stopScan()

            val isCheck = !isAllCheck
            setCheck(isCheck)
            addDevicesAdapter.allCheck(isCheck)
            binding.tvDevNetworkEquipment.text =
                "${getString(R.string.text_dev_number)}:${addDevicesAdapter.itemCount}/${addDevicesAdapter.getCheckDevices().size}"
        }

        binding.btAddDevice.setOnClickListener {
            addDevice()
        }
    }

    private fun setCheck(isCheck: Boolean) {
        isAllCheck = isCheck
        if (isAllCheck) {
            binding.ivCheck.setBackgroundResource(R.drawable.checked_image_on)
        } else {
            binding.ivCheck.setBackgroundResource(R.drawable.checked_image_off)
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        //停止搜索，释放资源
        searchDevices.destroy()

        fdsAddOrRemoveDeviceApi.destroy()
    }
}