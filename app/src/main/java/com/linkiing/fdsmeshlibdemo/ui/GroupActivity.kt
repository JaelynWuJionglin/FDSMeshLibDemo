package com.linkiing.fdsmeshlibdemo.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.LinearLayoutManager
import com.base.mesh.api.log.LOGUtils
import com.godox.sdk.api.FDSMeshApi
import com.godox.sdk.model.FDSGroupInfo
import com.godox.sdk.model.FDSNodeInfo
import com.linkiing.fdsmeshlibdemo.R
import com.linkiing.fdsmeshlibdemo.databinding.ActivityGroupBinding
import com.linkiing.fdsmeshlibdemo.adapter.GroupDeviceAdapter
import com.linkiing.fdsmeshlibdemo.ui.base.BaseActivity
import com.linkiing.fdsmeshlibdemo.utils.ConstantUtils
import com.linkiing.fdsmeshlibdemo.view.dialog.LoadingDialog
class GroupActivity : BaseActivity<ActivityGroupBinding>() {
    private lateinit var loadingDialog: LoadingDialog
    private var fdsGroupInfo: FDSGroupInfo? = null
    private var groupAdapter: GroupDeviceAdapter? = null
    private var groupAddress = 0
    private var addDevInGroupActivityLauncher: ActivityResultLauncher<Intent>? = null
    private var isAllCheck = false
    private var checkDeviceList = mutableListOf<FDSNodeInfo>()
    private var subIndex = 0
    private var index = 0

    override fun initBind(): ActivityGroupBinding {
        return ActivityGroupBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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

        index = intent.getIntExtra("index", 0)
        LOGUtils.d("StudioActivity =============> index:$index")
        if (index == 0) {
            finish()
        }

        loadingDialog = LoadingDialog(this)
    }

    private fun initRecyclerView() {
        groupAdapter = GroupDeviceAdapter(groupAddress)
        val manager = LinearLayoutManager(this)
        manager.orientation = LinearLayoutManager.VERTICAL
        binding.recyclerViewDevices.layoutManager = manager
        this.groupAdapter.also { binding.recyclerViewDevices.adapter = it }

        groupAdapter?.setIsAllCheckListener {
            setCheck(it)
        }
    }

    private fun initListener() {
        binding.ivCheck.setOnClickListener {
            val isCheck = !isAllCheck
            setCheck(isCheck)
            groupAdapter?.allCheck(isCheck)
        }

        binding.btAddDevice.setOnClickListener {
            addDevInGroupActivityLauncher?.launch(
                Intent(
                    this,
                    SelectNetWorkDeviceActivity::class.java
                )
            )
        }

        binding.btRemoveDevice.setOnClickListener {
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
            binding.ivCheck.setBackgroundResource(R.drawable.checked_image_on)
        } else {
            binding.ivCheck.setBackgroundResource(R.drawable.checked_image_off)
        }
    }

    private fun startSubscribe(checkDeviceList: MutableList<FDSNodeInfo>, isSubscribe: Boolean) {
        if (fdsGroupInfo != null && checkDeviceList.isNotEmpty()) {
            loadingDialog.showDialog()
            this.checkDeviceList = checkDeviceList
            this.subIndex = 0

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
        if (subIndex >= checkDeviceList.size) {
            loadingDialog.dismissDialog()
            setCheck(false)

            runOnUiThread {
                groupAdapter?.update()
            }

            //保存json修改
            ConstantUtils.saveJson(index)

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
            val fdsNodeInfo = checkDeviceList[subIndex]
            if (fdsNodeInfo.getFDSNodeState() == FDSNodeInfo.ON_OFF_STATE_OFFLINE){
                //离线设备不可进行订阅操作
                LOGUtils.e("Error! nextSubscribe 设备离线 ==> MAC:${fdsNodeInfo.macAddress}")
                subIndex++
                nextSubscribe(isSubscribe)
            } else {
                /**
                 * 同一个节点订阅组的上限是32个，超过32个便无法再订阅其他组。
                 * 删除组的时候，务必要取消不必要的订阅关系。
                 */
                FDSMeshApi.instance.configSubscribe(fdsNodeInfo, fdsGroupInfo!!, isSubscribe) {
                    LOGUtils.d("nextSubscribe 订阅结果 ==>Mac:${fdsNodeInfo.macAddress} GroupAddress${fdsGroupInfo?.address}  it:$it")
                    subIndex++
                    nextSubscribe(isSubscribe)
                }
            }
        }
    }

}