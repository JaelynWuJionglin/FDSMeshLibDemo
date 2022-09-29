package com.linkiing.fdsmeshlibdemo.view

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.TextView
import com.linkiing.fdsmeshlibdemo.R

class MainTabItem(context: Context, attrs: AttributeSet?) : LinearLayout(context, attrs, 0) {
    private lateinit var tv:TextView
    private lateinit var checkBox: CheckBox
    private var ck = false

    init {
        init(context, attrs)
    }

    @SuppressLint("Recycle")
    private fun init(context: Context, attrs: AttributeSet?) {
        val view = inflate(context, R.layout.item_main_tab, this)

        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.MainTab)
        val name = typedArray.getString(R.styleable.MainTab_tab_name)
        val image = typedArray.getResourceId(R.styleable.MainTab_tab_image, R.drawable.main_tab_device)
        ck = typedArray.getBoolean(R.styleable.MainTab_tab_ck, false)

        tv = view.findViewById(R.id.tv)
        checkBox = view.findViewById(R.id.checkBox)

        tv.text = name
        checkBox.setBackgroundResource(image)

    }

    fun isCk(): Boolean {
        return ck
    }

    fun setCk(ck: Boolean) {
        this.ck = ck
        checkBox.isChecked = ck
        tv.isSelected = ck
    }

}