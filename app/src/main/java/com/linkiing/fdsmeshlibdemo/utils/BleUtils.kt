package com.linkiing.fdsmeshlibdemo.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.provider.Settings
import androidx.appcompat.app.AlertDialog
import com.linkiing.fdsmeshlibdemo.R

class BleUtils {
    private lateinit var context: Context
    private var bluetoothManager: BluetoothManager? = null

    companion object {
        val instance: BleUtils by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            BleUtils()
        }
    }

    //初始化
    fun init(context: Context) {
        this.context = context
    }

    fun getBluetoothAdapter(): BluetoothAdapter? {
        if (bluetoothManager == null) {
            bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        }
        return bluetoothManager?.adapter
    }


    /**
     * 是否支持蓝牙
     * @return
     */
    fun hasBleOpen(): Boolean {
        return context.packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)
    }

    /**
     * 蓝牙是否打开
     * @return
     */
    fun isBleOpen(): Boolean {
        val mBluetoothAdapter = getBluetoothAdapter()
        return mBluetoothAdapter?.isEnabled ?: false
    }

    /**
     * 打开蓝牙
     */
    @SuppressLint("MissingPermission")
    fun openBLE(): Boolean {
        val mBluetoothAdapter = getBluetoothAdapter()
        return if (mBluetoothAdapter == null) {
            false
        } else try {
            mBluetoothAdapter.enable()
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 跳转GPS设置
     */
    fun openGPSSettings(activity: Activity): Boolean {
        return if (checkGPSIsOpen(activity)) {
            true
        } else {
            //没有打开则弹出对话框
            AlertDialog.Builder(activity)
                .setTitle(R.string.notifyTitle)
                .setMessage(R.string.gpsNotifyMsg) // 拒绝, 退出应用
                .setNegativeButton(R.string.cancel_text) { dialog, which -> dialog.cancel() }
                .setPositiveButton(R.string.confirm_text) { dialog, which ->
                    //跳转GPS设置界面
                    val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    activity.startActivityForResult(intent, 1)
                }
                .setCancelable(false)
                .show()
            false
        }
    }

    /**
     * 检测GPS是否打开
     */
    private fun checkGPSIsOpen(activity: Activity): Boolean {
        val isOpen: Boolean
        val locationManager = activity.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        isOpen = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        return isOpen
    }
}