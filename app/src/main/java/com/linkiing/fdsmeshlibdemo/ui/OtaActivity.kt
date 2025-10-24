package com.linkiing.fdsmeshlibdemo.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import com.base.mesh.api.log.LOGUtils
import com.godox.agm.GodoxCommandApi
import com.godox.agm.callback.FirmwareCallBack
import com.godox.agm.callback.MCUCallBack
import com.godox.agm.callback.OpenPaCallback
import com.godox.sdk.api.FDSMeshApi
import com.godox.sdk.model.FDSNodeInfo
import com.linkiing.fdsmeshlibdemo.databinding.ActivityOtaBinding
import com.linkiing.fdsmeshlibdemo.mmkv.MMKVSp
import com.linkiing.fdsmeshlibdemo.ui.base.BaseActivity
import com.linkiing.fdsmeshlibdemo.utils.ConstantUtils
import com.linkiing.fdsmeshlibdemo.utils.FileSelectorUtils
import com.linkiing.fdsmeshlibdemo.view.dialog.LoadingDialog
import com.linkiing.fdsmeshlibdemo.view.dialog.MeshOtaDialog
import java.io.File

class OtaActivity : BaseActivity<ActivityOtaBinding>() {
    private var loadingDialog: LoadingDialog? = null
    private var meshOtaDialog: MeshOtaDialog? = null
    private var meshMcuUpgradeDialog: MeshOtaDialog? = null
    private var isMcuUpgrade: Boolean = false
    private var meshAddress = 0
    private var path = ""
    private var upFdsNodeInfo: FDSNodeInfo? = null
    private var fmPaValue = 0

    override fun initBind(): ActivityOtaBinding {
        return ActivityOtaBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initView()
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
        isMcuUpgrade = intent.getBooleanExtra("isMcuUpgrade", false)
        meshAddress = intent.getIntExtra("meshAddress", 0)
        LOGUtils.d("OtaActivity =============> meshAddress:$meshAddress  isMcuUpgrade:$isMcuUpgrade")
        if (meshAddress <= 0) {
            finish()
            return
        }

        loadingDialog = LoadingDialog(this)

        meshOtaDialog = MeshOtaDialog(this, false)
        meshOtaDialog?.setListener {
            finish()
        }

        meshMcuUpgradeDialog = MeshOtaDialog(this, true)
        meshMcuUpgradeDialog?.setListener {
            finish()
        }

        binding.titleBar.setTitle(
            if (isMcuUpgrade) {
                "MCU-OTA"
            } else {
                "BLE-OTA"
            }
        )

        path = MMKVSp.instance.getFmPath()
        binding.tvFm.text = "固件:$path"

        getVersion(false)
    }

    @SuppressLint("SetTextI18n")
    private fun initListener() {
        binding.btStart.setOnClickListener {
            if (TextUtils.isEmpty(path)) {
                ConstantUtils.toast(this, "请选择固件!")
                return@setOnClickListener
            }
            if (!File(path).exists()) {
                ConstantUtils.toast(this, "文件不存在!")
                return@setOnClickListener
            }

            if (upFdsNodeInfo != null) {
                if (isMcuUpgrade) {
                    mcuUpgrade(upFdsNodeInfo)
                } else {
                    bleUpgrade(upFdsNodeInfo, fmPaValue)
                }
            } else {
                getVersion(true)
            }
        }

        binding.btFm.setOnClickListener {
            FileSelectorUtils.instance.goSelectBin(this) {
                path = it
                MMKVSp.instance.setFmPath(path)
                binding.tvFm.text = "固件:$path"
            }
        }
    }

    //获取固件版本信息
    private fun getVersion(isStart: Boolean) {
        loadingDialog?.showDialog(3000L)

        if (isMcuUpgrade) {
            GodoxCommandApi.instance.getMcuVersion(meshAddress, object : MCUCallBack {
                @SuppressLint("SetTextI18n")
                override fun onSuccess(address: Int, productVersion: String, mcuVersion: String) {
                    upFdsNodeInfo = FDSMeshApi.instance.getFDSNodeInfoByMeshAddress(address)

                    binding.tvMsg1.text = "产品版本:$productVersion"
                    binding.tvMsg2.text = "MCU方案版本:$mcuVersion"

                    loadingDialog?.dismissDialog()

                    if (isStart) {
                        mcuUpgrade(upFdsNodeInfo)
                    }
                }

            })
        } else {
            GodoxCommandApi.instance.getFirmwareVersion(meshAddress, object : FirmwareCallBack {
                @SuppressLint("SetTextI18n")
                override fun onSuccess(address: Int, version: Int, paValue: Int) {
                    upFdsNodeInfo = FDSMeshApi.instance.getFDSNodeInfoByMeshAddress(address)

                    fmPaValue = paValue

                    LOGUtils.v("OTA_VER ==> version:${version.toString(16)}")

                    binding.tvMsg1.text = "固件版本: ${version.toString(16)}"
                    binding.tvMsg2.text = "当前固件 paValue: $fmPaValue"

                    loadingDialog?.dismissDialog()

                    //test =========================================================================
//                    path = if (version <= 0x50) {
//                        "LK8620_mesh_GD_v000051_20240709_OTA_3.bin"
//                    } else {
//                        "LK8620_mesh_GD_v000050_20240709_OTA_3.bin"
//                    }
//                    tv_fm?.text = "固件:$path"
                    //==============================================================================

                    if (isStart) {
                        bleUpgrade(upFdsNodeInfo, fmPaValue)
                    }
                }
            })
        }
    }

    private fun bleUpgrade(fdsNodeInfo: FDSNodeInfo?, paValue: Int) {
        if (fdsNodeInfo == null) {
            return
        }

        /*
         * 1,比对固件版本
         * 2,判断是否是PA固件
         */
        if (paValue == 1) {
            //PA固件打开PA升级功能
            GodoxCommandApi.instance.openPaUpgrade(meshAddress, object : OpenPaCallback {
                override fun openPaComplete() {
                    //打开PA升级成功，开始升级
                    meshOtaDialog?.setOldFirmwareInfo(true)
                    meshOtaDialog?.showDialog(fdsNodeInfo, path)
                }
            })

        } else {
            //非PA固件，直接升级
            meshOtaDialog?.setOldFirmwareInfo(false)
            meshOtaDialog?.showDialog(fdsNodeInfo, path)
        }
    }

    private fun mcuUpgrade(fdsNodeInfo: FDSNodeInfo?) {
        if (fdsNodeInfo != null) {
            meshMcuUpgradeDialog?.showDialog(fdsNodeInfo, path)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        meshOtaDialog?.dismiss()
        meshMcuUpgradeDialog?.dismiss()
    }
}