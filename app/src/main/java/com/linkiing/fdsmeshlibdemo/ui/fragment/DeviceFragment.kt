package com.linkiing.fdsmeshlibdemo.ui.fragment

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.base.mesh.api.listener.NodeStatusChangeListener
import com.base.mesh.api.main.MeshLogin
import com.godox.sdk.api.FDSAddOrRemoveDeviceApi
import com.godox.sdk.api.FDSCommandApi
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
import java.util.*

class DeviceFragment : BaseFragment(R.layout.device_fragment), NodeStatusChangeListener {
    private lateinit var stuDevBottomMenuDialog: StuDevBottomMenuDialog
    private lateinit var loadingDialog: LoadingDialog
    private lateinit var renameTextDialog: InputTextDialog
    private lateinit var meshOtaDialog: MeshOtaDialog
    private lateinit var meshMcuUpgradeDialog: MeshOtaDialog
    private var studioDeviceAdapter: StudioDeviceAdapter? = null
    private var fdsAddOrRemoveDeviceApi: FDSAddOrRemoveDeviceApi? = null
    private var fdsNodeInfo: FDSNodeInfo? = null
    private var connectedFDSNodeInfo: FDSNodeInfo? = null
    private val mHandler = Handler(Looper.getMainLooper())
    private var isResetConnectDevice = false

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

        meshOtaDialog = MeshOtaDialog(mActivity, false)
        meshMcuUpgradeDialog = MeshOtaDialog(mActivity, true)
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
                ConstantUtils.toast(mContext, getString(R.string.equipment_not_online_text))
            } else {
                val bundle = Bundle()
                bundle.putInt("address", it.meshAddress)
                bundle.putString("typeName", it.name)
                goActivityBundle(ModeListActivity::class.java, false, bundle)
            }
        }
    }

    private fun initListener() {
        FDSMeshApi.instance.addFDSNodeStatusChangeCallBack(this)

        dev_switch.setOnCheckedChangeListener { compoundButton, isSwitch ->
            //设备全开全关
            if (!compoundButton.isPressed) {
                return@setOnCheckedChangeListener
            }
            FDSCommandApi.instance.changeLightSwitch(0xFFFF, isSwitch)
        }

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
                            fdsRemoveNodeCallBack)
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
                StuDevBottomMenuDialog.MENU_DELETE_ALL -> {
                    if (studioDeviceAdapter != null && studioDeviceAdapter!!.itemCount > 0) {
                        loadingDialog.showDialog()
                        fdsAddOrRemoveDeviceApi?.deviceRemoveNetWork(
                            studioDeviceAdapter!!.getAllFdsNodeList(),
                            true,
                            fdsRemoveNodeCallBack)
                    }
                }
            }
        }
    }

    private val fdsRemoveNodeCallBack = object : FDSRemoveNodeCallBack {
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
    }

    override fun onNodeStatusChange(meshAddressList: MutableList<Int>) {
        //节点在线状态改变
        studioDeviceAdapter?.update(meshAddressList)

        for (meshAddress in meshAddressList) {
            val fdsNodeInfo = FDSMeshApi.instance.getFDSNodeInfoByMeshAddress(meshAddress)
            if (fdsNodeInfo != null) {
                LOGUtils.i("onNodeStatusChange() =========> " + "FDSNodeState:${fdsNodeInfo.getFDSNodeState()}  macAddress:${fdsNodeInfo.macAddress} ")
                if (fdsNodeInfo.getFDSNodeState() == FDSNodeInfo.ON_OFF_STATE_OFFLINE) {
                    //设备离线
                    if (connectedFDSNodeInfo != null) {
                        if (connectedFDSNodeInfo!!.meshAddress == meshAddress) {
                            //直连节点离线
                            connectedFDSNodeInfo = null
                            isResetConnectDevice = false

                            //尝试等待连接高版本设备10s，10s连接不上则自动随机连接一个设备。
                            mHandler.postDelayed(stateOffLineRunnable, 10 * 1000)
                        }
                    }
                } else {
                    LOGUtils.v("==============> connectedFDSNodeInfo == null:${connectedFDSNodeInfo == null}  isResetConnectDevice:$isResetConnectDevice")
                    //设备上线
                    if (connectedFDSNodeInfo == null) {
                        connectedFDSNodeInfo = FDSMeshApi.instance.getConnectedFDSNodeInfo()
                    }
                    if (connectedFDSNodeInfo != null) {
                        LOGUtils.v(
                            "==============> " +
                                    "  connectedFDSNodeInfo!!.firmwareVersion:${connectedFDSNodeInfo!!.firmwareVersion}" +
                                    "  fdsNodeInfo.firmwareVersion:${fdsNodeInfo.firmwareVersion}"
                        )
                        if (connectedFDSNodeInfo!!.firmwareVersion < fdsNodeInfo.firmwareVersion) {
                            //直连节点版本小，切换直接节点
                            resetConnectDevice(fdsNodeInfo)
                            return
                        }
                    }
                }
            }
        }
    }

    private val stateOffLineRunnable = Runnable {
        //清除连接过滤，所有设备都可能成为直连节点
        MeshLogin.instance.clearAutoConnectFilterDevicesList()
    }

    /**
     * 切换直连节点
     */
    private fun resetConnectDevice(fdsNodeInfo: FDSNodeInfo) {
        if (!isResetConnectDevice) {
            isResetConnectDevice = true

            val list = arrayListOf<String>()
            for (device in FDSMeshApi.instance.getFDSNodes()) {
                if (device.meshAddress != fdsNodeInfo.meshAddress) {
                    list.add(device.macAddress)
                }
            }
            MeshLogin.instance.setAutoConnectFilterDevicesList(list)
            MeshLogin.instance.disconnect()
            MeshLogin.instance.autoConnect()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mHandler.removeCallbacks(stateOffLineRunnable)
        meshOtaDialog.dismiss()
        meshMcuUpgradeDialog.dismiss()
        fdsAddOrRemoveDeviceApi?.destroy()
        FDSMeshApi.instance.removeFDSNodeStatusChangeCallBack(this)
    }
}