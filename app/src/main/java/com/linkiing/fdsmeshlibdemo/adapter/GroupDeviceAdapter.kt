package com.linkiing.fdsmeshlibdemo.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.godox.sdk.api.FDSMeshApi
import com.godox.sdk.model.FDSNodeInfo
import com.linkiing.fdsmeshlibdemo.R
import com.linkiing.fdsmeshlibdemo.bean.FDSNodeBean

class GroupDeviceAdapter(private val groupAddress: Int) : RecyclerView.Adapter<GroupDeviceAdapter.MyHolder>() {
    private var devList = getFDSNodeList()
    private var isAllCheckListener: (Boolean) -> Unit = {}

    fun update(){
        devList = getFDSNodeList()
        notifyDataSetChanged()
    }

    fun setIsAllCheckListener(isAllCheckListener: (Boolean) -> Unit) {
        this.isAllCheckListener = isAllCheckListener
    }

    fun allCheck(isAllCheck: Boolean) {
        for (bean in devList) {
            bean.isChecked = isAllCheck
        }
        notifyDataSetChanged()
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
        val nodes = FDSMeshApi.instance.getGroupFDSNodes(groupAddress)
        for (node in nodes) {
            val nodeBean = FDSNodeBean(node)
            nodeBean.isChecked = false
            list.add(nodeBean)
        }
        return list
    }

    private fun isAllCheck(): Boolean{
        for (bean in devList) {
            if (!bean.isChecked) {
                return false
            }
        }
        return true
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_select_device_item, parent, false)
        return MyHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: MyHolder, position: Int) {
        val deviceBean = devList[position]
        holder.tv_name.text = deviceBean.fdsNodeInfo.name
        holder.tv_mac.text = deviceBean.fdsNodeInfo.macAddress

        if (deviceBean.isChecked) {
            holder.iv_check.setBackgroundResource(R.drawable.checked_image_on)
        } else {
            holder.iv_check.setBackgroundResource(R.drawable.checked_image_off)
        }

        holder.itemView.setOnClickListener {
            val isCheck = deviceBean.isChecked
            deviceBean.isChecked = !isCheck
            notifyItemChanged(position)

            isAllCheckListener(isAllCheck())
        }
    }

    override fun getItemCount(): Int {
        return devList.size
    }

    class MyHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tv_name = itemView.findViewById<TextView>(R.id.tv_name)
        val tv_mac = itemView.findViewById<TextView>(R.id.tv_mac)
        val iv_check = itemView.findViewById<ImageView>(R.id.iv_check)
    }
}