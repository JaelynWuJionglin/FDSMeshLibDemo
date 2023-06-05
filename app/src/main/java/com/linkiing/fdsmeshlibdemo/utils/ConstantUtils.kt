package com.linkiing.fdsmeshlibdemo.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File

object ConstantUtils {
    var scanTime = 0L
    private var toast: Toast? = null

    fun toast(context: Context, msg: String) {
        if (toast != null) {
            toast!!.cancel()
        }
        toast = Toast.makeText(context,msg,Toast.LENGTH_SHORT)
        toast!!.show()
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

    fun shareFile(activity: Activity, file: File) {
        if (file.exists()) {
            val uri: Uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                FileProvider.getUriForFile(activity, activity.packageName + ".fileProvider", file)
            } else {
                Uri.fromFile(file)
            }
            val intent = Intent(Intent.ACTION_SEND)
            intent.type = "*/*"
            intent.putExtra(Intent.EXTRA_STREAM, uri)
            activity.startActivity(Intent.createChooser(intent, "share"))
        }
    }
}