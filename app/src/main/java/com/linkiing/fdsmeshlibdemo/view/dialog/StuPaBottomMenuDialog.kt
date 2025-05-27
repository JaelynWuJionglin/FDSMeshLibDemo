package com.linkiing.fdsmeshlibdemo.view.dialog

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import com.linkiing.fdsmeshlibdemo.R
import kotlinx.android.synthetic.main.stu_pa_bottom_dialog_layout.*

class StuPaBottomMenuDialog(context: Context) : BaseFullDialog(context, R.layout.stu_pa_bottom_dialog_layout),
    View.OnClickListener {
    private var listener: (Int) -> Unit = {}

    companion object {
        const val MENU_PA = 0
        const val MENU_NOT_PA = 1
    }

    init {
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        window?.setWindowAnimations(R.style.bomToTopDialogAnim) //设置窗口弹出动画
        window?.decorView?.setPadding(0, 0, 0, 0)
        window?.setGravity(Gravity.BOTTOM)
    }

    /**
     * 设置按钮的显示内容和监听
     * @param listener
     */
    fun setOnDialogListener(listener: (Int) -> Unit) {
        this.listener = listener
    }

    /**
     * showDialog
     */
    fun showDialog() {
        if (!isShowing) {
            super.show()
        }
    }

    /**
     * dismissDialog
     */
    fun dismissDialog() {
        if (isShowing) {
            super.dismiss()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setCancelable(true)
        setCanceledOnTouchOutside(true)

        tv_pa.setOnClickListener(this)
        tv_not_pa.setOnClickListener(this)
        tv_cancel.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.tv_pa -> {
                listener(MENU_PA)
            }
            R.id.tv_not_pa -> {
                listener(MENU_NOT_PA)
            }
            R.id.tv_cancel -> {

            }
        }
        dismissDialog()
    }
}