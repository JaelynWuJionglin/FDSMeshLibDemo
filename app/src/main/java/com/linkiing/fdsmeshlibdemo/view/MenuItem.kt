package com.linkiing.fdsmeshlibdemo.view

import android.content.Context
import android.util.AttributeSet
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.linkiing.fdsmeshlibdemo.R

class MenuItem (context: Context, attrs: AttributeSet?) : LinearLayout(context, attrs, 0){
    private lateinit var tv: TextView
    private lateinit var tvEnd: TextView
    private lateinit var iv:ImageView
    private lateinit var ivEnd:ImageView

    init {
        initView()
        initAttrs(context, attrs)
    }

    private fun initView(){
        val view = inflate(context, R.layout.menu_item, this)
        tv = view.findViewById(R.id.tv)
        tvEnd = view.findViewById(R.id.tv_end)
        iv = view.findViewById(R.id.iv)
        ivEnd = view.findViewById(R.id.iv_end)
    }

    private fun initAttrs(context: Context, attrs: AttributeSet?) {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.MenuItem)
        val text = typedArray.getString(R.styleable.MenuItem_menu_text)
        val textEnd = typedArray.getString(R.styleable.MenuItem_menu_text_end)
        val image = typedArray.getResourceId(R.styleable.MenuItem_menu_image, 0)
        val imageEnd = typedArray.getResourceId(R.styleable.MenuItem_menu_image_end, 0)
        typedArray.recycle()

        tv.text = text
        tvEnd.text = textEnd
        if (image == 0){
            iv.visibility = GONE
        } else {
            iv.visibility = VISIBLE
            iv.setImageResource(image)
        }
        if (imageEnd == 0){
            ivEnd.visibility = GONE
        } else {
            ivEnd.visibility = VISIBLE
            ivEnd.setImageResource(imageEnd)
        }
    }

    fun setTextHint(text: String){
        tvEnd.text = text
    }
}