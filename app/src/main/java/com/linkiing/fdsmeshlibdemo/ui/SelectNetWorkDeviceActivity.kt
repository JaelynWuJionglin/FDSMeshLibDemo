package com.linkiing.fdsmeshlibdemo.ui

import android.content.Intent
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.linkiing.fdsmeshlibdemo.R
import com.linkiing.fdsmeshlibdemo.adapter.SelectNetWorkDeviceAdapter
import com.linkiing.fdsmeshlibdemo.ui.base.BaseActivity
import kotlinx.android.synthetic.main.activity_add_device_in_group.iv_check
import kotlinx.android.synthetic.main.activity_add_device_in_group.recyclerView_devices
import kotlinx.android.synthetic.main.activity_add_device_in_group.titleBar
import java.io.Serializable

/**
 * 添加设备到组
 */
class SelectNetWorkDeviceActivity : BaseActivity() {
    private var selectNetWorkDeviceAdapter: SelectNetWorkDeviceAdapter? = null
    private var isAllCheck = false
    private var isPa = -1 // -1: 无PA过滤项目， 0:非PA固件  1:PA固件

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_device_in_group)

        if (intent.hasExtra("isPA")) {
            isPa = intent.getIntExtra("isPA",0)
        }

        initTitleBar()
        initRecyclerView()
        initListener()
    }

    private fun initTitleBar() {
        titleBar?.initTitleBar("选择设备","确定")
        titleBar?.setOnEndTextListener{
            val intent = Intent()
            intent.putExtra("checkDeviceList",selectNetWorkDeviceAdapter?.getCheckDevices() as Serializable)
            setResult(RESULT_OK, intent)
            finish()
        }
    }

    private fun initRecyclerView() {
        selectNetWorkDeviceAdapter = SelectNetWorkDeviceAdapter(isPa)
        val manager = LinearLayoutManager(this)
        manager.orientation = LinearLayoutManager.VERTICAL
        recyclerView_devices?.layoutManager = manager
        this.selectNetWorkDeviceAdapter.also { recyclerView_devices?.adapter = it }

        selectNetWorkDeviceAdapter?.setIsAllCheckListener {
            setCheck(it)
        }
    }

    private fun initListener() {
        iv_check.setOnClickListener {
            val isCheck = !isAllCheck
            setCheck(isCheck)
            selectNetWorkDeviceAdapter?.allCheck(isCheck)
        }
    }

    private fun setCheck(isCheck: Boolean) {
        isAllCheck = isCheck
        if (isAllCheck) {
            iv_check.setBackgroundResource(R.drawable.checked_image_on)
        } else {
            iv_check.setBackgroundResource(R.drawable.checked_image_off)
        }
    }
}