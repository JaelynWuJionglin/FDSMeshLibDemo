package com.linkiing.fdsmeshlibdemo.ui

import android.content.Intent
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.linkiing.fdsmeshlibdemo.R
import com.linkiing.fdsmeshlibdemo.adapter.AddDeviceInGroupAdapter
import com.linkiing.fdsmeshlibdemo.ui.base.BaseActivity
import kotlinx.android.synthetic.main.activity_add_device_in_group.*

/**
 * 添加设备到组
 */
class AddDeviceInGroupActivity : BaseActivity() {
    private var addDeviceInGroupAdapter: AddDeviceInGroupAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_device_in_group)

        initRecyclerView()
    }

    private fun initRecyclerView() {
        addDeviceInGroupAdapter = AddDeviceInGroupAdapter()
        val manager = LinearLayoutManager(this)
        manager.orientation = LinearLayoutManager.VERTICAL
        recyclerView_devices?.layoutManager = manager
        this.addDeviceInGroupAdapter.also { recyclerView_devices?.adapter = it }

        addDeviceInGroupAdapter?.setCheckListener {
            val intent = Intent()
            intent.putExtra("fdsNodeInfo",it)
            setResult(RESULT_OK, intent)
            finish()
        }
    }
}