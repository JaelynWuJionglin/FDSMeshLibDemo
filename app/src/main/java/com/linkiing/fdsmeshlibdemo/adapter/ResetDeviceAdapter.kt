package com.linkiing.fdsmeshlibdemo.adapter

import android.annotation.SuppressLint
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.godox.sdk.model.FDSNodeInfo
import com.godox.sdk.tool.DevicesUtils
import com.linkiing.fdsmeshlibdemo.R
import com.linkiing.fdsmeshlibdemo.bean.DeviceLisBean
import com.telink.ble.mesh.entity.AdvertisingDevice
import com.telink.ble.mesh.util.PermissionUtils

class ResetDeviceAdapter : RecyclerView.Adapter<ResetDeviceAdapter.MyHolder>() {
    private val devList = mutableListOf<DeviceLisBean>()
    private var isAllCheckListener: (Boolean) -> Unit = {}

    fun addDevices(advertisingDevice: AdvertisingDevice, deviceName: String, type: String, firmwareVersion: Int) {
        if (haveDevice(advertisingDevice)) {
            return
        }
        devList.add(DeviceLisBean(advertisingDevice, deviceName, type, firmwareVersion))
        notifyItemChanged(devList.size - 1)
    }

    fun allCheck(isAllCheck: Boolean) {
        for (bean in devList) {
            bean.isChecked = isAllCheck
        }
        notifyDataSetChanged()
    }

    fun removeItemAtInNetWork(fdsNodes: MutableList<FDSNodeInfo>) {
        val iterator = devList.iterator()
        while (iterator.hasNext()) {
            val dev = iterator.next()
            for (fdsNode in fdsNodes) {
                if (fdsNode.macAddress == dev.advertisingDevice.device.address) {
                    iterator.remove()
                }
            }
        }
        notifyDataSetChanged()
    }

    fun clearList() {
        devList.clear()
        notifyDataSetChanged()
    }

    fun setIsAllCheckListener(isAllCheckListener: (Boolean) -> Unit) {
        this.isAllCheckListener = isAllCheckListener
    }

    /**
     * 获取选中的设备列表
     */
    fun getCheckDevices(): MutableList<AdvertisingDevice> {
        val list = mutableListOf<AdvertisingDevice>()
        for (bean in devList) {
            if (bean.isChecked) {
                list.add(bean.advertisingDevice)
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

    private fun haveDevice(advertisingDevice: AdvertisingDevice): Boolean {
        if (devList.isEmpty()) {
            return false
        }
        for (bean in devList) {
            if (bean.advertisingDevice.device.address == advertisingDevice.device.address) {
                return true
            }
        }
        return false
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_reset_device_item, parent, false)
        return MyHolder(view)
    }

    @SuppressLint("SetTextI18n", "MissingPermission")
    override fun onBindViewHolder(holder: MyHolder, position: Int) {
        val deviceBean = devList[position]
        val devName = if (TextUtils.isEmpty(deviceBean.deviceName)) {
            "null"
        } else {
            deviceBean.deviceName
        }

        holder.tv_name.text = "${devName}_${deviceBean.type}"
        holder.tv_mac.text = "mac:${deviceBean.advertisingDevice.device.address}" +
                " - ver:${deviceBean.firmwareVersion.toString(16)}" +
                " - rssi:${deviceBean.advertisingDevice.rssi}"

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