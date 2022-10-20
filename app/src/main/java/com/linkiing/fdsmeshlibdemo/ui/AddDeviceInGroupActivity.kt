package com.linkiing.fdsmeshlibdemo.ui

import android.content.Intent
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.linkiing.fdsmeshlibdemo.R
import com.linkiing.fdsmeshlibdemo.adapter.AddDeviceInGroupAdapter
import com.linkiing.fdsmeshlibdemo.ui.base.BaseActivity
import kotlinx.android.synthetic.main.activity_add_device_in_group.*
import java.io.Serializable

/**
 * 添加设备到组
 */
class AddDeviceInGroupActivity : BaseActivity() {
    private var addDeviceInGroupAdapter: AddDeviceInGroupAdapter? = null
    private var isAllCheck = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_device_in_group)

        initTitleBar()
        initRecyclerView()
        initListener()
    }

    private fun initTitleBar() {
        titleBar?.initTitleBar("选择设备","确定")
        titleBar?.setOnEndTextListener{
            val intent = Intent()
            intent.putExtra("checkDeviceList",addDeviceInGroupAdapter?.getCheckDevices() as Serializable)
            setResult(RESULT_OK, intent)
            finish()
        }
    }

    private fun initRecyclerView() {
        addDeviceInGroupAdapter = AddDeviceInGroupAdapter()
        val manager = LinearLayoutManager(this)
        manager.orientation = LinearLayoutManager.VERTICAL
        recyclerView_devices?.layoutManager = manager
        this.addDeviceInGroupAdapter.also { recyclerView_devices?.adapter = it }

        addDeviceInGroupAdapter?.setIsAllCheckListener {
            setCheck(it)
        }
    }

    private fun initListener() {
        iv_check.setOnClickListener {
            val isCheck = !isAllCheck
            setCheck(isCheck)
            addDeviceInGroupAdapter?.allCheck(isCheck)
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