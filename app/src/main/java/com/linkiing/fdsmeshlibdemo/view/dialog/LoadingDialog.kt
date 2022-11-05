package com.linkiing.fdsmeshlibdemo.view.dialog

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.View
import com.linkiing.fdsmeshlibdemo.R
import com.linkiing.fdsmeshlibdemo.utils.ConstantUtils

class LoadingDialog(private val context: Context) {
    private var mDialog: Dialog? = null
    private var contentView: View? = null
    private val handler: Handler = Handler(Looper.myLooper()!!)
    private var toastMsgStr: String = ""

    private var timeOutCallbacks: (Boolean) -> Unit = {}
    private var isCallback: Boolean = false

    private var runnable: Runnable = Runnable {
        dismissDialog()
        if (toastMsgStr != "") {
            ConstantUtils.toast(context,toastMsgStr)
        }
        if (isCallback) {
            timeOutCallbacks(true)
        }
    }

    /**
     * @param isCanceled 点击返回键是否消失  ture 消失； false  不消失。
     */
    private fun showRoundProcessDialog(outTime: Long, isCanceled: Boolean) {
        showRoundProcessDialog(isCanceled)

        handler.removeCallbacks(runnable)
        handler.postDelayed(runnable, outTime)
    }

    private fun showRoundProcessDialog(isCanceled: Boolean) {
        if ((context as Activity).isFinishing){
            return
        }
        if (contentView == null){
            contentView = View.inflate(context, R.layout.loading_dialog_layout, null)
        }
        if (mDialog == null){
            mDialog = AlertDialog.Builder(context, R.style.Dialog_bocop).create()
        }
        if (isCanceled) {
            //dialog弹出后点击屏幕，dialog不消失；点击物理返回键dialog消失
            mDialog?.setCanceledOnTouchOutside(false)
        } else {
            //dialog弹出后点击屏幕或物理返回键，dialog不消失
            mDialog?.setCancelable(false)
        }
        if (mDialog?.isShowing!!) {
            mDialog?.dismiss()
        }
        mDialog?.show()
        mDialog?.setContentView(contentView!!)
    }

    /**
     * showDialog
     */
    fun showDialog() {
        this.toastMsgStr = ""
        isCallback = false
        showRoundProcessDialog(false)
    }

    fun showDialog(outTime: Long) {
        this.toastMsgStr = ""
        isCallback = false
        showRoundProcessDialog(outTime, false)
    }

    fun showDialog(outTime: Long, msgId: Int) {
        this.toastMsgStr = context.resources.getString(msgId)
        isCallback = false
        showRoundProcessDialog(outTime, false)
    }

    fun showDialog(outTime: Long, msgId: Int, isCanceled: Boolean) {
        this.toastMsgStr = context.resources.getString(msgId)
        isCallback = false
        showRoundProcessDialog(outTime, isCanceled)
    }

    fun showDialog(outTime: Long, isCanceled: Boolean) {
        this.toastMsgStr = ""
        isCallback = false
        showRoundProcessDialog(outTime, isCanceled)
    }

    fun showDialog(outTime: Long, msgId: Int, timeOutCallbacks: (Boolean) -> Unit) {
        this.toastMsgStr = context.resources.getString(msgId)
        this.timeOutCallbacks = timeOutCallbacks
        isCallback = true
        showRoundProcessDialog(outTime, false)
    }
    fun showDialog(outTime: Long, msgStr: String, timeOutCallbacks: (Boolean) -> Unit) {
        this.toastMsgStr = msgStr
        this.timeOutCallbacks = timeOutCallbacks
        isCallback = true
        showRoundProcessDialog(outTime, false)
    }


    fun dismissDialog() {
        handler.removeCallbacks(runnable)
        if (mDialog != null) {
            if (mDialog!!.isShowing) {
                mDialog!!.dismiss()
            }
        }
    }
}