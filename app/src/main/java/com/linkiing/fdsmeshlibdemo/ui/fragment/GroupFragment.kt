package com.linkiing.fdsmeshlibdemo.ui.fragment

import android.os.Bundle
import android.text.TextUtils
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.godox.sdk.api.FDSMeshApi
import com.godox.sdk.model.FDSGroupInfo
import com.linkiing.fdsmeshlibdemo.R
import com.linkiing.fdsmeshlibdemo.adapter.StudioGroupAdapter
import com.linkiing.fdsmeshlibdemo.ui.ModeListActivity
import com.linkiing.fdsmeshlibdemo.ui.base.BaseFragment
import com.linkiing.fdsmeshlibdemo.view.dialog.InputTextDialog
import com.linkiing.fdsmeshlibdemo.view.dialog.StuGpBottomMenuDialog
import kotlinx.android.synthetic.main.group_fragment.*

class GroupFragment: BaseFragment(R.layout.group_fragment) {
    private lateinit var stuGpBottomMenuDialog: StuGpBottomMenuDialog
    private lateinit var createGroupTextDialog: InputTextDialog
    private lateinit var renameTextDialog: InputTextDialog
    private var groupAdapter: StudioGroupAdapter? = null
    private var fdsGroupInfo: FDSGroupInfo? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView()
        initRecyclerView()
        initListener()
    }

    private fun initView() {
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
            val bundle= Bundle()
            bundle.putInt("address",it.address)
            bundle.putString("typeName",it.name)
            goActivityBundle(ModeListActivity::class.java,false,bundle)
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
                FDSMeshApi.instance.renameGroup(fdsGroupInfo!!,it)
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

                }
            }
        }
    }
}