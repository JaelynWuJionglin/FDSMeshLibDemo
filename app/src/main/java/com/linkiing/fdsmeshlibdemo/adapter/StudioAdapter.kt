package com.linkiing.fdsmeshlibdemo.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.godox.sdk.api.FDSMeshApi
import com.linkiing.fdsmeshlibdemo.R
import com.linkiing.fdsmeshlibdemo.mmkv.MMKVSp
import com.telink.ble.mesh.util.LOGUtils

class StudioAdapter : RecyclerView.Adapter<StudioAdapter.MyHolder>() {
    private var studioList = MMKVSp.instance.getStudioList()
    private var onItemClickListener: (String) -> Unit = {}

    init {
        if (studioList.isEmpty()) {
            addStudio("Studio-1")
        }
        LOGUtils.d("StudioDeviceAdapter studioList.size:${studioList.size}")
        notifyDataSetChanged()
    }

    fun setOnItemClickListener(onItemClickListener: (String) -> Unit) {
        this.onItemClickListener = onItemClickListener
    }

    fun addStudio(name: String){
        for (s in studioList) {
            if (s == name) {
                return
            }
        }
        studioList.add(name)
        MMKVSp.instance.setStudioList(studioList)
        for ((index, str) in studioList.withIndex()) {
            if (str == name) {
                notifyItemChanged(index)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_studio_item, parent, false)
        return MyHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: MyHolder, position: Int) {
        val str = studioList[position]
        holder.tv_name.text = str

        //长按事件
        holder.itemView.setOnLongClickListener {
            onItemClickListener(str)
            false
        }
        holder.itemView.setOnClickListener{
            onItemClickListener(str)
        }
    }

    override fun getItemCount(): Int {
        return studioList.size
    }

    class MyHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tv_name = itemView.findViewById<TextView>(R.id.tv_name)
    }
}