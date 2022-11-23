package com.linkiing.fdsmeshlibdemo.ui

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import androidx.recyclerview.widget.LinearLayoutManager
import com.godox.sdk.api.FDSMeshApi
import com.godox.sdk.tool.HttpUtils
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.hjq.permissions.OnPermissionCallback
import com.linkiing.fdsmeshlibdemo.R
import com.linkiing.fdsmeshlibdemo.adapter.StudioAdapter
import com.linkiing.fdsmeshlibdemo.bean.HttpProvisionBean
import com.linkiing.fdsmeshlibdemo.bean.StudioListBean
import com.linkiing.fdsmeshlibdemo.ui.base.BaseActivity
import com.linkiing.fdsmeshlibdemo.utils.ConstantUtils
import com.linkiing.fdsmeshlibdemo.utils.FileSelectorUtils
import com.linkiing.fdsmeshlibdemo.utils.FileUtils
import com.linkiing.fdsmeshlibdemo.utils.PermissionsUtils
import com.linkiing.fdsmeshlibdemo.view.dialog.InputTextDialog
import com.telink.ble.mesh.util.LOGUtils
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : BaseActivity() {
    private lateinit var studioAdapter: StudioAdapter
    private lateinit var inputTextDialog: InputTextDialog
    private var jsonStr = ""

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
        titleBar?.initTitleBar(false, R.drawable.settings_image)

        titleBar?.setOnEndImageListener{
            goActivity(SettingActivity::class.java,false)
        }

        inputTextDialog = InputTextDialog(this)
        inputTextDialog.setTitleText("新增Studio")
        inputTextDialog.setOnDialogListener {
            //新增Studio
            val studioListBean = StudioListBean(studioAdapter.getStudioNextIndex())
            studioListBean.name = it
            if (TextUtils.isEmpty(jsonStr)) {
                studioListBean.meshJsonStr = FDSMeshApi.instance.getInitMeshJson()
            } else {
                studioListBean.meshJsonStr = jsonStr
            }
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
                        importMeshJson(it)
                    } else {
                        goActivity(StudioActivity::class.java, "index",it.index,false)
                    }
                    studioAdapter.changeChoose(it)
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
            jsonStr = ""
            inputTextDialog.setDefText("Studio-${studioAdapter.getStudioNextIndex()}")
            inputTextDialog.showDialog()
        }

        //导入json数据
        bt_import_json?.setOnClickListener {
            FileSelectorUtils.instance.goSelectJson(this) { path ->
                if (!TextUtils.isEmpty(path)) {
                    jsonStr = FileUtils.getJsonSelect(path)
                    if (!TextUtils.isEmpty(jsonStr)) {
                        inputTextDialog.setDefText("Studio-${studioAdapter.getStudioNextIndex()}")
                        inputTextDialog.showDialog()
                    } else {
                        ConstantUtils.toast(this,"选择错误！")
                    }
                }
            }
        }
    }

    private fun importMeshJson(studioListBean: StudioListBean) {

        //从json中获取信息
        val meshJsonInfo = FDSMeshApi.instance.getMeshJsonInfoByJson(studioListBean.meshJsonStr)
        LOGUtils.i("HTTP importMeshJson ==> netWorkKey:${meshJsonInfo.netWorkKey}  provisionAddress:${meshJsonInfo.provisionAddress}")

        //获取服务器上的provisionAddress
        val getProvisionAddressUrlStr = "http://godox.light.belvie-iot.com/api/provision/getByUuidAndSync?" +
                "mesh_uuid=${meshJsonInfo.netWorkKey}&address=${meshJsonInfo.provisionAddress}"
        HttpUtils().httpRequest(this,HttpUtils.GET,getProvisionAddressUrlStr){
            LOGUtils.i("HTTP getProvisionAddressUrlStr ==> it:$it")

            if (!TextUtils.isEmpty(it)) {
                try {
                    //请求到服务器的provisionAddress，根据provisionAddress导入json
                    val httpProvisionBean = Gson().fromJson(it,HttpProvisionBean::class.java)
                    LOGUtils.i("HTTP ==> address:${httpProvisionBean.data.address}")
                    var serverProvisionAddress = 0
                    if (httpProvisionBean.code == 0) {
                        //服务器有当前mesh的地址分配记录
                        serverProvisionAddress = httpProvisionBean.data.address
                    }

                    if (FDSMeshApi.instance.importMeshJson(studioListBean.meshJsonStr,serverProvisionAddress)) {
                        goActivity(StudioActivity::class.java, "index",studioListBean.index,false)
                    } else {
                        LOGUtils.e("Error! 导入json失败！serverProvisionAddress:$serverProvisionAddress")
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } else {
                LOGUtils.e("Error! 请求服务器上的provisionAddress出错！")
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (data != null && requestCode == FileSelectorUtils.SELECT_REQUEST_CODE) {
            //文件选择
            FileSelectorUtils.instance.onSelectActivityResult(requestCode, resultCode, data)
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onDestroy() {
        super.onDestroy()
        LOGUtils.d("MainActivity onDestroy()")
    }
}