package com.linkiing.fdsmeshlibdemo.view.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.WindowManager
import androidx.viewbinding.ViewBinding
import com.linkiing.fdsmeshlibdemo.R

abstract class BaseFullDialog<B : ViewBinding>(context: Context) :
    Dialog(context, R.style.Dialog_bocop) {
    protected val binding: B by lazy { initBind() }

    /**
     * 初始化UI绑定类
     * @return xxxBind
     */
    protected abstract fun initBind(): B

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        initView()
    }

    private fun initView() {
        val layoutParams = window!!.attributes
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT
        layoutParams.horizontalMargin = 0f
        window!!.attributes = layoutParams
    }
}