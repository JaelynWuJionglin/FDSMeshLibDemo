package com.linkiing.fdsmeshlibdemo.ui.fragment

import android.app.Activity
import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.LinearLayoutManager
import com.godox.sdk.api.FDSMeshApi
import com.godox.sdk.model.FDSGroupInfo
import com.godox.sdk.model.FDSNodeInfo
import com.google.gson.Gson
import com.linkiing.fdsmeshlibdemo.R
import com.linkiing.fdsmeshlibdemo.adapter.StudioGroupAdapter
import com.linkiing.fdsmeshlibdemo.ui.AddDeviceInGroupActivity
import com.linkiing.fdsmeshlibdemo.ui.ModeListActivity
import com.linkiing.fdsmeshlibdemo.ui.base.BaseFragment
import com.linkiing.fdsmeshlibdemo.utils.ConstantUtils
import com.linkiing.fdsmeshlibdemo.view.dialog.InputTextDialog
import com.linkiing.fdsmeshlibdemo.view.dialog.LoadingDialog
import com.linkiing.fdsmeshlibdemo.view.dialog.StuGpBottomMenuDialog
import com.telink.ble.mesh.util.LOGUtils
import kotlinx.android.synthetic.main.group_fragment.*

class GroupFragment : BaseFragment(R.layout.group_fragment) {
    private lateinit var loadingDialog: LoadingDialog
    private lateinit var stuGpBottomMenuDialog: StuGpBottomMenuDialog
    private lateinit var createGroupTextDialog: InputTextDialog
    private lateinit var renameTextDialog: InputTextDialog
    private var groupAdapter: StudioGroupAdapter? = null
    private var fdsGroupInfo: FDSGroupInfo? = null
    private var addDevInGroupActivityLauncher: ActivityResultLauncher<Intent>? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView()
        initRecyclerView()
        initListener()
    }

    private fun initView() {
        loadingDialog = LoadingDialog(requireContext())
        stuGpBottomMenuDialog = StuGpBottomMenuDialog(mContext)

        createGroupTextDialog = InputTextDialog(mContext)
        createGroupTextDialog.setTitleText("新增组别？")

        renameTextDialog = InputTextDialog(mContext)
        renameTextDialog.setTitleText("重名了组别？")
    }

    private fun initRecyclerView() {
        groupAdapter = StudioGroupAdapter()
        val manager = LinearLayoutManager(mContext)
        manager.orientation = LinearLayoutManager.VERTICAL
        recyclerView_group?.layoutManager = manager
        recyclerView_group?.adapter = groupAdapter

        groupAdapter?.setItemLongClickListener {
            fdsGroupInfo = it
            stuGpBottomMenuDialog.showDialog()
        }

        groupAdapter?.setItemClickListener {
            val bundle = Bundle()
            bundle.putInt("address", it.address)
            bundle.putString("typeName", it.name)
            goActivityBundle(ModeListActivity::class.java, false, bundle)
        }
    }

    private fun initListener() {
        tv_add_group?.setOnClickListener {
            createGroupTextDialog.setDefText("Group-${groupAdapter?.itemCount?.plus(1)}")
            createGroupTextDialog.showDialog()
        }

        createGroupTextDialog.setOnDialogListener {
            val fdsGroupInfo = FDSMeshApi.instance.createGroup(it)
            if (fdsGroupInfo != null) {
                groupAdapter?.update()
            }
        }

        renameTextDialog.setOnDialogListener {
            if (TextUtils.isEmpty(it) && fdsGroupInfo != null) {
                FDSMeshApi.instance.renameGroup(fdsGroupInfo!!, it)
                groupAdapter?.update(fdsGroupInfo!!.address)
            }
        }

        stuGpBottomMenuDialog.setOnDialogListener {
            when (it) {
                StuGpBottomMenuDialog.MENU_DELETE -> {
                    if (fdsGroupInfo != null) {
                        FDSMeshApi.instance.removeGroup(fdsGroupInfo!!)
                        groupAdapter?.update()
                    }
                }
                StuGpBottomMenuDialog.MENU_RENAME -> {
                    if (fdsGroupInfo != null) {
                        renameTextDialog.setDefText(fdsGroupInfo?.name)
                        renameTextDialog.showDialog()
                    }
                }
                StuGpBottomMenuDialog.MENU_EDIT -> {
                    addDevInGroupActivityLauncher?.launch(Intent(context, AddDeviceInGroupActivity::class.java))
                }
            }
        }

        addDevInGroupActivityLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK){
                val fdsNodeInfo = result.data?.getSerializableExtra("fdsNodeInfo") as FDSNodeInfo
                if (fdsGroupInfo != null) {
                    loadingDialog.showDialog()

                    LOGUtils.i("=======> fdsNodeInfo:${Gson().toJson(fdsNodeInfo)}")

                    /*
                     * 设备订阅组
                     */
                    FDSMeshApi.instance.configSubscribe(fdsNodeInfo, fdsGroupInfo!!, true) {
                        loadingDialog.dismissDialog()
                        groupAdapter?.update()

                        ConstantUtils.toast(
                            mContext, if (it) {
                                "订阅成功！"
                            } else {
                                "订阅失败！"
                            }
                        )
                    }
                } else {
                    ConstantUtils.toast(mContext,"添加失败！")
                }
            }
        }
    }
}