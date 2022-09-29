package com.linkiing.fdsmeshlibdemo.ui

import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.godox.sdk.api.FDSMeshApi
import com.hjq.permissions.OnPermissionCallback
import com.linkiing.fdsmeshlibdemo.R
import com.linkiing.fdsmeshlibdemo.adapter.StudioAdapter
import com.linkiing.fdsmeshlibdemo.adapter.StudioDeviceAdapter
import com.linkiing.fdsmeshlibdemo.app.App
import com.linkiing.fdsmeshlibdemo.bean.StudioListBean
import com.linkiing.fdsmeshlibdemo.ui.base.BaseActivity
import com.linkiing.fdsmeshlibdemo.utils.PermissionsUtils
import com.linkiing.fdsmeshlibdemo.view.dialog.InputTextDialog
import com.telink.ble.mesh.util.LOGUtils
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_studio.*

class MainActivity : BaseActivity() {
    private lateinit var studioAdapter: StudioAdapter
    private lateinit var inputTextDialog: InputTextDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initView()
        initRecyclerView()
        initListener()
    }

    override fun onResume() {
        super.onResume()
        studioAdapter.updateData()
    }

    private fun initView() {
        inputTextDialog = InputTextDialog(this)
        inputTextDialog.setTitleText("新增Studio")
        inputTextDialog.setOnDialogListener {
            //新增Studio
            val studioListBean = StudioListBean(studioAdapter.getStudioNextIndex())
            studioListBean.name = it
            studioListBean.meshJsonStr = FDSMeshApi.instance.getInitMeshJson()
            studioAdapter.addStudio(studioListBean)
        }
    }

    private fun initRecyclerView() {
        studioAdapter = StudioAdapter()
        val manager = LinearLayoutManager(this)
        manager.orientation = LinearLayoutManager.VERTICAL
        recyclerView_studio.layoutManager = manager
        recyclerView_studio.adapter = studioAdapter

        studioAdapter.setOnItemClickListener {
            //权限请求
            PermissionsUtils.blePermissions(this, object : OnPermissionCallback {
                override fun onGranted(permissions: MutableList<String>?, all: Boolean) {
                    //权限申请成功

                    if (!it.choose && !TextUtils.isEmpty(it.meshJsonStr)) {
                        //需要切换Mesh网络
                        LOGUtils.e("切换网络 =============> ${it.name}")
                        FDSMeshApi.instance.importMeshJson(it.meshJsonStr)
                    }

                    goActivity(StudioActivity::class.java, "index",it.index,false)
                }

                override fun onDenied(permissions: MutableList<String>?, never: Boolean) {
                    super.onDenied(permissions, never)
                }
            })
        }
    }

    private fun initListener() {
        //新增Studio
        bt_add_studio.setOnClickListener {
            inputTextDialog.setDefText("Studio-${studioAdapter.getStudioNextIndex()}")
            inputTextDialog.showDialog()
        }

        //跳转到功能列表
        testOnclick.setOnClickListener {
            val bundle=Bundle()
            bundle.putInt("address",-1)
            bundle.putString("typeName","节点");
            goActivityBundle(ModeListActivity::class.java,false,bundle)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        LOGUtils.d("MainActivity onDestroy()")
    }
}