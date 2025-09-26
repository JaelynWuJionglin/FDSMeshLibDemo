package com.linkiing.fdsmeshlibdemo.view.dialog

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import com.linkiing.fdsmeshlibdemo.R
import com.linkiing.fdsmeshlibdemo.databinding.StuDevBottomDialogLayoutBinding
import androidx.core.graphics.drawable.toDrawable

class StuDevBottomMenuDialog(context: Context) : BaseFullDialog<StuDevBottomDialogLayoutBinding>(context),
    View.OnClickListener {
    private var listener: (Int) -> Unit = {}

    companion object {
        const val MENU_DELETE = 0
        const val MENU_RENAME = 1
        const val MENU_BLE_UPGRADE = 2
        const val MENU_MCU_UPGRADE = 3
        const val MENU_DELETE_ALL = 4
        const val MENU_DELETE_FORCE = 5
    }

    init {
        window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
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

    override fun initBind(): StuDevBottomDialogLayoutBinding {
        return StuDevBottomDialogLayoutBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setCancelable(true)
        setCanceledOnTouchOutside(true)

        binding.tvRename.setOnClickListener(this)
        binding.tvDelete.setOnClickListener(this)
        binding.tvBleUpgrade.setOnClickListener(this)
        binding.tvMcuUpgrade.setOnClickListener(this)
        binding.tvDeleteAll.setOnClickListener(this)
        binding.tvCancel.setOnClickListener(this)
        binding.tvDeleteForce.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.tv_rename -> {
                listener(MENU_RENAME)
            }

            R.id.tv_ble_upgrade -> {
                listener(MENU_BLE_UPGRADE)
            }

            R.id.tv_mcu_upgrade -> {
                listener(MENU_MCU_UPGRADE)
            }

            R.id.tv_delete -> {
                listener(MENU_DELETE)
            }

            R.id.tv_delete_force -> {
                listener(MENU_DELETE_FORCE)
            }

            R.id.tv_delete_all -> {
                listener(MENU_DELETE_ALL)
            }
        }
        dismissDialog()
    }
}