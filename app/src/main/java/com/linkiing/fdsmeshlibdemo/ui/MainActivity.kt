package com.linkiing.fdsmeshlibdemo.ui

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import androidx.recyclerview.widget.LinearLayoutManager
import com.base.mesh.api.main.MeshLogin
import com.godox.sdk.api.FDSMeshApi
import com.godox.sdk.tool.HttpUtils
import com.google.gson.Gson
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
import com.linkiing.fdsmeshlibdemo.view.dialog.LoadingDialog
import com.telink.ble.mesh.util.LOGUtils
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : BaseActivity() {
    private lateinit var studioAdapter: StudioAdapter
    private lateinit var inputTextDialog: InputTextDialog
    private lateinit var loadingDialog: LoadingDialog
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

        loadingDialog = LoadingDialog(this)

        inputTextDialog = InputTextDialog(this)
        inputTextDialog.setTitleText("新增Studio")
        inputTextDialog.setOnDialogListener {

            val studioListBean = StudioListBean(studioAdapter.getStudioNextIndex())
            studioListBean.name = it
            if (TextUtils.isEmpty(jsonStr)) {
                //新增Studio
                studioListBean.meshJsonStr = FDSMeshApi.instance.getInitMeshJson()
                studioAdapter.addStudio(studioListBean)
            } else {
                //导入json数据
                loadingDialog.showDialog()
                updateProvisionerAddress(jsonStr) { newJson ->
                    runOnUiThread{
                        loadingDialog.dismissDialog()
                        if (!TextUtils.isEmpty(newJson)) {
                            studioListBean.meshJsonStr = newJson
                            studioAdapter.addStudio(studioListBean)
                        } else {
                            ConstantUtils.toast(this,"导入失败！")
                        }
                    }
                }
            }
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
                    studioAdapter.changeChoose(it)
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

    /**
     * 更新json的ProvisionAddress
     * 步骤：1，通过后台#queryAndSync#接口同步获取到唯一的《provisionerAddress》
     *      2，通过 FDSMeshApi 的 #updateMeshJsonProvisionerAddress()# 方法修改json信息，并保存数据，下次中可直接使用。
     *      3，通过 的 #importMeshJson()#方法导入并应用mesh网络
     *
     *     （2，3可使用 #updateAndImportMeshJson()# 方法合并为一步执行，导入成功后可通过 #getCurrentMeshJson()# 方法获取json数据保存）
     *
     * 注：只需要在第一次导入其他app分享的json数据时，才同需要同步服务端的provisionerAddress修改到json中。
     *    之后保存修改后的json可直接使用。
     *    同一台手机相同的json信息，每次从后台请求的provisionerAddress相同，应尽量避免多次调用给后台制造压力。
     */
    private fun updateProvisionerAddress(jsonStr: String, updateProvisionerAddressListener: (String) -> Unit) {
        //从json中获取信息
        val meshJsonInfo = FDSMeshApi.instance.getMeshInfoByJson(jsonStr)
        LOGUtils.i("HTTP importMeshJson ==> netWorkKey:${meshJsonInfo.netWorkKey}  provisionAddress:${meshJsonInfo.provisionerAddress}")

        //从服务器获取分配的provisionerAddress
        val getProvisionAddressUrlStr = "http://godox.light.belvie-iot.com/api/provision/queryAndSync?" +
                "app_uuid=${FDSMeshApi.instance.getAppLocalUUID()}" +
                "&network_key=${meshJsonInfo.netWorkKey}" +
                "&address=${meshJsonInfo.provisionerAddress}"
        HttpUtils().httpRequest(this,HttpUtils.GET,getProvisionAddressUrlStr){
            LOGUtils.i("HTTP getProvisionAddressUrlStr ==> it:$it")

            if (!TextUtils.isEmpty(it)) {
                try {
                    //请求到服务器的provisionAddress，根据provisionAddress导入json
                    val httpProvisionBean = Gson().fromJson(it,HttpProvisionBean::class.java)
                    LOGUtils.i("HTTP ==> address:${httpProvisionBean.data.address}")

                    var newMeshJson = jsonStr
                    if (httpProvisionBean.code == 0) {
                        //获取到ProvisionerAddress，更新到要导入的JSON文件中
                        val serverProvisionAddress = httpProvisionBean.data.address
                        newMeshJson = FDSMeshApi.instance.updateMeshJsonProvisionerAddress(jsonStr,serverProvisionAddress)
                    }
                    updateProvisionerAddressListener(newMeshJson)
                } catch (e: Exception) {
                    e.printStackTrace()
                    updateProvisionerAddressListener("")
                }
            } else {
                LOGUtils.e("Error! 请求服务器上的provisionAddress出错！")
                updateProvisionerAddressListener("")
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