package com.linkiing.fdsmeshlibdemo.ui.base

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.linkiing.fdsmeshlibdemo.bean.ModelInfo

open class BaseActivity : AppCompatActivity() {

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