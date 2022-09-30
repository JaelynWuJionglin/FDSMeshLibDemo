package com.linkiing.fdsmeshlibdemo.ui

import android.os.Bundle
import com.godox.sdk.api.FDSMeshApi
import com.linkiing.fdsmeshlibdemo.R
import com.linkiing.fdsmeshlibdemo.ui.base.BaseActivity
import com.linkiing.fdsmeshlibdemo.utils.ConstantUtils
import kotlinx.android.synthetic.main.activity_setting.*

class SettingActivity: BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)

        mesh_lib_ver?.setTextHint("V${FDSMeshApi.instance.getVersion()}")
        my_about?.setTextHint("V${ConstantUtils.getAppVerStr(this)}")
    }
}