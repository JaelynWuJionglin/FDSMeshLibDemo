package com.linkiing.fdsmeshlibdemo.ui

import android.os.Bundle
import com.base.mesh.api.log.FileJaUtils
import com.base.mesh.api.log.LOGUtils
import com.godox.sdk.api.FDSMeshApi
import com.linkiing.fdsmeshlibdemo.R
import com.linkiing.fdsmeshlibdemo.databinding.ActivitySettingBinding
import com.linkiing.fdsmeshlibdemo.mmkv.MMKVSp
import com.linkiing.fdsmeshlibdemo.ui.base.BaseActivity
import com.linkiing.fdsmeshlibdemo.utils.ConstantUtils
import com.linkiing.fdsmeshlibdemo.utils.FileUtils
import com.linkiing.fdsmeshlibdemo.view.dialog.LoadingDialog
import java.io.File

class SettingActivity : BaseActivity<ActivitySettingBinding>() {
    private lateinit var loadingDialog: LoadingDialog
    private val jsonName = "JSON_NAME_SHEAR.json"

    override fun initBind(): ActivitySettingBinding {
        return ActivitySettingBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        loadingDialog = LoadingDialog(this)

        binding.meshLibVer.setTextHint("V${FDSMeshApi.instance.getVersion()}")
        binding.myAbout.setTextHint("V${ConstantUtils.getAppVerStr(this)}")

        binding.radioGroupPv.check(
            when (MMKVSp.instance.getProvisionModel()) {
                MMKVSp.PROVISION_MODEL_FAST -> {
                    R.id.rd_fast
                }

                MMKVSp.PROVISION_MODEL_AUTO -> {
                    R.id.rd_ver_auto
                }

                else -> {
                    R.id.rd_def
                }
            }
        )

        initListener()
    }

    private fun initListener() {
        binding.resetDevNetwork.setOnClickListener {
            goActivity(ResetActivity::class.java, false)
        }

        binding.radioGroupPv.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rd_def -> {
                    MMKVSp.instance.setProvisionModel(MMKVSp.PROVISION_MODEL_DEF)
                }

                R.id.rd_fast -> {
                    MMKVSp.instance.setProvisionModel(MMKVSp.PROVISION_MODEL_FAST)
                }

                R.id.rd_ver_auto -> {
                    MMKVSp.instance.setProvisionModel(MMKVSp.PROVISION_MODEL_AUTO)
                }
            }
        }

        binding.myShearJson.setOnClickListener {
            shareJson()
        }

        binding.myShearLog?.setOnClickListener {
            LOGUtils.shareAppLogFile { file ->
                if (file != null) {
                    FileJaUtils.shareFile(this, file, getString(R.string.app_name))
                }
            }
        }
    }

    private fun shareJson() {
        loadingDialog.showDialog()
        Thread {
            val jsonStr = FDSMeshApi.instance.getCurrentMeshJson()
            FileUtils.saveOrReplaceJson(this@SettingActivity, jsonStr, jsonName)
            val file = File(FileUtils.getMeshJsonDir(this@SettingActivity), jsonName)

            runOnUiThread {
                ConstantUtils.shareFile(this@SettingActivity, file)
                loadingDialog.dismissDialog()
            }

        }.start()
    }
}