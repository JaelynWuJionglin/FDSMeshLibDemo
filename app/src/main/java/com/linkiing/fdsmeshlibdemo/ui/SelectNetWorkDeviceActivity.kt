package com.linkiing.fdsmeshlibdemo.ui

import android.content.Intent
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.linkiing.fdsmeshlibdemo.R
import com.linkiing.fdsmeshlibdemo.databinding.ActivityAddDeviceInGroupBinding
import com.linkiing.fdsmeshlibdemo.adapter.SelectNetWorkDeviceAdapter
import com.linkiing.fdsmeshlibdemo.ui.base.BaseActivity
import java.io.Serializable

/**
 * 添加设备到组
 */
class SelectNetWorkDeviceActivity : BaseActivity<ActivityAddDeviceInGroupBinding>() {
    private var selectNetWorkDeviceAdapter: SelectNetWorkDeviceAdapter? = null
    private var isAllCheck = false
    private var paValue = -1 // -1: 无PA过滤项目， 0:非PA固件  1:PA固件 3:类型03

    override fun initBind(): ActivityAddDeviceInGroupBinding {
        return ActivityAddDeviceInGroupBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (intent.hasExtra("paValue")) {
            paValue = intent.getIntExtra("paValue",0)
        }

        initTitleBar()
        initRecyclerView()
        initListener()
    }

    private fun initTitleBar() {
        binding.titleBar.initTitleBar("选择设备","确定")
        binding.titleBar.setOnEndTextListener{
            val intent = Intent()
            intent.putExtra("checkDeviceList",selectNetWorkDeviceAdapter?.getCheckDevices() as Serializable)
            setResult(RESULT_OK, intent)
            finish()
        }
    }

    private fun initRecyclerView() {
        selectNetWorkDeviceAdapter = SelectNetWorkDeviceAdapter(paValue)
        val manager = LinearLayoutManager(this)
        manager.orientation = LinearLayoutManager.VERTICAL
        binding.recyclerViewDevices.layoutManager = manager
        this.selectNetWorkDeviceAdapter.also { binding.recyclerViewDevices.adapter = it }

        selectNetWorkDeviceAdapter?.setIsAllCheckListener {
            setCheck(it)
        }
    }

    private fun initListener() {
        binding.ivCheck.setOnClickListener {
            val isCheck = !isAllCheck
            setCheck(isCheck)
            selectNetWorkDeviceAdapter?.allCheck(isCheck)
        }
    }

    private fun setCheck(isCheck: Boolean) {
        isAllCheck = isCheck
        if (isAllCheck) {
            binding.ivCheck.setBackgroundResource(R.drawable.checked_image_on)
        } else {
            binding.ivCheck.setBackgroundResource(R.drawable.checked_image_off)
        }
    }
}