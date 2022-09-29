package com.linkiing.fdsmeshlibdemo.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.godox.sdk.api.FDSMeshApi
import com.linkiing.fdsmeshlibdemo.R
import com.linkiing.fdsmeshlibdemo.bean.StudioListBean
import com.linkiing.fdsmeshlibdemo.mmkv.MMKVSp
import com.telink.ble.mesh.util.LOGUtils

class StudioAdapter : RecyclerView.Adapter<StudioAdapter.MyHolder>() {
    private var studioList = MMKVSp.instance.getStudioList()
    private var onItemClickListener: (StudioListBean) -> Unit = {}

    init {
        if (studioList.isEmpty()) {
            val studioListBean = StudioListBean(getStudioNextIndex())
            studioListBean.name = "Studio-1"
            studioListBean.meshJsonStr = FDSMeshApi.instance.getInitMeshJson()
            addStudio(studioListBean)
        }
        LOGUtils.d("StudioDeviceAdapter studioList.size:${studioList.size}")
    }

    fun updateData(){
        studioList = MMKVSp.instance.getStudioList()
        notifyDataSetChanged()
    }

    fun setOnItemClickListener(onItemClickListener: (StudioListBean) -> Unit) {
        this.onItemClickListener = onItemClickListener
    }

    fun addStudio(studioListBean: StudioListBean) {
        studioList.add(studioListBean)
        MMKVSp.instance.setStudioList(studioList)
        notifyDataSetChanged()
    }

    fun getStudioNextIndex(): Int {
        return if (studioList.isEmpty()) {
            1
        } else {
            var index = 1
            for (bean in studioList) {
                if (bean.index > index) {
                    index = bean.index
                }
            }
            index+1
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_studio_item, parent, false)
        return MyHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: MyHolder, position: Int) {
        val bean = studioList[position]
        holder.tv_name.text = if (bean.name == "") {
            "Studio-${bean.index}"
        } else {
            bean.name
        }

        LOGUtils.e("=====> position:$position  studioList.size:${studioList.size}")
        if (position == studioList.size - 1) {
            holder.view_line.visibility = View.GONE
        } else {
            holder.view_line.visibility = View.VISIBLE
        }

        //点击事件
        holder.itemView.setOnClickListener {
            onItemClickListener(bean)
            for (studio in studioList) {
                studio.choose = studio.name == bean.name
            }
            MMKVSp.instance.setStudioList(studioList)
        }
        holder.itemView.setOnClickListener{
            onItemClickListener(bean)
        }
    }

    override fun getItemCount(): Int {
        return studioList.size
    }

    class MyHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tv_name = itemView.findViewById<TextView>(R.id.tv_name)
        val view_line = itemView.findViewById<View>(R.id.view_line)
    }
}