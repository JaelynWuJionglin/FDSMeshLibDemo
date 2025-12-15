package com.linkiing.fdsmeshlibdemo.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.view.WindowManager
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.base.mesh.api.listener.MeshNetworkOtaListener
import com.base.mesh.api.log.LOGUtils
import com.base.mesh.api.main.MeshLogin
import com.godox.agm.GodoxCommandApi
import com.godox.agm.callback.FirmwareCallBack
import com.godox.sdk.api.FDSMeshApi
import com.godox.sdk.model.FDSNodeInfo
import com.linkiing.fdsmeshlibdemo.R
import com.linkiing.fdsmeshlibdemo.databinding.ActivityMeshOtaBinding
import com.linkiing.fdsmeshlibdemo.adapter.MeshOtaDeviceAdapter
import com.linkiing.fdsmeshlibdemo.bean.FDSNodeBean
import com.linkiing.fdsmeshlibdemo.mmkv.MMKVSp
import com.linkiing.fdsmeshlibdemo.ui.base.BaseActivity
import com.linkiing.fdsmeshlibdemo.utils.ConstantUtils
import com.linkiing.fdsmeshlibdemo.utils.FileSelectorUtils
import com.linkiing.fdsmeshlibdemo.view.dialog.LoadingDialog
import com.telink.ble.mesh.util.Arrays
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import kotlin.math.floor

