package com.linkiing.fdsmeshlibdemo.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import androidx.annotation.ColorRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.hjq.permissions.OnPermissionCallback
import com.leon.lfilepickerlibrary.LFilePicker
import com.leon.lfilepickerlibrary.utils.Constant
import com.linkiing.fdsmeshlibdemo.R

class FileSelectorUtils private constructor() {
    private val mLFilePicker = LFilePicker()
    private var listener: (String) -> Unit = {}

    /**
     * 单例
     */
    companion object {
        const val SELECT_REQUEST_CODE = 3001

        val instance: FileSelectorUtils by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            FileSelectorUtils()
        }
    }

    /**
     * 选择Gson文件
     */
    fun goSelectJson(activity: AppCompatActivity, listener: (String) -> Unit) {
        PermissionsUtils.filePermissions(activity, object : OnPermissionCallback {
            override fun onGranted(permissions: MutableList<String>?, all: Boolean) {
                selectFile(activity, arrayOf(".json", ".txt"), listener)
            }

            override fun onDenied(permissions: MutableList<String>?, never: Boolean) {
                super.onDenied(permissions, never)
                ConstantUtils.toast(activity, "权限被拒绝！")
            }
        })
    }

    /**
     * 选择Bin文件
     */
    fun goSelectBin(activity: AppCompatActivity, listener: (String) -> Unit) {
        PermissionsUtils.filePermissions(activity, object : OnPermissionCallback {
            override fun onGranted(permissions: MutableList<String>?, all: Boolean) {
                selectFile(activity, arrayOf(".bin"), listener)
            }

            override fun onDenied(permissions: MutableList<String>?, never: Boolean) {
                super.onDenied(permissions, never)
                ConstantUtils.toast(activity, "权限被拒绝！")
            }
        })
    }

    /**
     * onActivityResult中调用
     */
    fun onSelectActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == SELECT_REQUEST_CODE) {
                val list: List<String>? = data.getStringArrayListExtra(Constant.RESULT_INFO)
                if (list != null) {
                    if (list.isNotEmpty()) {
                        listener(list[0])
                    }
                }
            }
        }
    }

    @SuppressLint("SdCardPath")
    private fun selectFile(
        activity: AppCompatActivity,
        filterArray: Array<String>,
        listener: (String) -> Unit
    ) {
        this.listener = listener
        mLFilePicker.withActivity(activity)
            .withRequestCode(SELECT_REQUEST_CODE)//设置返回码
            .withMutilyMode(false)
            .withFileFilter(filterArray)
            .withTitleStyle(Constant.ICON_STYLE_YELLOW)
            //.withBackgroundColor(colorIntToString(activity,R.color.yellow))
            .withStartPath("/sdcard/")
            .start()

    }

    private fun colorIntToString(context: Context, @ColorRes id: Int): String {
        val color = ContextCompat.getColor(context, R.color.color_92e5e9)
        val stringBuffer = StringBuffer()
        stringBuffer.append("#")
        stringBuffer.append(colorTo2Hex(Color.red(color)))
        stringBuffer.append(colorTo2Hex(Color.green(color)))
        stringBuffer.append(colorTo2Hex(Color.blue(color)))
        return stringBuffer.toString()
    }

    private fun colorTo2Hex(rgb: Int): String {
        val str = Integer.toHexString(rgb)
        if (str.length < 2) {
            return "0$str"
        }
        return str
    }
}