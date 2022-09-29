package com.linkiing.fdsmeshlibdemo.utils

import android.content.Context
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
}