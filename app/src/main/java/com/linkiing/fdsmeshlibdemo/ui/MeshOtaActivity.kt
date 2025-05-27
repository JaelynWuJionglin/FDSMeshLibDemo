package com.linkiing.fdsmeshlibdemo.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.base.mesh.api.listener.MeshNetworkOtaListener
import com.base.mesh.api.log.LOGUtils
import com.base.mesh.api.main.MeshLogin
import com.godox.sdk.api.FDSMeshApi
import com.godox.sdk.model.FDSNodeInfo
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.linkiing.fdsmeshlibdemo.R
import com.linkiing.fdsmeshlibdemo.adapter.MeshOtaDeviceAdapter
import com.linkiing.fdsmeshlibdemo.bean.FDSNodeBean
import com.linkiing.fdsmeshlibdemo.mmkv.MMKVSp
import com.linkiing.fdsmeshlibdemo.ui.base.BaseActivity
import com.linkiing.fdsmeshlibdemo.utils.ConstantUtils
import com.linkiing.fdsmeshlibdemo.utils.FileSelectorUtils
import com.linkiing.fdsmeshlibdemo.view.dialog.LoadingDialog
import com.linkiing.fdsmeshlibdemo.view.dialog.MeshOtaDialog
import com.telink.ble.mesh.core.MeshUtils
import com.telink.ble.mesh.util.Arrays
import kotlinx.android.synthetic.main.activity_mesh_ota.*
import kotlinx.android.synthetic.main.activity_select_devices.recyclerView_devices
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import java.nio.ByteOrder
import kotlin.math.floor

class MeshOtaActivity : BaseActivity(), ActivityResultCallback<ActivityResult>,
    MeshNetworkOtaListener {
    private var selectResultLauncher: ActivityResultLauncher<Intent>? = null
    private var path = ""
    private var firmwareData = byteArrayOf()
    private var meshOtaDeviceAdapter: MeshOtaDeviceAdapter? = null
    private var isPa = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mesh_ota)

        initView()
        initRecyclerView()
        initListener()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (data != null && requestCode == FileSelectorUtils.SELECT_REQUEST_CODE) {
            //文件选择
            FileSelectorUtils.instance.onSelectActivityResult(requestCode, resultCode, data)
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun initView() {
        titleBar?.initTitleBar("MeshOta", "选择设备")

        isPa = intent.getIntExtra("isPA",0)

        path = MMKVSp.instance.getFmPath()
        tv_fm?.text = "固件:$path"

        selectResultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult(), this)
    }

    private fun initRecyclerView() {
        meshOtaDeviceAdapter = MeshOtaDeviceAdapter()
        val manager = LinearLayoutManager(this)
        manager.orientation = LinearLayoutManager.VERTICAL
        recyclerView_devices.layoutManager = manager
        recyclerView_devices.adapter = meshOtaDeviceAdapter
    }

    @SuppressLint("SetTextI18n")
    private fun initListener() {
        bt_start?.setOnClickListener {
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

            //start
            progressBar.progress = 0
            meshOtaDeviceAdapter?.updateItemDef()
            val list = meshOtaDeviceAdapter?.getItemList() ?: mutableListOf()
            val start = FDSMeshApi.instance.startMeshOTAWithOtaData(firmwareData, list, this)
            if (start) {
                ConstantUtils.toast(this,"开始升级!")
                bt_start?.isEnabled = false
                bt_start?.setBackgroundColor(ContextCompat.getColor(this,R.color.grey))
            }
        }

        bt_fm?.setOnClickListener {
            FileSelectorUtils.instance.goSelectBin(this) {
                path = it
                MMKVSp.instance.setFmPath(path)
                tv_fm?.text = "固件:$path"
            }
        }

        titleBar?.setOnEndTextListener {
            val intent = Intent(this, SelectNetWorkDeviceActivity::class.java)
            intent.putExtra("isPA",isPa)
            selectResultLauncher?.launch(intent)
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

                tv_msg?.text = "固件信息: firmVersion:$firmVersion"
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
        if (result.resultCode == Activity.RESULT_OK) {
            val checkDeviceList = result.data?.getSerializableExtra("checkDeviceList")
            if (checkDeviceList != null && checkDeviceList is MutableList<*>) {
                meshOtaDeviceAdapter?.updateItem(checkDeviceList as MutableList<FDSNodeInfo>)
            }
        }
    }

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
            progressBar?.progress = pro
        }
    }

    override fun onDeviceFailed(meshAddress: Int) {
        runOnUiThread {
            meshOtaDeviceAdapter?.updateItem(meshAddress, FDSNodeBean.UPGRADE_OTA_FAIL)
        }
    }

    override fun onComplete() {
        runOnUiThread {
            meshOtaDeviceAdapter?.updateItemOtherSusOrFail(true)
            ConstantUtils.toast(this, "升级完成!")
            bt_start?.isEnabled = true
            bt_start?.setBackgroundColor(ContextCompat.getColor(this,R.color.color_92e5e9))
        }
    }

    override fun onFailed(errorCode: Int) {
        runOnUiThread {
            meshOtaDeviceAdapter?.updateItemOtherSusOrFail(false)
            ConstantUtils.toast(this, "升级失败! errorCode:$errorCode")
            bt_start?.isEnabled = true
            bt_start?.setBackgroundColor(ContextCompat.getColor(this,R.color.color_92e5e9))
        }
        LOGUtils.e("MeshOtaActivity onFailed  errorCode:$errorCode")
    }

    override fun onDestroy() {
        super.onDestroy()
        FDSMeshApi.instance.stopMeshOTA()
    }
}