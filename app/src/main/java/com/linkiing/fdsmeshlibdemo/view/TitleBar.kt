package com.linkiing.fdsmeshlibdemo.view

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.linkiing.fdsmeshlibdemo.R

class TitleBar(context: Context, attrs: AttributeSet) : RelativeLayout(context, attrs) {
    private var back: ImageView
    private var titleText: TextView
    private var endText: TextView
    private var lrEndImage: LinearLayout
    private var ivEndImage: ImageView

    init {
        val view = LayoutInflater.from(context).inflate(R.layout.title_bar_layout, this)
        back = view.findViewById(R.id.back)
        endText = view.findViewById(R.id.tv_end)
        titleText = view.findViewById(R.id.title_text)
        lrEndImage = view.findViewById(R.id.lr_end_image)
        ivEndImage = view.findViewById(R.id.iv_end_image)

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

    fun initTitleBar(titleTextId: Int, @StringRes endTextId: Int) {
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

    fun initTitleBar(titleTextValue: String, endTextValue: String) {
        titleText.text = titleTextValue
        endText.text = endTextValue
        setEndImage(false)
    }

    fun initTitleBar(titleTextStr: String, @StringRes endTextId: Int) {
        titleText.text = titleTextStr
        if (endTextId == 0) {
            endText.text = ""
        } else {
            endText.setText(endTextId)
        }
        setEndImage(false)
    }

    fun initTitleBar(isBack: Boolean, @DrawableRes endImageViewId: Int) {
        if (isBack) {
            back.visibility = View.VISIBLE
        } else {
            back.visibility = View.GONE
        }
        endText.text = ""
        setEndImage(endImageViewId != 0)
        if (endImageViewId != 0) {
            ivEndImage.setBackgroundResource(endImageViewId)
        }
    }

    fun setTitle(text: String) {
        titleText.text = text
    }

    fun getTitle(): TextView {
        return titleText;
    }

    fun setTitle(@StringRes titleTextId: Int) {
        titleText.setText(titleTextId)
    }

    fun getBackView(): ImageView {
        return back
    }

    private fun setEndImage(isEndImage: Boolean) {
        lrEndImage.visibility = if (isEndImage) {
            VISIBLE
        } else {
            GONE
        }
    }

    fun setOnEndTextListener(listener: OnClickListener) {
        endText.setOnClickListener(listener)
    }

    fun setOnEndImageListener(listener: OnClickListener) {
        lrEndImage.setOnClickListener(listener)
    }
}