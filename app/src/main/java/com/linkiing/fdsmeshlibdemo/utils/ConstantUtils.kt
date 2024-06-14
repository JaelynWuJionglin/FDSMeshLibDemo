package com.linkiing.fdsmeshlibdemo.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.core.content.FileProvider
import com.base.mesh.api.log.LOGUtils
import com.godox.sdk.api.FDSMeshApi
import com.linkiing.fdsmeshlibdemo.mmkv.MMKVSp
import java.io.File

object ConstantUtils {
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

    /**
     * 保存Studio数据到json
     */
    fun saveJson(index: Int) {
        LOGUtils.i("ConstantUtils ==> saveJson index:$index")
        if (index > 0) {
            //保存当前MeshJson数据
            val meshJsonStr = FDSMeshApi.instance.getCurrentMeshJson()
            val studioList = MMKVSp.instance.getStudioList()
            for (bean in studioList) {
                if (bean.index == index) {
                    bean.meshJsonStr = meshJsonStr
                }
            }
            MMKVSp.instance.setStudioList(studioList)
        }
    }
}