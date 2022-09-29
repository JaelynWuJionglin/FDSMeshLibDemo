package com.linkiing.fdsmeshlibdemo.utils

import android.annotation.SuppressLint
import android.content.Context
import android.view.Gravity
import androidx.annotation.StringRes
import com.blankj.utilcode.util.ToastUtils
import com.linkiing.fdsmeshlibdemo.R

object ConstantUtils {

    @SuppressLint("ResourceAsColor")
    fun toastSuccess(msg: String) {
        ToastUtils.getDefaultMaker()
            .setGravity(Gravity.CENTER, 0, 0)
            .setBgColor(R.color.white)
            .setTextColor(R.color.text_color)
            .setTopIcon(R.drawable.ic_baseline_tick)
        ToastUtils.showShort(msg)
    }

    @SuppressLint("ResourceAsColor")
    fun toastFail(msg: String) {
        ToastUtils.getDefaultMaker()
            .setGravity(Gravity.CENTER, 0, 0)
            .setBgColor(R.color.white)
            .setTextColor(R.color.text_color)
            .setTopIcon(R.drawable.ic_baseline_close)
        ToastUtils.showShort(msg)
    }

    fun spTextInt(context: Context, @StringRes textId: Int, end: Int): String {
        val res = context.resources
        return "${res.getString(textId)}$end"
    }

    fun spText(context: Context, @StringRes textId1: Int, @StringRes textId2: Int): String {
        val res = context.resources
        return "${res.getString(textId1)}${res.getString(textId2)}"
    }

    fun spText(context: Context, @StringRes textId: Int, end: String): String {
        val res = context.resources
        return "${res.getString(textId)}$end"
    }


}