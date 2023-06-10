package com.linkiing.fdsmeshlibdemo.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.LinearLayoutManager
import com.godox.sdk.api.FDSMeshApi
import com.godox.sdk.model.FDSGroupInfo
import com.godox.sdk.model.FDSNodeInfo
import com.linkiing.fdsmeshlibdemo.R
import com.linkiing.fdsmeshlibdemo.adapter.GroupDeviceAdapter
import com.linkiing.fdsmeshlibdemo.ui.base.BaseActivity
import com.linkiing.fdsmeshlibdemo.utils.ConstantUtils
import com.linkiing.fdsmeshlibdemo.view.dialog.LoadingDialog
import kotlinx.android.synthetic.main.activity_add_device_in_group.recyclerView_devices
import kotlinx.android.synthetic.main.activity_group.*
import kotlinx.android.synthetic.main.activity_group.iv_check

class GroupActivity : BaseActivity() {
    private lateinit var loadingDialog: LoadingDialog
    private var fdsGroupInfo: FDSGroupInfo? = null
    private var groupAdapter: GroupDeviceAdapter? = null
    private var groupAddress = 0
    private var addDevInGroupActivityLauncher: ActivityResultLauncher<Intent>? = null
    private var isAllCheck = false
    private var checkDeviceList = mutableListOf<FDSNodeInfo>()
    private var index = 0

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
            return
        }
        fdsGroupInfo = FDSMeshApi.instance.getGroupByAddress(groupAddress)
        if (fdsGroupInfo == null) {
            finish()
            return
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
            val checkDeviceList = groupAdapter?.getCheckDevices()
            if (checkDeviceList != null && checkDeviceList.isNotEmpty()) {
                startSubscribe(checkDeviceList, false)
            }
        }

        addDevInGroupActivityLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    val checkDeviceList = result.data?.getSerializableExtra("checkDeviceList")
                    if (checkDeviceList != null && checkDeviceList is MutableList<*>) {
                        startSubscribe(checkDeviceList as MutableList<FDSNodeInfo>, true)
                    }
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

    private fun startSubscribe(checkDeviceList: MutableList<FDSNodeInfo>, isSubscribe: Boolean) {
        if (fdsGroupInfo != null && checkDeviceList.isNotEmpty()) {
            loadingDialog.showDialog()
            this.checkDeviceList = checkDeviceList
            this.index = 0

            nextSubscribe(isSubscribe)
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

    /**
     * 设备订阅组
     */
    private fun nextSubscribe(isSubscribe: Boolean) {
        if (index >= checkDeviceList.size) {
            loadingDialog.dismissDialog()
            groupAdapter?.update()

            //一次执行完成
            ConstantUtils.toast(
                this,
                if (isSubscribe) {
                    "订阅完成！"
                } else {
                    "取消订阅完成！"
                }
            )
        } else {
            val fdsNodeInfo = checkDeviceList[index]

            /**
             * 同一个节点订阅组的上限是32个，超过32个便无法再订阅其他组。
             * 删除组的时候，务必要取消不必要的订阅关系。
             */
            FDSMeshApi.instance.configSubscribe(fdsNodeInfo, fdsGroupInfo!!, isSubscribe) {
                index++
                nextSubscribe(isSubscribe)
            }
        }
    }

}