package com.linkiing.fdsmeshlibdemo.view.dialog

import android.content.Context
import android.os.Bundle
import android.text.TextUtils
import androidx.annotation.StringRes
import com.linkiing.fdsmeshlibdemo.databinding.InputTextDialogLayoutBinding

class InputTextDialog(context: Context) : BaseFullDialog<InputTextDialogLayoutBinding>(context) {
    private var listener: (String) -> Unit = {}
    private var defText = ""
    private var titleText = ""

    /**
     * 设置按钮的显示内容和监听
     */
    fun setOnDialogListener(listener: (String) -> Unit) {
        this.listener = listener
    }

    /**
     * 设置title text
     */
    fun setTitleText(@StringRes id:Int){
        titleText = context.resources.getString(id)
        binding.tvTitle.text = titleText
    }

    fun setTitleText(text:String){
        titleText = text
        binding.tvTitle.text = titleText
    }

    /**
     * 设置默认文字
     */
    fun setDefText(str:String?):InputTextDialog{
        if (!TextUtils.isEmpty(str)){
            defText = str!!
        }
        binding.etInput.setText(defText)
        return this
    }

    /**
     * show
     */
    fun showDialog(){
        dismissDialog()
        super.show()
    }

    fun dismissDialog(){
        if (isShowing){
            super.dismiss()
        }
    }

    override fun initBind(): InputTextDialogLayoutBinding {
        return InputTextDialogLayoutBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (titleText != ""){
            binding.tvTitle.text = titleText
        }

        if (defText != ""){
            binding.etInput.setText(defText)
            binding.etInput.setSelection(defText.length)
        }

        //设置确定按钮被点击后，向外界提供监听
        binding.tvConfirm.setOnClickListener {
            dismissDialog()
            val text: String = binding.etInput.text.toString()
            listener(text)
        }
        //设置取消按钮被点击后，向外界提供监听
        binding.tvCancel.setOnClickListener {
            dismissDialog()
        }
    }
}