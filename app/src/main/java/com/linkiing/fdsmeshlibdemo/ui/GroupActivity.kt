package com.linkiing.fdsmeshlibdemo.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.LinearLayoutManager
import com.godox.sdk.api.FDSMeshApi
import com.godox.sdk.model.FDSGroupInfo
import com.godox.sdk.model.FDSNodeInfo
import com.google.gson.Gson
import com.linkiing.fdsmeshlibdemo.R
import com.linkiing.fdsmeshlibdemo.adapter.GroupDeviceAdapter
import com.linkiing.fdsmeshlibdemo.ui.base.BaseActivity
import com.linkiing.fdsmeshlibdemo.utils.ConstantUtils
import com.linkiing.fdsmeshlibdemo.view.dialog.LoadingDialog
import com.telink.ble.mesh.util.LOGUtils
import kotlinx.android.synthetic.main.activity_add_device.*
import kotlinx.android.synthetic.main.activity_add_device_in_group.*
import kotlinx.android.synthetic.main.activity_add_device_in_group.recyclerView_devices
import kotlinx.android.synthetic.main.activity_group.*
import kotlinx.android.synthetic.main.activity_group.bt_add_device
import kotlinx.android.synthetic.main.activity_group.iv_check

class GroupActivity : BaseActivity() {
    private lateinit var loadingDialog: LoadingDialog
    private var groupAdapter: GroupDeviceAdapter? = null
    private var groupAddress = 0
    private var addDevInGroupActivityLauncher: ActivityResultLauncher<Intent>? = null
    private var isAllCheck = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_group)

        initView()
        initRecyclerView()
        initListener()
    }

    private fun initView() {
        groupAddress = intent.getIntExtra("address", 0)
        if (groupAddress == 0) {
            finish()
        }

        loadingDialog = LoadingDialog(this)
    }

    private fun initRecyclerView() {
        groupAdapter = GroupDeviceAdapter(groupAddress)
        val manager = LinearLayoutManager(this)
        manager.orientation = LinearLayoutManager.VERTICAL
        recyclerView_devices?.layoutManager = manager
        this.groupAdapter.also { recyclerView_devices?.adapter = it }

        groupAdapter?.setIsAllCheckListener {
            setCheck(it)
        }
    }

    private fun initListener() {
        iv_check.setOnClickListener {
            val isCheck = !isAllCheck
            setCheck(isCheck)
            groupAdapter?.allCheck(isCheck)
        }

        bt_add_device.setOnClickListener {
            addDevInGroupActivityLauncher?.launch(
                Intent(
                    this,
                    AddDeviceInGroupActivity::class.java
                )
            )
        }

        bt_remove_device.setOnClickListener {
            val fdsNodes = groupAdapter?.getCheckDevices()
            if (fdsNodes != null && fdsNodes.isNotEmpty()) {
                val fdsNodeInfo = fdsNodes[0]
                setSubscribe(fdsNodeInfo,false)
            }
        }

        addDevInGroupActivityLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    val fdsNodeInfo =
                        result.data?.getSerializableExtra("fdsNodeInfo") as FDSNodeInfo
                    setSubscribe(fdsNodeInfo, true)
                }
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

    private fun setSubscribe(fdsNodeInfo: FDSNodeInfo, isSubscribe: Boolean) {
        val fdsGroupInfo = FDSMeshApi.instance.getGroupByAddress(groupAddress)
        if (fdsGroupInfo != null) {
            loadingDialog.showDialog()

            LOGUtils.i("=======> fdsNodeInfo:${Gson().toJson(fdsNodeInfo)}")

            /*
             * 设备订阅组
             */
            FDSMeshApi.instance.configSubscribe(fdsNodeInfo, fdsGroupInfo, isSubscribe) {
                loadingDialog.dismissDialog()
                groupAdapter?.update()

                ConstantUtils.toast(
                    this, if (it) {
                        if (isSubscribe) {
                            "订阅成功！"
                        } else {
                            "取消订阅成功！"
                        }
                    } else {
                        if (isSubscribe) {
                            "订阅失败！"
                        } else {
                            "取消订阅失败！"
                        }
                    }
                )
            }
        } else {
            ConstantUtils.toast(
                this, if (isSubscribe) {
                    "添加失败！"
                } else {
                    "移除失败！"
                }
            )
        }
    }
}