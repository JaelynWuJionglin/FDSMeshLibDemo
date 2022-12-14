package com.linkiing.fdsmeshlibdemo.ui

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.godox.sdk.api.FDSAddOrRemoveDeviceApi
import com.godox.sdk.api.FDSMeshApi
import com.godox.sdk.api.FDSSearchDevicesApi
import com.godox.sdk.callbacks.FDSAddNetWorkCallBack
import com.godox.sdk.callbacks.FDSBleDevCallBack
import com.godox.sdk.model.FDSNodeInfo
import com.linkiing.fdsmeshlibdemo.R
import com.linkiing.fdsmeshlibdemo.adapter.AddDeviceAdapter
import com.linkiing.fdsmeshlibdemo.ui.base.BaseActivity
import com.linkiing.fdsmeshlibdemo.view.dialog.LoadingDialog
import com.telink.ble.mesh.entity.AdvertisingDevice
import com.telink.ble.mesh.util.LOGUtils
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
        searchDevices.startScanDevice(this, "GD_LED", 20 * 1000, object : FDSBleDevCallBack {
            override fun onDeviceSearch(advertisingDevice: AdvertisingDevice, type: String) {
                addDevicesAdapter.addDevices(advertisingDevice, type)
            }

            override fun onScanTimeOut() {
                isScanning = false
            }

            /*
             * ???????????????????????????
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
        loadingDialog.showDialog()

        stopScan()

        //???????????????mesh
        val deviceList = addDevicesAdapter.getCheckDevices()
        if (deviceList.isEmpty()) {
            return
        }
        addDeviceSize = deviceList.size
        addDeviceSusSize = 0
        addDeviceFailSize = 0
        loadingDialog.updateLoadingMsg("$addDeviceSusSize/$addDeviceSize ??????:$addDeviceFailSize")

        fdsAddOrRemoveDeviceApi.deviceAddNetWork(deviceList, object : FDSAddNetWorkCallBack {
            /*
             * ??????????????????
             * isAllSuccess ????????????????????????
             * fdsNodes ?????????????????????
             */
            override fun onComplete(
                isAllSuccess: Boolean,
                fdsNodes: MutableList<FDSNodeInfo>
            ) {
                LOGUtils.d("AddDeviceActivity isAllSuccess:$isAllSuccess size:${fdsNodes.size}")

                addDeviceSusSize = fdsNodes.size
                addDeviceFailSize = addDeviceSize - addDeviceSusSize
                loadingDialog.updateLoadingMsg("$addDeviceSusSize/$addDeviceSize ??????:$addDeviceFailSize")

                //????????????????????????
                for (fdsNode in fdsNodes) {
                    FDSMeshApi.instance.renameFDSNodeInfo(fdsNode, "GD_LED_${fdsNode.type}", "")
                }

                addDevicesAdapter.removeItemAtInNetWork(fdsNodes)
                loadingDialog.dismissDialog()
            }

            /*
             * ??????????????????????????????
             */
            override fun onFDSNodeSuccess(fdsNodeInfo: FDSNodeInfo) {
                super.onFDSNodeSuccess(fdsNodeInfo)
                addDeviceSusSize++
                loadingDialog.updateLoadingMsg("$addDeviceSusSize/$addDeviceSize ??????:$addDeviceFailSize")

                //???????????????????????????????????????????????????????????????
                val isFDSNodeConfigPublish = isFDSNodeConfigPublish(fdsNodeInfo)
                LOGUtils.i("onFDSNodeSuccess() =========> " + "FDSNodeState:${fdsNodeInfo.getFDSNodeState()}  macAddress:${fdsNodeInfo.macAddress} isFDSNodeConfigPublish:$isFDSNodeConfigPublish")
                if (isFDSNodeConfigPublish) {
                    return
                }

                /**
                 * ????????????????????????????????????
                 */
                val isOk = FDSMeshApi.instance.configFDSNodePublishState(true, fdsNodeInfo)
                if (isOk) {
                    publishFdsNodeInfoList.add(fdsNodeInfo)
                }
                LOGUtils.i("configFDSNodePublishState() =====> isOk:$isOk")
            }

            /*
             * ??????????????????????????????
             */
            override fun onFDSNodeFail(fdsNodeInfo: FDSNodeInfo) {
                super.onFDSNodeFail(fdsNodeInfo)
                addDeviceFailSize++
                loadingDialog.updateLoadingMsg("$addDeviceSusSize/$addDeviceSize ??????:$addDeviceFailSize")
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

        //???????????????????????????
        searchDevices.destroy()

        /**
         * ??????????????????????????????mesh?????????
         * ?????????????????????????????????MeshLogin.instance.autoConnect()?????????????????????
         * ?????????onStop() onDestroy()????????????????????????????????????
         */
        fdsAddOrRemoveDeviceApi.destroy()
    }
}