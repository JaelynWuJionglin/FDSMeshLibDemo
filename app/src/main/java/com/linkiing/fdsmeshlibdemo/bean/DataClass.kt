package com.linkiing.fdsmeshlibdemo.bean

import com.telink.ble.mesh.entity.AdvertisingDevice

/**
 * 搜索到的设备列表
 */
data class DeviceLisBean(
    var advertisingDevice: AdvertisingDevice,
    var deviceType: String
) {
    var isChecked = false
}

/**
 * studio列表
 */
data class StudioListBean(
    var index: Int = 0
){
    var name: String = ""
    var meshJsonStr = ""
    var choose = false
}