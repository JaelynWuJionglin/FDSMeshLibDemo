package com.linkiing.fdsmeshlibdemo.ui.base

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import com.linkiing.fdsmeshlibdemo.bean.ModelInfo

open class BaseActivity: AppCompatActivity() {

    protected fun goActivity(cls: Class<*>, isFinish:Boolean){
        startActivity(Intent(this, cls))
        if (isFinish){
            finish()
        }
    }
    //添加列表名字
    protected fun modelData(type:Int,name:String,address:Int): ModelInfo {
        val mInfo= ModelInfo()
        mInfo.address=address
        mInfo.type=type
        mInfo.name=name
        return mInfo;
    }
}