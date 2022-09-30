package com.linkiing.fdsmeshlibdemo.ui.fragment

import android.os.Bundle
import android.view.View
import android.widget.Toast
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
import com.linkiing.fdsmeshlibdemo.view.dialog.BottomMenuDialog
import com.linkiing.fdsmeshlibdemo.view.dialog.InputTextDialog
import com.linkiing.fdsmeshlibdemo.view.dialog.LoadingDialog
import com.telink.ble.mesh.util.LOGUtils
import kotlinx.android.synthetic.main.device_fragment.*

class DeviceFragment: BaseFragment(R.layout.device_fragment), NodeStatusChangeListener {
    private lateinit var bottomMenuDialog: BottomMenuDialog
    private lateinit var loadingDialog: LoadingDialog
    private lateinit var inputTextDialog: InputTextDialog
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
        MeshLogin.instance.autoConnect()
        studioDeviceAdapter?.update()
    }

    private fun initView() {
        loadingDialog = LoadingDialog(requireContext())
        bottomMenuDialog = BottomMenuDialog(mContext)

        inputTextDialog = InputTextDialog(mContext)
        inputTextDialog.setTitleText("重命名节点？")

        fdsAddOrRemoveDeviceApi = FDSAddOrRemoveDeviceApi(mActivity)
    }

    private fun initRecyclerView() {
        studioDeviceAdapter = StudioDeviceAdapter()
        val manager = LinearLayoutManager(mContext)
        manager.orientation = LinearLayoutManager.VERTICAL
        recyclerView_devices?.layoutManager = manager
        recyclerView_devices?.adapter = studioDeviceAdapter

        studioDeviceAdapter?.setItemLongClickListener {
            fdsNodeInfo = it
            bottomMenuDialog.showDialog()
        }

        studioDeviceAdapter?.setItemClickListener {
            if (it.getFDSNodeState() == FDSNodeInfo.ON_OFF_STATE_OFFLINE) {
                ConstantUtils.toast(mContext,getString(R.string.equipment_not_online_text))
            } else {
                val bundle= Bundle()
                bundle.putInt("address",it.meshAddress)
                bundle.putString("typeName",it.name);
                goActivityBundle(ModeListActivity::class.java,false,bundle)
            }

        }
    }

    private fun initListener() {
        FDSMeshApi.instance.addFDSNodeStatusChangeCallBack(this)

        tv_add_dev.setOnClickListener {
            goActivity(AddDeviceActivity::class.java, false)
        }

        inputTextDialog.setOnDialogListener {
            if (fdsNodeInfo != null) {
                /*
                 * 重名了节点
                 * type == "", 则不修改类型
                 */
                FDSMeshApi.instance.renameFDSNodeInfo(fdsNodeInfo!!, it, "")
                studioDeviceAdapter?.update()
            }
        }

        bottomMenuDialog.setOnDialogListener {
            when (it) {
                BottomMenuDialog.MENU_DELETE -> {
                    //从Mesh中删除设备
                    if (fdsNodeInfo != null) {
                        loadingDialog.showDialog()
                        fdsAddOrRemoveDeviceApi?.deviceRemoveNetWork(
                            fdsNodeInfo!!,
                            false,
                            object : FDSRemoveNodeCallBack {

                                /*
                                 * 删除设备完成回调
                                 * isAllSuccess 是否全部退网成功
                                 * fdsNodes 未退网成功的节点列表
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
                BottomMenuDialog.MENU_RENAME -> {
                    if (fdsNodeInfo != null) {
                        inputTextDialog.setDefText(fdsNodeInfo!!.name)
                        inputTextDialog.showDialog()
                    }
                }
                BottomMenuDialog.MENU_BLE_UPGRADE -> {
                    ConstantUtils.toast(mContext,"功能开发中，敬请期待！")
                }
                BottomMenuDialog.MENU_MCU_UPGRADE -> {
                    ConstantUtils.toast(mContext,"功能开发中，敬请期待！")
                }
            }
        }
    }

    override fun onNodeStatusChange(meshAddress: Int) {
        //节点在线状态改变
        LOGUtils.d("StudioActivity =====================> onNodeStatusChange()")
        studioDeviceAdapter?.update(meshAddress)
    }

    override fun onDestroy() {
        super.onDestroy()
        fdsAddOrRemoveDeviceApi?.destroy()
        FDSMeshApi.instance.removeFDSNodeStatusChangeCallBack(this)
    }
}