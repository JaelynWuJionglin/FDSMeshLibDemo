package com.linkiing.fdsmeshlibdemo.view

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import com.linkiing.fdsmeshlibdemo.R

class TitleBar(context: Context, attrs: AttributeSet) : RelativeLayout(context, attrs) {
    private var back: ImageView
    private var titleText: TextView
    private var endText: TextView
    private var lrScan: LinearLayout

    init {
        val view = LayoutInflater.from(context).inflate(R.layout.title_bar_layout, this)
        back = view.findViewById(R.id.back)
        endText = view.findViewById(R.id.tv_end)
        titleText = view.findViewById(R.id.title_text)
        lrScan = view.findViewById(R.id.lr_scan)

        initTypedArray(attrs)

        back.setOnClickListener {
            (context as Activity).finish()
        }
    }

    //typedArray
    @SuppressLint("Recycle")
    private fun initTypedArray(attrs: AttributeSet) {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.TitleBar)
        val text = typedArray.getString(R.styleable.TitleBar_titleBar_titleText)
        titleText.text = text
    }

    fun initTitleBar(titleTextId: Int, endTextId: Int, isEdit: Boolean) {
        if (titleTextId == 0) {
            titleText.text = ""
        } else {
            titleText.setText(titleTextId)
        }
        if (endTextId == 0) {
            endText.text = ""
        } else {
            endText.setText(endTextId)
        }
        setEndImage(false)
    }

    fun initTitleBar(titleTextValue: String, endTextValue: String, isEdit: Boolean) {
        titleText.text = titleTextValue
        endText.text = endTextValue
        setEndImage(false)
    }

    fun initTitleBar(titleTextStr: String, endTextId: Int, isEdit: Boolean) {
        titleText.text = titleTextStr
        if (endTextId == 0) {
            endText.text = ""
        } else {
            endText.setText(endTextId)
        }
        setEndImage(false)
    }

    fun initTitleBar(titleTextId: Int, isEdit: Boolean, isEndImage: Boolean) {
        titleText.setText(titleTextId)
        endText.text = ""
        setEndImage(isEndImage)
    }

    fun setTitle(text: String) {
        titleText.text = text
    }

    fun getTitle(): TextView {
        return titleText;
    }

    fun setTitle(titleTextId: Int) {
        titleText.setText(titleTextId)
    }

    private fun setEndImage(isEndImage: Boolean) {
        lrScan.visibility = if (isEndImage) {
            VISIBLE
        } else {
            GONE
        }
    }

    fun setOnEndTextListener(listener: OnClickListener) {
        endText.setOnClickListener(listener)
    }

    fun setOnEndImageListener(listener: OnClickListener) {
        lrScan.setOnClickListener(listener)
    }
}