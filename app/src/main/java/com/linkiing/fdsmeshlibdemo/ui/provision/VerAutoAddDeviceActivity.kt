package com.linkiing.fdsmeshlibdemo.ui.provision

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.base.mesh.api.log.LOGUtils
import com.godox.sdk.api.FDSAddOrRemoveDeviceApi
import com.godox.sdk.api.FDSSearchDevicesApi
import com.godox.sdk.callbacks.FDSBleDevCallBack
import com.linkiing.fdsmeshlibdemo.R
import com.linkiing.fdsmeshlibdemo.adapter.AddDeviceAdapter
import com.linkiing.fdsmeshlibdemo.databinding.ActivityAddDeviceBinding
import com.linkiing.fdsmeshlibdemo.mmkv.MMKVSp
import com.linkiing.fdsmeshlibdemo.ui.base.BaseActivity
import com.linkiing.fdsmeshlibdemo.utils.ConstantUtils
import com.linkiing.fdsmeshlibdemo.view.dialog.LoadingDialog
import com.telink.ble.mesh.entity.AdvertisingDevice

/**
 * 根据版本自适应配网（预留）
 */
class VerAutoAddDeviceActivity : BaseActivity<ActivityAddDeviceBinding>() {
    private lateinit var addDevicesAdapter: AddDeviceAdapter
    private lateinit var loadingDialog: LoadingDialog
    private val handler = Handler(Looper.getMainLooper())
    private val searchDevices = FDSSearchDevicesApi()
    private val fdsAddOrRemoveDeviceApi = FDSAddOrRemoveDeviceApi(this)
    private var isAllCheck = false
    private var isScanning = true
    private var index = 0

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
        LOGUtils.d("VerAutoAddDeviceActivity =============> index:$index")
        if (index == 0) {
            finish()
        }

        binding.titleBar.initTitleBar(true, R.drawable.refresh)
        binding.titleBar.setTitle("搜索设备(Auto)")
        binding.titleBar.setOnEndImageListener {
            addDevicesAdapter.clearList()
            binding.tvDevNetworkEquipment.text = "${getString(R.string.text_dev_number)}:0/0"
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
            this, filterName, 10 * 60 * 1000, object : FDSBleDevCallBack {

                @SuppressLint("SetTextI18n")
                override fun onDeviceSearch(
                    advertisingDevice: AdvertisingDevice,
                    deviceName: String,//设备名(广播中解析的,有时有些手机从“advertisingDevice.device.name”获取的广播名可能为空或null)
                    type: String,
                    firmwareVersion: Int,
                ) {
                    addDevicesAdapter.addDevices(
                        advertisingDevice, deviceName, type, firmwareVersion
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
            ConstantUtils.toast(this,"开发中...")
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
        handler.removeCallbacksAndMessages(null)

        //停止搜索，释放资源
        searchDevices.destroy()

        fdsAddOrRemoveDeviceApi.destroy()
    }
}