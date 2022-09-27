package com.linkiing.fdsmeshlibdemo.view.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.WindowManager
import androidx.annotation.LayoutRes
import com.linkiing.fdsmeshlibdemo.R

open class BaseFullDialog(context: Context, @LayoutRes private val layoutResID: Int)
    : Dialog(context, R.style.Dialog_bocop) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layoutResID)
        initView()
    }

    private fun initView() {
        val layoutParams = window!!.attributes
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT
        layoutParams.horizontalMargin = 0f
        window!!.attributes = layoutParams
    }
}