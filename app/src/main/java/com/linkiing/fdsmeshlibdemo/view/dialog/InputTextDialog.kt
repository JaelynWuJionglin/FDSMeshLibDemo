package com.linkiing.fdsmeshlibdemo.view.dialog

import android.content.Context
import android.os.Bundle
import android.text.TextUtils
import androidx.annotation.StringRes
import com.linkiing.fdsmeshlibdemo.R
import kotlinx.android.synthetic.main.input_text_dialog_layout.*

class InputTextDialog(context: Context)
    : BaseFullDialog(context, R.layout.input_text_dialog_layout) {
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
        if (tv_title!=null){
            tv_title.text = titleText
        }
    }

    fun setTitleText(text:String){
        titleText = text
        if (tv_title!=null){
            tv_title.text = titleText
        }
    }

    /**
     * 设置默认文字
     */
    fun setDefText(str:String?):InputTextDialog{
        if (!TextUtils.isEmpty(str)){
            defText = str!!
        }
        
        if (et_input!=null){
            et_input.setText(defText)
        }
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (titleText != ""){
            tv_title.text = titleText
        }

        if (defText != ""){
            et_input.setText(defText)
            et_input.setSelection(defText.length)
        }

        //设置确定按钮被点击后，向外界提供监听
        tv_confirm.setOnClickListener {
            dismissDialog()
            val text: String = et_input.text.toString()
            listener(text)
        }
        //设置取消按钮被点击后，向外界提供监听
        tv_cancel.setOnClickListener {
            dismissDialog()
        }
    }
}