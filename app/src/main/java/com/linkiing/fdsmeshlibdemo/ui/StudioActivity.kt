package com.linkiing.fdsmeshlibdemo.ui

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsetsController
import androidx.fragment.app.FragmentActivity
import com.base.mesh.api.listener.MeshLoginListener
import com.base.mesh.api.log.LOGUtils
import com.base.mesh.api.main.MeshLogin
import com.godox.sdk.api.FDSMeshApi
import com.linkiing.fdsmeshlibdemo.R
import com.linkiing.fdsmeshlibdemo.mmkv.MMKVSp
import com.linkiing.fdsmeshlibdemo.ui.base.BaseFragment
import com.linkiing.fdsmeshlibdemo.ui.fragment.DeviceFragment
import com.linkiing.fdsmeshlibdemo.ui.fragment.GroupFragment
import com.linkiing.fdsmeshlibdemo.view.dialog.StuDevBottomMenuDialog
import com.linkiing.fdsmeshlibdemo.view.dialog.StuPaBottomMenuDialog
import com.telink.ble.mesh.core.networking.ExtendBearerMode
import kotlinx.android.synthetic.main.activity_studio.tab_devices
import kotlinx.android.synthetic.main.activity_studio.tab_group
import kotlinx.android.synthetic.main.activity_studio.titleBar

class StudioActivity : FragmentActivity(), View.OnClickListener, MeshLoginListener {
    private lateinit var stuPaBottomMenuDialog: StuPaBottomMenuDialog
    private val deviceFragment = DeviceFragment()
    private val groupFragment = GroupFragment()
    private var nowFragment: BaseFragment? = null
    private var tabId: Int = -1
    private var index = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_studio)

        initView()
        initListener()

        MeshLogin.instance.addLoginListener(this)
    }

    override fun onStart() {
        super.onStart()

        // 设置状态栏文字颜色为暗色
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window?.insetsController?.setSystemBarsAppearance(
                WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
                WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window?.decorView?.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
        }
    }

    override fun onResume() {
        super.onResume()
        LOGUtils.v("StudioActivity onResume()")
//        if (MMKVSp.instance.isTestModel()) {
        MeshLogin.instance.autoConnect()
//        } else {
//            connectHighVersionDevice()
//        }

        resetExtendBearerMode()
    }

    override fun onMeshDisconnect() {
        super.onMeshDisconnect()

        if (!FDSMeshApi.instance.isMcuOtaIng()) {
            //Mesh网络断开，重新连接
            MeshLogin.instance.autoConnect()
        }

        if(MMKVSp.instance.isTestModel()) {
            deviceFragment.updateList()
        }
    }

    override fun onMeshConnected() {
        super.onMeshConnected()
        //Mesh连接成功

        resetExtendBearerMode()

        if(MMKVSp.instance.isTestModel()) {
            deviceFragment.updateList()
        }
    }

    private fun initView() {
        titleBar?.initTitleBar("","MeshOta")
        index = intent.getIntExtra("index", 0)
        LOGUtils.d("StudioActivity =============> index:$index")
        if (index == 0) {
            finish()
        }

        stuPaBottomMenuDialog = StuPaBottomMenuDialog(this)

        deviceFragment.setIndex(index)
        groupFragment.setIndex(index)

        setTab(0)
    }

    private fun initListener() {
        tab_devices.setOnClickListener(this)
        tab_group.setOnClickListener(this)

        titleBar?.setOnEndTextListener{
            stuPaBottomMenuDialog.showDialog()
        }

        stuPaBottomMenuDialog.setOnDialogListener {
            when(it) {
                StuPaBottomMenuDialog.MENU_NOT_PA -> {
                    val intent = Intent(this, MeshOtaActivity::class.java)
                    intent.putExtra("isPA",0)
                    startActivity(intent)
                }

                StuPaBottomMenuDialog.MENU_PA -> {
                    val intent = Intent(this, MeshOtaActivity::class.java)
                    intent.putExtra("isPA",1)
                    startActivity(intent)
                }
            }
        }
    }

    private fun setTab(id: Int) {
        if (id == tabId) {
            return
        }
        this.tabId = id
        tab_devices.setCk(id == 0)
        tab_group.setCk(id == 1)
        if (id == 0) {
            titleBar?.setTitle("节点")
            showFragment(deviceFragment)
        } else if (id == 1) {
            titleBar?.setTitle("组别")
            showFragment(groupFragment)
        }
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.tab_devices -> {
                setTab(0)
            }

            R.id.tab_group -> {
                setTab(1)
            }
        }
    }

    private fun showFragment(fragment: BaseFragment) {
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        //先hide之前的fragment
        if (nowFragment != null) {
            if (nowFragment!!.isAdded && nowFragment!!.isVisible) {
                fragmentTransaction.hide(nowFragment!!)
            }
        }

        //添加新的fragment
        if (!fragment.isAdded) {
            fragmentTransaction.add(R.id.frameLayout, fragment)
        } else {
            fragmentTransaction.show(fragment)
        }
        nowFragment = fragment

        fragmentTransaction.commit()
    }

    private fun resetExtendBearerMode() {
        if (MMKVSp.instance.isTestModel()) {
            return
        }
        val node = FDSMeshApi.instance.getConnectedFDSNodeInfo()
        if (node != null) {
            if (node.firmwareVersion >= 41) {
                /*
                 * 设置Mesh发送数据包承载模式（需固件支持）
                 * NONE:默认都不使用长包
                 * GATT:直连节点长包
                 * GATT_ADV:全部长包
                 */
                FDSMeshApi.instance.resetExtendBearerMode(ExtendBearerMode.GATT_ADV)
            } else {
                FDSMeshApi.instance.resetExtendBearerMode(ExtendBearerMode.NONE)
            }
        }
    }

    /**
     * 连接高版本设备节点，如果超时连接不成功，则自动连接一个随机节点。
     */
    private fun connectHighVersionDevice() {
        val deviceHighVersion = getDeviceHighVersion()
        if (MeshLogin.instance.isLogin()) {
            val connectedDevice = FDSMeshApi.instance.getConnectedFDSNodeInfo()
            if (connectedDevice != null) {
                if (connectedDevice.firmwareVersion >= deviceHighVersion) {
                    //已经连接高版本设备
                    return
                }
            }
        }
        val list = arrayListOf<String>()
        for (device in FDSMeshApi.instance.getFDSNodes()) {
            if (device.firmwareVersion < deviceHighVersion) {
                list.add(device.macAddress)
            }
        }
        MeshLogin.instance.setAutoConnectFilterDevicesList(list)
        MeshLogin.instance.disconnect()
        MeshLogin.instance.autoConnect(10 * 1000) {
            LOGUtils.d("autoConnect =============================> $it")
            //尝试连接高版本设备10s，10s连接不上则自动随机连接一个设备。
            if (!it) {
                //清除连接过滤，所有设备都可能成为直连节点
                MeshLogin.instance.clearAutoConnectFilterDevicesList()
            }
        }
    }

    /**
     * 获取节点中设备的最高版本
     */
    private fun getDeviceHighVersion(): Int {
        var version = 0
        for (device in FDSMeshApi.instance.getFDSNodes()) {
            if (device.firmwareVersion > version) {
                version = device.firmwareVersion
            }
        }
        return version
    }

    override fun onDestroy() {
        super.onDestroy()
        MeshLogin.instance.removeLoginListener(this)
        MeshLogin.instance.disconnect()
    }
}