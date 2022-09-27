package com.linkiing.fdsmeshlibdemo.ui.base

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity

open class BaseActivity: AppCompatActivity() {

    protected fun goActivity(cls: Class<*>, isFinish:Boolean){
        startActivity(Intent(this, cls))
        if (isFinish){
            finish()
        }
    }
}