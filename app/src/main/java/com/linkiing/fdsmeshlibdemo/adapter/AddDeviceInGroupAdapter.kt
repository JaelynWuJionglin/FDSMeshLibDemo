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

class AddDeviceInGroupAdapter : RecyclerView.Adapter<AddDeviceInGroupAdapter.MyHolder>() {
    private var devList = FDSMeshApi.instance.getFDSNodeWhitOutGroup()
    private var checkListener: (FDSNodeInfo) -> Unit = {}

    fun update(){
        devList = FDSMeshApi.instance.getFDSNodeWhitOutGroup()
        notifyDataSetChanged()
    }

    fun setCheckListener(checkListener: (FDSNodeInfo) -> Unit) {
        this.checkListener = checkListener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_add_device_in_group_item, parent, false)
        return MyHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: MyHolder, position: Int) {
        val fdsNodeInfo = devList[position]
        holder.tv_name.text = fdsNodeInfo.deviceName
        holder.tv_mac.text = fdsNodeInfo.macAddress

        holder.itemView.setOnClickListener {
            checkListener(fdsNodeInfo)
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