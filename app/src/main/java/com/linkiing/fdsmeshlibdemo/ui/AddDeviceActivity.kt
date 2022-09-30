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

class AddDeviceActivity: BaseActivity() {
    private lateinit var addDevicesAdapter: AddDeviceAdapter
    private lateinit var loadingDialog: LoadingDialog
    private val searchDevices = FDSSearchDevicesApi()
    private val fdsAddOrRemoveDeviceApi = FDSAddOrRemoveDeviceApi(this)
    private var isAllCheck = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_device)

        initView()
        initRecyclerView()
        scanDevices()
        initListener()
    }

    private fun initView() {
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
        searchDevices.startScanDevice(this, "GD_LED", 20 * 1000, object : FDSBleDevCallBack {
            override fun onDeviceSearch(advertisingDevice: AdvertisingDevice, type: String) {
                addDevicesAdapter.addDevices(advertisingDevice,type)
            }

            override fun onScanTimeOut() {

            }
        })
    }

    private fun addDevice() {
        loadingDialog.showDialog()

        searchDevices.stopScan()

        //添加设备到mesh
        val deviceList = addDevicesAdapter.getCheckDevices()
        if (deviceList.isEmpty()){
            return
        }
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

                //节点设置默认名称
                for (fdsNode in fdsNodes) {
                    val symbol = fdsNode.symbol
                    FDSMeshApi.instance.renameFDSNodeInfo(fdsNode,"GD_LED_$symbol","")
                }

                addDevicesAdapter.removeItemAtInNetWork(fdsNodes)
                loadingDialog.dismissDialog()
            }
        })
    }

    private fun initListener(){
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
        searchDevices.stopScan()
        fdsAddOrRemoveDeviceApi.destroy()
    }
}