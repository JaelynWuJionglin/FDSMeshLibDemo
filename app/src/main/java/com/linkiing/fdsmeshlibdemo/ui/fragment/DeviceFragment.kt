package com.linkiing.fdsmeshlibdemo.ui.fragment

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.base.mesh.api.listener.NodeStatusChangeListener
import com.base.mesh.api.main.MeshLogin
import com.godox.sdk.api.FDSAddOrRemoveDeviceApi
import com.godox.sdk.api.FDSMeshApi
import com.godox.sdk.callbacks.FDSRemoveNodeCallBack
import com.godox.sdk.model.FDSNodeInfo
import com.linkiing.fdsmeshlibdemo.R
import com.linkiing.fdsmeshlibdemo.adapter.StudioDeviceAdapter
import com.linkiing.fdsmeshlibdemo.ui.AddDeviceActivity
import com.linkiing.fdsmeshlibdemo.ui.ModeListActivity
import com.linkiing.fdsmeshlibdemo.ui.base.BaseFragment
import com.linkiing.fdsmeshlibdemo.utils.ConstantUtils
import com.linkiing.fdsmeshlibdemo.view.dialog.MeshOtaDialog
import com.linkiing.fdsmeshlibdemo.view.dialog.InputTextDialog
import com.linkiing.fdsmeshlibdemo.view.dialog.LoadingDialog
import com.linkiing.fdsmeshlibdemo.view.dialog.StuDevBottomMenuDialog
import com.telink.ble.mesh.util.LOGUtils
import kotlinx.android.synthetic.main.device_fragment.*

class DeviceFragment: BaseFragment(R.layout.device_fragment), NodeStatusChangeListener {
    private lateinit var stuDevBottomMenuDialog: StuDevBottomMenuDialog
    private lateinit var loadingDialog: LoadingDialog
    private lateinit var renameTextDialog: InputTextDialog
    private lateinit var meshOtaDialog: MeshOtaDialog
    private lateinit var meshMcuUpgradeDialog: MeshOtaDialog
    private var studioDeviceAdapter: StudioDeviceAdapter? = null
    private var fdsAddOrRemoveDeviceApi:FDSAddOrRemoveDeviceApi? = null
    private var fdsNodeInfo: FDSNodeInfo? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView()
        initRecyclerView()
        initListener()
    }

    override fun onResume() {
        super.onResume()
        LOGUtils.v("DeviceFragment onResume()")
        MeshLogin.instance.autoConnect()
        studioDeviceAdapter?.update()
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        LOGUtils.v("DeviceFragment onHiddenChanged() hidden:$hidden")
        if (!hidden) {
            studioDeviceAdapter?.update()
        }
    }

    private fun initView() {
        loadingDialog = LoadingDialog(requireContext())
        stuDevBottomMenuDialog = StuDevBottomMenuDialog(mContext)

        renameTextDialog = InputTextDialog(mContext)
        renameTextDialog.setTitleText("重命名节点？")

        fdsAddOrRemoveDeviceApi = FDSAddOrRemoveDeviceApi(mActivity)

        meshOtaDialog = MeshOtaDialog(mActivity,false)
        meshMcuUpgradeDialog = MeshOtaDialog(mActivity,true)
    }

    private fun initRecyclerView() {
        studioDeviceAdapter = StudioDeviceAdapter()
        val manager = LinearLayoutManager(mContext)
        manager.orientation = LinearLayoutManager.VERTICAL
        recyclerView_devices?.layoutManager = manager
        recyclerView_devices?.adapter = studioDeviceAdapter

        studioDeviceAdapter?.setItemLongClickListener {
            fdsNodeInfo = it
            stuDevBottomMenuDialog.showDialog()
        }

        studioDeviceAdapter?.setItemClickListener {
            if (it.getFDSNodeState() == FDSNodeInfo.ON_OFF_STATE_OFFLINE) {
                ConstantUtils.toast(mContext,getString(R.string.equipment_not_online_text))
            } else {
                val bundle= Bundle()
                bundle.putInt("address",it.meshAddress)
                bundle.putString("typeName",it.name)
                goActivityBundle(ModeListActivity::class.java,false,bundle)
            }
        }
    }

    private fun initListener() {
        FDSMeshApi.instance.addFDSNodeStatusChangeCallBack(this)

        tv_refresh.setOnClickListener {
            //刷新设备在线状态
            val isOk = FDSMeshApi.instance.refreshFDSNodeInfoState()
            LOGUtils.v("refreshFDSNodeInfoState() =====> isOk:$isOk")
        }

        tv_add_dev.setOnClickListener {
            goActivity(AddDeviceActivity::class.java, false)
        }

        renameTextDialog.setOnDialogListener {
            if (fdsNodeInfo != null) {
                /*
                 * 重命名节点
                 * type == "", 则不修改类型
                 */
                FDSMeshApi.instance.renameFDSNodeInfo(fdsNodeInfo!!, it, "")
                studioDeviceAdapter?.update()
            }
        }

        stuDevBottomMenuDialog.setOnDialogListener {
            when (it) {
                StuDevBottomMenuDialog.MENU_DELETE -> {
                    //从Mesh中删除设备
                    if (fdsNodeInfo != null) {
                        loadingDialog.showDialog()
                        fdsAddOrRemoveDeviceApi?.deviceRemoveNetWork(
                            fdsNodeInfo!!,
                            true,
                            object : FDSRemoveNodeCallBack() {

                                /*
                                 * 删除设备完成回调
                                 * isAllSuccess 是否全部退网成功
                                 * fdsNodes 退网成功的节点列表
                                 */
                                override fun onComplete(
                                    isAllSuccess: Boolean,
                                    fdsNodes: MutableList<FDSNodeInfo>,
                                ) {
                                    LOGUtils.d("AddDeviceActivity isAllSuccess:$isAllSuccess size:${fdsNodes.size}")
                                    studioDeviceAdapter?.update()
                                    loadingDialog.dismissDialog()
                                }
                            })
                    }
                }
                StuDevBottomMenuDialog.MENU_RENAME -> {
                    if (fdsNodeInfo != null) {
                        renameTextDialog.setDefText(fdsNodeInfo!!.name)
                        renameTextDialog.showDialog()
                    }
                }
                StuDevBottomMenuDialog.MENU_BLE_UPGRADE -> {
                    if (fdsNodeInfo != null) {
                        meshOtaDialog.showDialog(fdsNodeInfo!!)
                    }
                }
                StuDevBottomMenuDialog.MENU_MCU_UPGRADE -> {
                    if (fdsNodeInfo != null) {
                        meshMcuUpgradeDialog.showDialog(fdsNodeInfo!!)
                    }
                }
            }
        }
    }

    override fun onNodeStatusChange(meshAddressList: MutableList<Int>) {
        //节点在线状态改变
        studioDeviceAdapter?.update(meshAddressList)

        for (meshAddress in meshAddressList) {
            val fdsNodeInfo = FDSMeshApi.instance.getFDSNodeInfoByMeshAddress(meshAddress)
            if (fdsNodeInfo != null) {
                LOGUtils.i("onNodeStatusChange() =========> " + "FDSNodeState:${fdsNodeInfo.getFDSNodeState()}  macAddress:${fdsNodeInfo.macAddress} ")
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        meshOtaDialog.dismiss()
        meshMcuUpgradeDialog.dismiss()
        fdsAddOrRemoveDeviceApi?.destroy()
        FDSMeshApi.instance.removeFDSNodeStatusChangeCallBack(this)
    }
}