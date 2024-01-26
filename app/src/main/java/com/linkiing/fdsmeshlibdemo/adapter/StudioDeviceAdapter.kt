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
import com.godox.agm.GodoxCommandApi
import com.godox.sdk.api.FDSMeshApi
import com.godox.sdk.model.FDSNodeInfo
import com.google.gson.Gson
import com.linkiing.fdsmeshlibdemo.R
import com.base.mesh.api.log.LOGUtils

class StudioDeviceAdapter : RecyclerView.Adapter<StudioDeviceAdapter.MyHolder>() {
    private var fdsNodeList = FDSMeshApi.instance.getFDSNodes()
    private var itemLongClickListener: (FDSNodeInfo) -> Unit = {}
    private var itemClickListener: (FDSNodeInfo) -> Unit = {}

    fun update() {
        fdsNodeList = FDSMeshApi.instance.getFDSNodeWhitOutGroup()
        LOGUtils.d("StudioDeviceAdapter fdsNodeList.size:${fdsNodeList.size}")
        LOGUtils.i("fdsNodeList:${Gson().toJson(fdsNodeList)}")
        notifyDataSetChanged()
    }

    fun update(meshAddressList: MutableList<Int>) {
        for ((index, dev) in fdsNodeList.withIndex()) {
            if (listHaveMeshAddress(dev.meshAddress, meshAddressList)) {
                notifyItemChanged(index)
            }
        }
    }

    fun getAllFdsNodeList(): MutableList<FDSNodeInfo> {
        return fdsNodeList
    }

    fun setItemLongClickListener(itemLongClickListener: (FDSNodeInfo) -> Unit) {
        this.itemLongClickListener = itemLongClickListener
    }

    fun setItemClickListener(itemClickListener: (FDSNodeInfo) -> Unit) {
        this.itemClickListener = itemClickListener
    }

    private fun listHaveMeshAddress(meshAddress: Int, meshAddressList: MutableList<Int>): Boolean {
        for (address in meshAddressList) {
            if (address == meshAddress) {
                return true
            }
        }
        return false
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
        holder.tv_mac.text =
            "${fdsNodeInfo.macAddress} - ${fdsNodeInfo.type} - ${fdsNodeInfo.firmwareVersion}"

        //在线状态
        if (fdsNodeInfo.getFDSNodeState() == FDSNodeInfo.ON_OFF_STATE_OFFLINE) {
            holder.iv_light.setBackgroundResource(R.drawable.device_image_off)
        } else {
            holder.iv_light.setBackgroundResource(R.drawable.device_image_on)
        }

        //开关状态
        holder.iv_switch.isChecked = fdsNodeInfo.getFDSNodeState() == FDSNodeInfo.ON_OFF_STATE_ON

        val connectedFDSNodeInfo = FDSMeshApi.instance.getConnectedFDSNodeInfo()
        if (connectedFDSNodeInfo != null) {
            if (connectedFDSNodeInfo.macAddress == fdsNodeInfo.macAddress) {
                //直连设备
                holder.tv_name.setTextColor(
                    ContextCompat.getColor(
                        holder.itemView.context,
                        R.color.red
                    )
                )
                holder.tv_mac.setTextColor(
                    ContextCompat.getColor(
                        holder.itemView.context,
                        R.color.red
                    )
                )
            } else {
                holder.tv_name.setTextColor(
                    ContextCompat.getColor(
                        holder.itemView.context,
                        R.color.black
                    )
                )
                holder.tv_mac.setTextColor(
                    ContextCompat.getColor(
                        holder.itemView.context,
                        R.color.black
                    )
                )
            }
        } else {
            LOGUtils.e("StudioDeviceAdapter connectedFDSNodeInfo == null")
        }

        //Switch
        holder.iv_switch.setOnCheckedChangeListener { compoundButton, b ->
            //设备开关灯
            if (!compoundButton.isPressed) {
                return@setOnCheckedChangeListener
            }
            GodoxCommandApi.instance.changeLightSwitch(fdsNodeInfo.meshAddress, b)
        }

        //长按事件
        holder.itemView.setOnLongClickListener {
            itemLongClickListener(fdsNodeInfo)
            false
        }
        //点击事件
        holder.itemView.setOnClickListener {
            itemClickListener(fdsNodeInfo)
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