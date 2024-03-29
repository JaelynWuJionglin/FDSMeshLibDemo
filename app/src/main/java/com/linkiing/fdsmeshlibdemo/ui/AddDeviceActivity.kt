package com.linkiing.fdsmeshlibdemo.ui

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.base.mesh.api.main.MeshLogin
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
import com.linkiing.fdsmeshlibdemo.view.dialog.LoadingDialog
import com.telink.ble.mesh.entity.AdvertisingDevice
import com.base.mesh.api.log.LOGUtils
import kotlinx.android.synthetic.main.activity_add_device.*

class AddDeviceActivity : BaseActivity() {
    private lateinit var addDevicesAdapter: AddDeviceAdapter
    private lateinit var loadingDialog: LoadingDialog
    private val searchDevices = FDSSearchDevicesApi()
    private val fdsAddOrRemoveDeviceApi = FDSAddOrRemoveDeviceApi(this)
    private var isAllCheck = false
    private var publishFdsNodeInfoList = mutableListOf<FDSNodeInfo>()
    private var isScanning = true
    private var addDeviceSize = 0
    private var addDeviceSusSize = 0
    private var addDeviceFailSize = 0

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
        searchDevices.startScanDevice(this, filterName, 20 * 1000, object : FDSBleDevCallBack {
            override fun onDeviceSearch(advertisingDevice: AdvertisingDevice, type: String) {
                addDevicesAdapter.addDevices(advertisingDevice, type)
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

        loadingDialog.showDialog()

        addDeviceSize = deviceList.size
        addDeviceSusSize = 0
        addDeviceFailSize = 0
        loadingDialog.updateLoadingMsg("$addDeviceSusSize/$addDeviceSize 失败:$addDeviceFailSize")

        fdsAddOrRemoveDeviceApi.deviceAddNetWork(deviceList, object : FDSAddNetWorkCallBack {
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
                for (fdsNode in fdsNodes) {
                    FDSMeshApi.instance.renameFDSNodeInfo(fdsNode, "GD_LED_${fdsNode.type}", "")
                }

                addDevicesAdapter.removeItemAtInNetWork(fdsNodes)

                //主动查询在线状态
                val isOk = FDSMeshApi.instance.refreshFDSNodeInfoState()
                LOGUtils.v("refreshFDSNodeInfoState() =====> isOk:$isOk")

                loadingDialog.dismissDialog()

            }

            /*
             * 单个设备入网成功返回
             */
            override fun onFDSNodeSuccess(fdsNodeInfo: FDSNodeInfo) {
                super.onFDSNodeSuccess(fdsNodeInfo)
                addDeviceSusSize++
                loadingDialog.updateLoadingMsg("$addDeviceSusSize/$addDeviceSize 失败:$addDeviceFailSize")

                //设备成功入网，配置打开设备主动上报在线状态
                val isFDSNodeConfigPublish = isFDSNodeConfigPublish(fdsNodeInfo)
                LOGUtils.i("onFDSNodeSuccess() =========> " + "FDSNodeState:${fdsNodeInfo.getFDSNodeState()}  macAddress:${fdsNodeInfo.macAddress} isFDSNodeConfigPublish:$isFDSNodeConfigPublish")
                if (isFDSNodeConfigPublish) {
                    return
                }

                /**
                 * 配置节点主动上报在线状态
                 */
                val isOk = FDSMeshApi.instance.configFDSNodePublishState(true, fdsNodeInfo)
                if (isOk) {
                    publishFdsNodeInfoList.add(fdsNodeInfo)
                }
                LOGUtils.i("configFDSNodePublishState() =====> isOk:$isOk")
            }

            /*
             * 单个设备入网失败返回
             */
            override fun onFDSNodeFail(fdsNodeInfo: FDSNodeInfo) {
                super.onFDSNodeFail(fdsNodeInfo)
                addDeviceFailSize++
                loadingDialog.updateLoadingMsg("$addDeviceSusSize/$addDeviceSize 失败:$addDeviceFailSize")
            }
        })
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

    private fun isFDSNodeConfigPublish(fdsNodeInfo: FDSNodeInfo): Boolean {
        if (publishFdsNodeInfoList.isEmpty()) {
            return false
        }
        for (fdsNode in publishFdsNodeInfoList) {
            if (fdsNode.meshAddress == fdsNodeInfo.meshAddress) {
                return true
            }
        }
        return false
    }

    override fun finish() {
        super.finish()

        //停止搜索，释放资源
        searchDevices.destroy()

        fdsAddOrRemoveDeviceApi.destroy()
    }
}