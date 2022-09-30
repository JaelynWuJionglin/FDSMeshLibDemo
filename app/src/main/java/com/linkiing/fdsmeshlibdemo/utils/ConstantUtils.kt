package com.linkiing.fdsmeshlibdemo.utils

import android.content.Context
import android.content.pm.PackageManager
import android.widget.Toast

object ConstantUtils {
    private var toast: Toast? = null

    fun toast(context: Context, msg: String) {
        if (toast == null) {
            toast = Toast.makeText(context,msg,Toast.LENGTH_SHORT)
        } else {
            toast?.cancel()
        }
        toast?.show()
    }

    /*获取app版本字符串*/
    fun getAppVerStr(context: Context): String {
        return try {
            val manager = context.packageManager.getPackageInfo(context.packageName, 0)
            manager.versionName
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            "Unknown"
        }
    }
}