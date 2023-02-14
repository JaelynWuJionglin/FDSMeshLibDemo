package com.linkiing.fdsmeshlibdemo.ui

import android.os.Bundle
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

class SettingActivity: BaseActivity() {
    private lateinit var loadingDialog: LoadingDialog
    private val jsonName = "JSON_NAME_SHEAR.json"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)

        loadingDialog = LoadingDialog(this)

        mesh_lib_ver?.setTextHint("V${FDSMeshApi.instance.getVersion()}")
        my_about?.setTextHint("V${ConstantUtils.getAppVerStr(this)}")

        my_shear?.setOnClickListener {
            shareJson()
        }

        switch_test_model?.isChecked = MMKVSp.instance.isTestModel()
        switch_test_model?.setOnCheckedChangeListener { buttonView, isChecked ->
            MMKVSp.instance.setTestModel(isChecked)
            if (isChecked){
                App.getInstance().defMeshConfigure()
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