package com.linkiing.fdsmeshlibdemo.ui

import android.os.Bundle
import android.view.View
import com.base.mesh.api.log.FileJaUtils
import com.base.mesh.api.log.LOGUtils
import com.godox.sdk.api.FDSMeshApi
import com.linkiing.fdsmeshlibdemo.R
import com.linkiing.fdsmeshlibdemo.app.App
import com.linkiing.fdsmeshlibdemo.mmkv.MMKVSp
import com.linkiing.fdsmeshlibdemo.ui.base.BaseActivity
import com.linkiing.fdsmeshlibdemo.utils.ConstantUtils
import com.linkiing.fdsmeshlibdemo.utils.FileUtils
import com.linkiing.fdsmeshlibdemo.view.dialog.LoadingDialog
import kotlinx.android.synthetic.main.activity_setting.*
import java.io.File

class SettingActivity : BaseActivity() {
    private lateinit var loadingDialog: LoadingDialog
    private val jsonName = "JSON_NAME_SHEAR.json"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)

        loadingDialog = LoadingDialog(this)

        mesh_lib_ver?.setTextHint("V${FDSMeshApi.instance.getVersion()}")
        my_about?.setTextHint("V${ConstantUtils.getAppVerStr(this)}")

        radio_group_pv?.check(
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
        switch_test_model?.isChecked = MMKVSp.instance.isTestModel()

        initListener()
    }

    private fun initListener() {
        reset_dev_network?.setOnClickListener {
            goActivity(ResetActivity::class.java, false)
        }

        radio_group_pv?.setOnCheckedChangeListener { _, checkedId ->
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

        my_shear_json?.setOnClickListener {
            shareJson()
        }

        my_shear_log?.setOnClickListener {
            LOGUtils.shareAppLogFile { file ->
                if (file != null) {
                    FileJaUtils.shareFile(this, file, getString(R.string.app_name))
                }
            }
        }

        switch_test_model?.setOnCheckedChangeListener { _, isChecked ->
            MMKVSp.instance.setTestModel(isChecked)
            if (isChecked) {
                App.getInstance().setTestMeshConfigure()
            } else {
                App.getInstance().setMeshConfigure()
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