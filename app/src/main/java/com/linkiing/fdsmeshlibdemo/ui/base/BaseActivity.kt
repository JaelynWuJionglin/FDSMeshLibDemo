package com.linkiing.fdsmeshlibdemo.ui.base

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsetsController
import androidx.appcompat.app.AppCompatActivity
import com.linkiing.fdsmeshlibdemo.bean.ModelInfo

open class BaseActivity : AppCompatActivity() {
    override fun onStart() {
        super.onStart()

        // 设置状态栏文字颜色为暗色
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window?.insetsController?.setSystemBarsAppearance(
                WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
                WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window?.decorView?.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
        }
    }

    protected fun goActivity(cls: Class<*>, isFinish: Boolean) {
        startActivity(Intent(this, cls))
        if (isFinish) {
            finish()
        }
    }

    protected fun goActivity(cls: Class<*>, key: String, value: Int, isFinish: Boolean) {
        val intent = Intent(this, cls)
        intent.putExtra(key,value)
        startActivity(intent)
        if (isFinish) {
            finish()
        }
    }

    protected fun goActivityKeyAndValue(cls: Class<*>, isFinish:Boolean,key:String,value:Int){
        val intent=Intent(this, cls)
        intent.putExtra(key,value)
        startActivity(intent)
        if (isFinish){
            finish()
        }
    }

    protected fun goActivityBundle(cls: Class<*>, isFinish:Boolean,bundle:Bundle){
        val intent=Intent(this, cls)
        intent.putExtras(bundle)
        startActivity(intent)
        if (isFinish){
            finish()
        }
    }

    //添加列表名字
    protected fun modelData(name:String, address:Int): ModelInfo {
        val mInfo= ModelInfo()
        mInfo.address = address
        mInfo.name = name
        return mInfo;
    }
}