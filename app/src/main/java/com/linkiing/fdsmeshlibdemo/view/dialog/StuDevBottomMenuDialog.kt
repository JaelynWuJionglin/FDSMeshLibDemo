package com.linkiing.fdsmeshlibdemo.view.dialog

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import com.linkiing.fdsmeshlibdemo.R
import kotlinx.android.synthetic.main.stu_dev_bottom_dialog_layout.*

class StuDevBottomMenuDialog(context: Context) : BaseFullDialog(context, R.layout.stu_dev_bottom_dialog_layout),
    View.OnClickListener {
    private var listener: (Int) -> Unit = {}

    companion object {
        const val MENU_DELETE = 0
        const val MENU_RENAME = 1
        const val MENU_BLE_UPGRADE = 2
        const val MENU_MCU_UPGRADE = 3
        const val MENU_DELETE_ALL = 4
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

        tv_rename.setOnClickListener(this)
        tv_delete.setOnClickListener(this)
        tv_ble_upgrade.setOnClickListener(this)
        tv_mcu_upgrade.setOnClickListener(this)
        tv_delete_all.setOnClickListener(this)
        tv_cancel.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.tv_rename -> {
                listener(MENU_RENAME)
            }
            R.id.tv_delete -> {
                listener(MENU_DELETE)
            }
            R.id.tv_ble_upgrade -> {
                listener(MENU_BLE_UPGRADE)
            }
            R.id.tv_mcu_upgrade->{
                listener(MENU_MCU_UPGRADE)
            }
            R.id.tv_delete_all -> {
                listener(MENU_DELETE_ALL)
            }
        }
        dismissDialog()
    }
}