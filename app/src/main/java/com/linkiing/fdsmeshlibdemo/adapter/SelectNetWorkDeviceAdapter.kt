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

class SelectNetWorkDeviceAdapter(private val isPA: Int) : RecyclerView.Adapter<SelectNetWorkDeviceAdapter.MyHolder>() {
    private var devList = getFDSNodeList()
    private var isAllCheckListener: (Boolean) -> Unit = {}

    fun update() {
        devList = getFDSNodeList()
        notifyDataSetChanged()
    }

    fun allCheck(isAllCheck: Boolean) {
        for (bean in devList) {
            bean.isChecked = isAllCheck
        }
        notifyDataSetChanged()
    }

    fun getCheckDevices(): MutableList<FDSNodeInfo> {
        val list = arrayListOf<FDSNodeInfo>()
        for (fdsNodeBean in devList) {
            if (fdsNodeBean.isChecked) {
                list.add(fdsNodeBean.fdsNodeInfo )
            }
        }
        return list
    }

    fun setIsAllCheckListener(isAllCheckListener: (Boolean) -> Unit) {
        this.isAllCheckListener = isAllCheckListener
    }

    private fun getFDSNodeList(): MutableList<FDSNodeBean> {
        val list = arrayListOf<FDSNodeBean>()
        val nodes = FDSMeshApi.instance.getFDSNodeWhitOutGroup()
        for (node in nodes) {
            val nodeBean = FDSNodeBean(node)
            nodeBean.isChecked = false
            if (isPA == -1) {
                list.add(nodeBean)
            } else {
                if (node.isPA == isPA) {
                    list.add(nodeBean)
                }
            }
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
        val device = devList[position]
        holder.tv_name.text = device.fdsNodeInfo.name
        holder.tv_mac.text = device.fdsNodeInfo.macAddress

        if (FDSMeshApi.instance.isMeshOtaSupport(device.fdsNodeInfo.meshAddress)) {

            holder.iv_check.visibility = View.VISIBLE
            holder.tv_up_result.visibility = View.GONE

            if (device.isChecked) {
                holder.iv_check.setBackgroundResource(R.drawable.checked_image_on)
            } else {
                holder.iv_check.setBackgroundResource(R.drawable.checked_image_off)
            }

            holder.itemView.setOnClickListener {
                val isCheck = device.isChecked
                device.isChecked = !isCheck
                notifyItemChanged(position)

                isAllCheckListener(isAllCheck())
            }

        } else {
            holder.iv_check.visibility = View.GONE
            holder.tv_up_result.visibility = View.VISIBLE

            holder.tv_up_result.text = "不支持"
        }
    }

    override fun getItemCount(): Int {
        return devList.size
    }

    class MyHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tv_name = itemView.findViewById<TextView>(R.id.tv_name)
        val tv_mac = itemView.findViewById<TextView>(R.id.tv_mac)
        val iv_check = itemView.findViewById<ImageView>(R.id.iv_check)
        val tv_up_result = itemView.findViewById<TextView>(R.id.tv_up_result)
    }
}