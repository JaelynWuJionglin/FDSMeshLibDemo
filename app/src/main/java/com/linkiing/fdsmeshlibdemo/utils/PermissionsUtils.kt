package com.linkiing.fdsmeshlibdemo.utils

import android.Manifest
import android.app.Activity
import android.os.Build
import com.hjq.permissions.OnPermissionCallback
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions

object PermissionsUtils {

    /**
     * 蓝牙权限
     */
    fun blePermissions(activity: Activity, callback: OnPermissionCallback) {
        if (!BleUtils.instance.hasBleOpen()) {
            //设备不支持蓝牙
            return
        }
        //申请权限
        val permCallback = object : OnPermissionCallback{
            override fun onGranted(permissions: MutableList<String>?, all: Boolean) {
                if (BleUtils.instance.isBleOpen()) {
                    if (BleUtils.instance.openGPSSettings(activity)) {
                        callback.onGranted(permissions,all)
                    } else {
                        callback.onDenied(permissions,all)
                    }
                } else {
                    if (!BleUtils.instance.openBLE()){
                        callback.onDenied(permissions,all)
                    }
                }
            }

            override fun onDenied(permissions: MutableList<String>?, never: Boolean) {
                super.onDenied(permissions, never)
                callback.onDenied(permissions,never)
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            XXPermissions
                .with(activity)
                .unchecked()// 设置不触发错误检测机制（局部设置）
//                .permission(Permission.ACCESS_FINE_LOCATION)
//                .permission(Permission.ACCESS_COARSE_LOCATION)
                .permission(Permission.BLUETOOTH_SCAN)
                .permission(Permission.BLUETOOTH_CONNECT)
                .permission(Permission.BLUETOOTH_ADVERTISE)
                .request(permCallback)
        } else {
            XXPermissions
                .with(activity)
                .permission(Permission.ACCESS_FINE_LOCATION)
                .permission(Permission.ACCESS_COARSE_LOCATION)
                .request(permCallback)
        }
    }

    /**
     * 文件访问权限
     */
    fun filePermissions(activity: Activity, callback: OnPermissionCallback) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            XXPermissions
                .with(activity)
                .permission(Permission.MANAGE_EXTERNAL_STORAGE)
                .request(callback)
        } else {
            XXPermissions
                .with(activity)
                .permission(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
                .request(callback)
        }
    }

    /**
     * 文件访问权限,相机权限
     */
    fun cameraPermissions(activity: Activity, callback: OnPermissionCallback){
        XXPermissions
            .with(activity)
            .permission(
                Manifest.permission.CAMERA
            )
            .request(callback)
    }
}