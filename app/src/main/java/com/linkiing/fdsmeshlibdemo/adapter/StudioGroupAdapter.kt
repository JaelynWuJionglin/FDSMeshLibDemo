package com.linkiing.fdsmeshlibdemo.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import androidx.recyclerview.widget.RecyclerView
import com.godox.agm.GodoxCommandApi
import com.godox.sdk.api.FDSMeshApi
import com.godox.sdk.model.FDSGroupInfo
import com.linkiing.fdsmeshlibdemo.R
import com.base.mesh.api.log.LOGUtils

class StudioGroupAdapter : RecyclerView.Adapter<StudioGroupAdapter.MyHolder>() {
    private var fdsGroupList = FDSMeshApi.instance.getGroups()
    private var itemLongClickListener: (FDSGroupInfo) -> Unit = {}
    private var itemClickListener: (FDSGroupInfo) -> Unit = {}

    fun update(){
        fdsGroupList = FDSMeshApi.instance.getGroups()
        LOGUtils.d("StudioGroupAdapter fdsGroupList.size:${fdsGroupList.size}")
        notifyDataSetChanged()
    }

    fun update(address: Int){
        for ((index, group) in fdsGroupList.withIndex()) {
            if (group.address == address) {
                notifyItemChanged(index)
            }
        }
    }

    fun setItemLongClickListener(itemLongClickListener: (FDSGroupInfo) -> Unit) {
        this.itemLongClickListener = itemLongClickListener
    }
    fun setItemClickListener(itemClickListener: (FDSGroupInfo) -> Unit) {
        this.itemClickListener = itemClickListener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_group_item, parent, false)
        return MyHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: MyHolder, position: Int) {
        val fdsGroupInfo = fdsGroupList[position]
        holder.tv_name.text = fdsGroupInfo.name
        holder.tv_msg.text = "${FDSMeshApi.instance.getGroupFDSNodes(fdsGroupInfo.address).size}个节点"

        //Switch
        holder.iv_switch.setOnCheckedChangeListener { _, b ->
            //设备开关灯
            GodoxCommandApi.instance.changeLightSwitch(fdsGroupInfo.address,b)
        }

        //长按事件
        holder.itemView.setOnLongClickListener {
            itemLongClickListener(fdsGroupInfo)
            false
        }

        //点击事件
        holder.itemView.setOnClickListener {
            itemClickListener(fdsGroupInfo)
        }
    }

    override fun getItemCount(): Int {
        return fdsGroupList.size
    }

    class MyHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val iv_light = itemView.findViewById<ImageView>(R.id.iv_light)
        val tv_name = itemView.findViewById<TextView>(R.id.tv_name)
        val tv_msg = itemView.findViewById<TextView>(R.id.tv_msg)
        val iv_switch = itemView.findViewById<SwitchCompat>(R.id.iv_switch)
    }
}