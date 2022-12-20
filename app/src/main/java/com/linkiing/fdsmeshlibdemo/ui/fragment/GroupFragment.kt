package com.linkiing.fdsmeshlibdemo.ui.fragment

import android.os.Bundle
import android.text.TextUtils
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.godox.sdk.api.FDSMeshApi
import com.godox.sdk.model.FDSGroupInfo
import com.google.gson.Gson
import com.linkiing.fdsmeshlibdemo.R
import com.linkiing.fdsmeshlibdemo.adapter.StudioGroupAdapter
import com.linkiing.fdsmeshlibdemo.ui.GroupActivity
import com.linkiing.fdsmeshlibdemo.ui.ModeListActivity
import com.linkiing.fdsmeshlibdemo.ui.base.BaseFragment
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
    private var studioGroupAdapter: StudioGroupAdapter? = null
    private var fdsGroupInfo: FDSGroupInfo? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView()
        initRecyclerView()
        initListener()
    }

    override fun onResume() {
        super.onResume()
        studioGroupAdapter?.update()
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
        studioGroupAdapter = StudioGroupAdapter()
        val manager = LinearLayoutManager(mContext)
        manager.orientation = LinearLayoutManager.VERTICAL
        recyclerView_group?.layoutManager = manager
        recyclerView_group?.adapter = studioGroupAdapter

        studioGroupAdapter?.setItemLongClickListener {
            fdsGroupInfo = it
            stuGpBottomMenuDialog.showDialog()
        }

        studioGroupAdapter?.setItemClickListener {
            val bundle = Bundle()
            bundle.putInt("address", it.address)
            bundle.putString("typeName", it.name)
            goActivityBundle(ModeListActivity::class.java, false, bundle)
        }
    }

    private fun initListener() {
        tv_add_group?.setOnClickListener {
            createGroupTextDialog.setDefText("Group-${studioGroupAdapter?.itemCount?.plus(1)}")
            createGroupTextDialog.showDialog()
        }

        createGroupTextDialog.setOnDialogListener {
            val fdsGroupInfo = FDSMeshApi.instance.createGroup(it)
            LOGUtils.e("fdsGroupInfo =============> ${Gson().toJson(fdsGroupInfo)}")
            if (fdsGroupInfo != null) {
                studioGroupAdapter?.update()
            }
        }

        renameTextDialog.setOnDialogListener {
            if (TextUtils.isEmpty(it) && fdsGroupInfo != null) {
                FDSMeshApi.instance.renameGroup(fdsGroupInfo!!, it)
                studioGroupAdapter?.update(fdsGroupInfo!!.address)
            }
        }

        stuGpBottomMenuDialog.setOnDialogListener {
            when (it) {
                StuGpBottomMenuDialog.MENU_DELETE -> {
                    if (fdsGroupInfo != null) {

                        /**
                         * 同一个节点订阅组的上限是32个，超过32个便无法再订阅其他组。
                         * 删除组的时候，务必要取消不必要的订阅关系。
                         */
//                        if (FDSMeshApi.instance.getGroupFDSNodes(fdsGroupInfo!!.address).isEmpty()){
                            FDSMeshApi.instance.removeGroup(fdsGroupInfo!!)
                            studioGroupAdapter?.update()
//                        } else {
//                            ConstantUtils.toast(mActivity,"组内还有未取消订阅的设备！")
//                        }
                    }
                }
                StuGpBottomMenuDialog.MENU_RENAME -> {
                    if (fdsGroupInfo != null) {
                        renameTextDialog.setDefText(fdsGroupInfo?.name)
                        renameTextDialog.showDialog()
                    }
                }
                StuGpBottomMenuDialog.MENU_EDIT -> {
                    if (fdsGroupInfo != null) {
                        val bundle= Bundle()
                        bundle.putInt("address",fdsGroupInfo!!.address)
                        goActivityBundle(GroupActivity::class.java,false,bundle)
                    }
                }
            }
        }
    }
}