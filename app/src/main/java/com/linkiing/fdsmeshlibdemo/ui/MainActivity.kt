package com.linkiing.fdsmeshlibdemo.ui

import android.os.Bundle
import com.hjq.permissions.OnPermissionCallback
import com.linkiing.fdsmeshlibdemo.R
import com.linkiing.fdsmeshlibdemo.app.App
import com.linkiing.fdsmeshlibdemo.ui.base.BaseActivity
import com.linkiing.fdsmeshlibdemo.utils.PermissionsUtils
import com.telink.ble.mesh.util.LOGUtils
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //初始化设备自动连接
        App.getInstance().getMeshLogin().init()

        lr_studio1.setOnClickListener {
            //权限请求
            PermissionsUtils.blePermissions(this, object : OnPermissionCallback {
                override fun onGranted(permissions: MutableList<String>?, all: Boolean) {
                    //权限申请成功
                    goActivity(StudioActivity::class.java, false)
                }

                override fun onDenied(permissions: MutableList<String>?, never: Boolean) {
                    super.onDenied(permissions, never)
                }
            })
        }

        bt_test.setOnClickListener {

        }
    }

    override fun onDestroy() {
        super.onDestroy()
        LOGUtils.d("MainActivity onDestroy()")

        //销毁设备自动连接
        App.getInstance().getMeshLogin().destroy()
    }
}