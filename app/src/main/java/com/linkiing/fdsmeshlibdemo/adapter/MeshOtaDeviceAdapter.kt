package com.linkiing.fdsmeshlibdemo.adapter

import android.annotation.SuppressLint
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.godox.sdk.api.FDSMeshApi
import com.godox.sdk.model.FDSNodeInfo
import com.linkiing.fdsmeshlibdemo.R
import com.linkiing.fdsmeshlibdemo.bean.FDSNodeBean

class MeshOtaDeviceAdapter : RecyclerView.Adapter<MeshOtaDeviceAdapter.MyHolder>() {
    private val devList = mutableListOf<FDSNodeBean>()

    fun updateItem(list: MutableList<FDSNodeInfo>) {
        if (list.isEmpty()) {
            return
        }
        devList.clear()
        for (fdsNodeInfo in list) {
            devList.add(FDSNodeBean(fdsNodeInfo))
        }
        notifyDataSetChanged()
    }

    fun updateItemDef() {
        for (fdsNodeBean in devList) {
            fdsNodeBean.upgradeResults = FDSNodeBean.UPGRADE_OTA_IDLE
        }
        notifyDataSetChanged()
    }

    fun updateItem(meshAddress: Int, upgradeResults: Int) {
        for ((index, fdsNodeBean) in devList.withIndex()) {
            if (fdsNodeBean.fdsNodeInfo.meshAddress == meshAddress) {
                fdsNodeBean.upgradeResults = upgradeResults
                notifyItemChanged(index)
                return
            }
        }
    }

    fun updateItemOtherSusOrFail(isSus: Boolean) {
        for ((index, fdsNodeBean) in devList.withIndex()) {
            if (fdsNodeBean.upgradeResults == FDSNodeBean.UPGRADE_OTA_IDLE) {
                fdsNodeBean.upgradeResults = if (isSus) {
                    FDSNodeBean.UPGRADE_OTA_SUS
                } else {
                    FDSNodeBean.UPGRADE_OTA_FAIL
                }
                notifyItemChanged(index)
                return
            }
        }
    }

    fun getItemList(): MutableList<FDSNodeInfo> {
        val list = mutableListOf<FDSNodeInfo>()
        for (fdsNodeBean in devList) {
            list.add(fdsNodeBean.fdsNodeInfo)
        }
        return list
    }

    /**
     * 获取选中的设备列表
     */
    fun getCheckDevices(): MutableList<FDSNodeInfo> {
        val list = mutableListOf<FDSNodeInfo>()
        for (bean in devList) {
            if (bean.isChecked) {
                list.add(bean.fdsNodeInfo)
            }
        }
        return list
    }

    private fun getFDSNodeList(): MutableList<FDSNodeBean> {
        val list = arrayListOf<FDSNodeBean>()
        val nodes = FDSMeshApi.instance.getFDSNodeWhitOutGroup()
        for (node in nodes) {
            val nodeBean = FDSNodeBean(node)
            nodeBean.isChecked = false
            list.add(nodeBean)
        }
        return list
    }

    private fun isAllCheck(): Boolean {
        for (bean in devList) {
            if (!bean.isChecked) {
                return false
            }
        }
        return true
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_mesh_ota_device_item, parent, false)
        return MyHolder(view)
    }

    @SuppressLint("SetTextI18n", "MissingPermission")
    override fun onBindViewHolder(holder: MyHolder, position: Int) {
        val deviceBean = devList[position]
        val devName = if (TextUtils.isEmpty(deviceBean.fdsNodeInfo.name)) {
            "null"
        } else {
            deviceBean.fdsNodeInfo.name
        }

        holder.tv_name.text = "${devName}_${deviceBean.fdsNodeInfo.type}"
        holder.tv_mac.text = "mac:${deviceBean.fdsNodeInfo.macAddress}" +
                " - ver:${deviceBean.fdsNodeInfo.firmwareVersion.toString(16)}"

        when (deviceBean.upgradeResults) {
            FDSNodeBean.UPGRADE_OTA_SUS -> {
                holder.tv_up_result.text = "升级成功"
                holder.tv_up_result.setTextColor(
                    ContextCompat.getColor(
                        holder.itemView.context,
                        R.color.green
                    )
                )
            }

            FDSNodeBean.UPGRADE_OTA_FAIL -> {
                holder.tv_up_result.text = "升级失败"
                holder.tv_up_result.setTextColor(
                    ContextCompat.getColor(
                        holder.itemView.context,
                        R.color.red
                    )
                )
            }

            else -> {
                holder.tv_up_result.text = ""
                holder.tv_up_result.setTextColor(
                    ContextCompat.getColor(
                        holder.itemView.context,
                        R.color.grey
                    )
                )
            }
        }
    }

    override fun getItemCount(): Int {
        return devList.size
    }

    class MyHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tv_name = itemView.findViewById<TextView>(R.id.tv_name)
        val tv_mac = itemView.findViewById<TextView>(R.id.tv_mac)
        val tv_up_result = itemView.findViewById<TextView>(R.id.tv_up_result)
    }
}