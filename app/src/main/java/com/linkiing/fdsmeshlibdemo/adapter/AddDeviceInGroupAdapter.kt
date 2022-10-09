package com.linkiing.fdsmeshlibdemo.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.godox.sdk.api.FDSMeshApi
import com.godox.sdk.model.FDSNodeInfo
import com.linkiing.fdsmeshlibdemo.R
import com.linkiing.fdsmeshlibdemo.bean.FDSNodeBean

class AddDeviceInGroupAdapter : RecyclerView.Adapter<AddDeviceInGroupAdapter.MyHolder>() {
    private var devList = getFDSNodeList()
    private var checkListener: (FDSNodeInfo) -> Unit = {}

    fun update(){
        devList = getFDSNodeList()
        notifyDataSetChanged()
    }

    fun setCheckListener(checkListener: (FDSNodeInfo) -> Unit) {
        this.checkListener = checkListener
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_select_device_item, parent, false)
        return MyHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: MyHolder, position: Int) {
        val device = devList[position]
        holder.tv_name.text = device.fdsNodeInfo.deviceName
        holder.tv_mac.text = device.fdsNodeInfo.macAddress

        holder.itemView.setOnClickListener {
            checkListener(device.fdsNodeInfo)
        }
    }

    override fun getItemCount(): Int {
        return devList.size
    }

    class MyHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tv_name = itemView.findViewById<TextView>(R.id.tv_name)
        val tv_mac = itemView.findViewById<TextView>(R.id.tv_mac)
    }
}