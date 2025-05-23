package com.linkiing.fdsmeshlibdemo.bean

import com.godox.sdk.model.FDSNodeInfo
import com.telink.ble.mesh.entity.AdvertisingDevice

/**
 * 搜索到的设备列表
 */
data class DeviceLisBean(
    var advertisingDevice: AdvertisingDevice,
    var deviceName: String,
    var type: String,
    var firmwareVersion: Int
) {
    var isChecked = false
}

/**
 * studio列表
 */
data class StudioListBean(
    var index: Int = 0
) {
    var name: String = ""
    var meshJsonStr = ""
    var choose = false
}

/**
 * ModelInfo
 */
data class ModelInfo(

    /**
     * 类型名字
     */
    var name: String = "",

    /**
     * 设备/组地址
     */
    var address: Int = -1,
)

/**
 * FDSNodeBean
 */
data class FDSNodeBean(var fdsNodeInfo: FDSNodeInfo){
    var isChecked = false
    var upgradeResults = UPGRADE_OTA_IDLE

    companion object {
        const val UPGRADE_OTA_IDLE = 0
        const val UPGRADE_OTA_SUS = 1
        const val UPGRADE_OTA_FAIL = 2
    }
}

/**
 * SeekBar
 */
data class SeekBarBean(var model: Int, var value: Int)