class MeshOtaActivity : BaseActivity<ActivityMeshOtaBinding>(), ActivityResultCallback<ActivityResult>,
    MeshNetworkOtaListener, FirmwareCallBack {
    private lateinit var loadingDialog: LoadingDialog
    private var exitWarningAlert: AlertDialog.Builder? = null
    private var selectResultLauncher: ActivityResultLauncher<Intent>? = null
    private var path = ""
    private var firmwareData = byteArrayOf()
    private var meshOtaDeviceAdapter: MeshOtaDeviceAdapter? = null
    private var paValue = 0

    override fun initBind(): ActivityMeshOtaBinding {
        return ActivityMeshOtaBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initView()
        initRecyclerView()
        initListener()

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (data != null && requestCode == FileSelectorUtils.SELECT_REQUEST_CODE) {
            //文件选择
            FileSelectorUtils.instance.onSelectActivityResult(requestCode, resultCode, data)
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun initView() {
        binding.titleBar.initTitleBar("MeshOta", "选择设备")

        loadingDialog = LoadingDialog(this)

        if (intent.hasExtra("paValue")) {
            paValue = intent.getIntExtra("paValue",0)
        }

        path = MMKVSp.instance.getFmPath()
        binding.tvFm.text = "固件:$path"

        selectResultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult(), this)
    }

    private fun initRecyclerView() {
        meshOtaDeviceAdapter = MeshOtaDeviceAdapter()
        val manager = LinearLayoutManager(this)
        manager.orientation = LinearLayoutManager.VERTICAL
        binding.recyclerViewDevices.layoutManager = manager
        binding.recyclerViewDevices.adapter = meshOtaDeviceAdapter

        meshOtaDeviceAdapter?.itemListener = {
            if (binding.btStart.isEnabled) {
                loadingDialog.showDialog(3000)
                GodoxCommandApi.instance.getFirmwareVersion(it, this)
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun initListener() {
        binding.btStart.setOnClickListener {
            if (!MeshLogin.instance.isLogin()) {
                ConstantUtils.toast(this, "Mesh未连接!")
                return@setOnClickListener
            }

            if (TextUtils.isEmpty(path)) {
                ConstantUtils.toast(this, "请选择固件!")
                return@setOnClickListener
            }

            readFirmware()

            if (firmwareData.isEmpty()) {
                ConstantUtils.toast(this, "文件不存在!")
                return@setOnClickListener
            }

            startBtEn(false)

            //start
            binding.progressBar.progress = 0
            binding.tvProgress.text = "0%"
            meshOtaDeviceAdapter?.updateItemDef()
            val list = meshOtaDeviceAdapter?.getItemList() ?: mutableListOf()
            val start = FDSMeshApi.instance.startMeshOTAWithOtaData(firmwareData, list, this)
            if (start) {
                ConstantUtils.toast(this, "开始升级!")
            } else {
                startBtEn(true)
            }
        }

        binding.btFm.setOnClickListener {
            FileSelectorUtils.instance.goSelectBin(this) {
                path = it
                MMKVSp.instance.setFmPath(path)
                binding.tvFm.text = "固件:$path"
            }
        }

        binding.titleBar.setOnEndTextListener {
            val intent = Intent(this, SelectNetWorkDeviceActivity::class.java)
            intent.putExtra("paValue", paValue)
            selectResultLauncher?.launch(intent)
        }

        binding.titleBar.getBackView().setOnClickListener {
            if (!binding.btStart.isEnabled) {
                showWarningDialog()
            } else {
                finish()
            }
        }
    }

    private fun startBtEn(isEn: Boolean) {
        binding.btStart.isEnabled = isEn
        binding.btFm.isEnabled = isEn
        if (isEn) {
            binding.btStart.setBackgroundColor(ContextCompat.getColor(this, R.color.color_92e5e9))
            binding.btFm.setBackgroundColor(ContextCompat.getColor(this, R.color.color_92e5e9))
            binding.loading.visibility = View.GONE
        } else {
            binding.btStart.setBackgroundColor(ContextCompat.getColor(this, R.color.grey))
            binding.btFm.setBackgroundColor(ContextCompat.getColor(this, R.color.grey))
            binding.loading.visibility = View.VISIBLE
        }
    }

    @SuppressLint("SetTextI18n")
    private fun readFirmware() {
        if (path == "") {
            return
        }
        val fmFile = File(path)
        if (fmFile.isFile && fmFile.name.endsWith(".bin")) {
            try {
                val stream: InputStream = FileInputStream(path)
                val length = stream.available()
                firmwareData = ByteArray(length)
                stream.read(firmwareData)
                stream.close()

                val firmwareId = ByteArray(4)
                System.arraycopy(firmwareData, 2, firmwareId, 0, 4)

                val pid = ByteArray(2)
                val vid = ByteArray(2)
                System.arraycopy(firmwareData, 2, pid, 0, 2)
                System.arraycopy(firmwareData, 4, vid, 0, 2)

                val pidInfo = Arrays.bytesToHexString(pid, ":")
                val vidInfo = Arrays.bytesToHexString(vid, ":")
                val firmVersion = "pid-$pidInfo vid-$vidInfo"

                binding.tvMsg.text = "固件信息: firmVersion:$firmVersion"
            } catch (e: IOException) {
                e.printStackTrace()
                firmwareData = byteArrayOf()
            }
        }
    }

    override fun onActivityResult(result: ActivityResult?) {
        if (result == null) {
            return
        }
        if (result.resultCode == RESULT_OK) {
            val checkDeviceList = result.data?.getSerializableExtra("checkDeviceList")
            if (checkDeviceList != null && checkDeviceList is MutableList<*>) {
                meshOtaDeviceAdapter?.updateItem(checkDeviceList as MutableList<FDSNodeInfo>)
            }
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onProgress(type: Int, progress: Int) {
        val pro = if (type == 0) {
            val rp = (progress / 100.0f) * 10
            if (rp <= 9) {
                floor(rp).toInt()
            } else {
                10
            }
        } else if (type == 1) {
            val rp = (progress / 100.0f) * 90
            if (rp <= 90) {
                floor(rp).toInt() + 10
            } else {
                100
            }
        } else {
            0
        }
        runOnUiThread {
            binding.progressBar.progress = pro
            binding.tvProgress.text = "$pro%"
        }
    }

    override fun onDeviceStatus(isSuccess: Boolean, meshAddress: Int) {
        runOnUiThread {
            meshOtaDeviceAdapter?.updateItemStatus(
                meshAddress,
                if (isSuccess) {
                    FDSNodeBean.UPGRADE_OTA_SUS
                } else {
                    FDSNodeBean.UPGRADE_OTA_FAIL
                }
            )
        }
    }

    override fun onComplete() {
        LOGUtils.e("MeshOtaActivity onComplete")
        runOnUiThread {
            meshOtaDeviceAdapter?.updateAllItemSusOrFail(true)
            ConstantUtils.toast(this, "升级完成!")
            startBtEn(true)
        }
    }

    override fun onFailed(errorCode: Int) {
        LOGUtils.e("MeshOtaActivity onFailed  errorCode:$errorCode")
        runOnUiThread {
            meshOtaDeviceAdapter?.updateAllItemSusOrFail(false)
            ConstantUtils.toast(this, "升级失败! errorCode:$errorCode")
            startBtEn(true)
        }
    }

    override fun onSuccess(address: Int, version: Int, paValue: Int) {
        loadingDialog.dismissDialog()

        //更新蓝牙固件版本
        val fdsNodeInfo = FDSMeshApi.instance.getFDSNodeInfoByMeshAddress(address) ?: return
        FDSMeshApi.instance.updateFirmwareVersion(fdsNodeInfo, version)

        val msg = "固件版本:$version"
        LOGUtils.i("GATT_OTA ==> $msg")
        ConstantUtils.toast(this, msg)

        meshOtaDeviceAdapter?.updateItemFDSNodeInfo(fdsNodeInfo)
    }

    override fun onBackPressed() {
       if (!binding.btStart.isEnabled) {
           showWarningDialog()
       } else {
           super.onBackPressed()
       }
    }

    private fun showWarningDialog() {
        if (exitWarningAlert == null) {
            exitWarningAlert = AlertDialog.Builder(this)
            exitWarningAlert?.setCancelable(true)
            exitWarningAlert?.setTitle("警告")
            exitWarningAlert?.setMessage("正在OTA中,退出会导致OTA失败!")
            exitWarningAlert?.setNegativeButton("退出") { _, _ ->
                FDSMeshApi.instance.stopMeshOTA()
                finish()
            }
            exitWarningAlert?.setPositiveButton("取消") { dialog, _ ->
                dialog.dismiss()
            }
        }
        exitWarningAlert?.show()
    }

    override fun finish() {
        super.finish()
        FDSMeshApi.instance.stopMeshOTA()
    }
}