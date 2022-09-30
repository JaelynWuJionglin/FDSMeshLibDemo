package com.linkiing.fdsmeshlibdemo.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.FragmentActivity
import com.base.mesh.api.main.MeshLogin
import com.godox.sdk.api.FDSMeshApi
import com.linkiing.fdsmeshlibdemo.R
import com.linkiing.fdsmeshlibdemo.mmkv.MMKVSp
import com.linkiing.fdsmeshlibdemo.ui.base.BaseFragment
import com.linkiing.fdsmeshlibdemo.ui.fragment.DeviceFragment
import com.linkiing.fdsmeshlibdemo.ui.fragment.GroupFragment
import com.telink.ble.mesh.util.LOGUtils
import kotlinx.android.synthetic.main.activity_studio.*

class StudioActivity : FragmentActivity(), View.OnClickListener {
    private val deviceFragment = DeviceFragment()
    private val groupFragment = GroupFragment()
    private var nowFragment:BaseFragment? = null
    private var tabId: Int = -1
    private var index = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_studio)

        initView()
        initListener()
    }

    override fun onResume() {
        super.onResume()
        MeshLogin.instance.autoConnect()
    }

    private fun initView() {
        index = intent.getIntExtra("index", 0)
        if (index == 0) {
            finish()
        }

        setTab(0)
    }

    private fun initListener() {
        tab_devices.setOnClickListener(this)
        tab_group.setOnClickListener(this)
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

    private fun showFragment(fragment:BaseFragment){
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        //先hide之前的fragment
        if (nowFragment != null) {
            if (nowFragment!!.isAdded && nowFragment!!.isVisible){
                fragmentTransaction.hide(nowFragment!!)
            }
        }

        //添加新的fragment
        if (!fragment.isAdded){
            fragmentTransaction.add(R.id.frameLayout,fragment)
        }else{
            fragmentTransaction.show(fragment)
        }
        nowFragment = fragment
        fragmentTransaction.commit()
    }


    override fun finish() {
        super.finish()

        if (index != 0){
            //保存当前MeshJson数据
            val meshJsonStr = FDSMeshApi.instance.getCurrentMeshJson()
            val studioList = MMKVSp.instance.getStudioList()
            for (bean in studioList) {
                if (bean.index == index) {
                    bean.meshJsonStr = meshJsonStr
                }
            }
            MMKVSp.instance.setStudioList(studioList)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        MeshLogin.instance.disconnect()
    }
}