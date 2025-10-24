package com.linkiing.fdsmeshlibdemo.view.dialog

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import com.linkiing.fdsmeshlibdemo.R
import com.linkiing.fdsmeshlibdemo.databinding.StuPaBottomDialogLayoutBinding
import androidx.core.graphics.drawable.toDrawable

class StuPaBottomMenuDialog(context: Context) :
    BaseFullDialog<StuPaBottomDialogLayoutBinding>(context),
    View.OnClickListener {
    private var listener: (Int) -> Unit = {}

    companion object {
        const val MENU_NOT_PA = 0
        const val MENU_PA = 1
        const val MENU_TYPE_03 = 3
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

    override fun initBind(): StuPaBottomDialogLayoutBinding {
        return StuPaBottomDialogLayoutBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setCancelable(true)
        setCanceledOnTouchOutside(true)

        binding.tvCancel.setOnClickListener(this)

        binding.tvPa.setOnClickListener(this)
        binding.tvNotPa.setOnClickListener(this)
        binding.tvType03.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.tv_pa -> {
                listener(MENU_PA)
            }

            R.id.tv_not_pa -> {
                listener(MENU_NOT_PA)
            }

            R.id.tv_type_03 -> {
                listener(MENU_TYPE_03)
            }

            R.id.tv_cancel -> {

            }
        }
        dismissDialog()
    }
}