package com.linkiing.fdsmeshlibdemo.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.godox.sdk.api.FDSMeshApi
import com.godox.sdk.model.FDSNodeInfo
import com.linkiing.fdsmeshlibdemo.R
import com.telink.ble.mesh.util.LOGUtils

class StudioDeviceAdapter : RecyclerView.Adapter<StudioDeviceAdapter.MyHolder>() {
    private var fdsNodeList = FDSMeshApi.instance.getFDSNodes()
    private var itemLongClickListener: (FDSNodeInfo) -> Unit = {}

    fun update(){
        fdsNodeList = FDSMeshApi.instance.getFDSNodeWhitOutGroup()
        LOGUtils.d("StudioDeviceAdapter fdsNodeList.size:${fdsNodeList.size}")
        notifyDataSetChanged()
    }

    fun update(meshAddress: Int){
        for ((index, dev) in fdsNodeList.withIndex()) {
            if (dev.meshAddress == meshAddress) {
                notifyItemChanged(index)
            }
        }
    }

    fun setItemLongClickListener(itemLongClickListener: (FDSNodeInfo) -> Unit) {
        this.itemLongClickListener = itemLongClickListener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_mesh_device_item, parent, false)
        return MyHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: MyHolder, position: Int) {
        val fdsNodeInfo = fdsNodeList[position]
        holder.tv_name.text = fdsNodeInfo.name
        holder.tv_mac.text = fdsNodeInfo.macAddress

        //在线状态
        if (fdsNodeInfo.getFDSNodeState() == FDSNodeInfo.ON_OFF_STATE_OFFLINE) {
            holder.iv_light.setBackgroundResource(R.drawable.light_off)
        } else {
            holder.iv_light.setBackgroundResource(R.drawable.light_on)
        }

        //开光状态
        holder.iv_switch.isChecked = fdsNodeInfo.getFDSNodeState() == FDSNodeInfo.ON_OFF_STATE_ON

        val connectedFDSNodeInfo = FDSMeshApi.instance.getConnectedFDSNodeInfo()
        if (connectedFDSNodeInfo != null){
            LOGUtils.e("StudioDeviceAdapter connectedFDSNodeInfo:$connectedFDSNodeInfo")
            if (connectedFDSNodeInfo.macAddress == fdsNodeInfo.macAddress){
                //直连设备
                holder.tv_name.setTextColor(ContextCompat.getColor(holder.itemView.context,R.color.red))
                holder.tv_mac.setTextColor(ContextCompat.getColor(holder.itemView.context,R.color.red))
            } else {
                holder.tv_name.setTextColor(ContextCompat.getColor(holder.itemView.context,R.color.black))
                holder.tv_mac.setTextColor(ContextCompat.getColor(holder.itemView.context,R.color.black))
            }
        } else {
            LOGUtils.e("StudioDeviceAdapter connectedFDSNodeInfo == null")
        }

        //Switch
        holder.iv_switch.setOnCheckedChangeListener { compoundButton, b ->

        }

        //长按事件
        holder.itemView.setOnLongClickListener {
            itemLongClickListener(fdsNodeInfo)
            false
        }
    }

    override fun getItemCount(): Int {
        return fdsNodeList.size
    }

    class MyHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val iv_light = itemView.findViewById<ImageView>(R.id.iv_light)
        val tv_name = itemView.findViewById<TextView>(R.id.tv_name)
        val tv_mac = itemView.findViewById<TextView>(R.id.tv_mac)
        val iv_switch = itemView.findViewById<SwitchCompat>(R.id.iv_switch)
    }
}