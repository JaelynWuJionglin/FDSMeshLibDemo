package com.linkiing.fdsmeshlibdemo.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.CompoundButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import com.linkiing.fdsmeshlibdemo.R

class MenuItem(context: Context, attrs: AttributeSet?) : LinearLayout(context, attrs, 0) {
    private lateinit var tv: TextView
    private lateinit var tvEnd: TextView
    private lateinit var iv: ImageView
    private lateinit var ivEnd: ImageView
    private lateinit var switchCompat: SwitchCompat
    private lateinit var lrEnd: LinearLayout
    private lateinit var btLine: View

    init {
        initView()
        initAttrs(context, attrs)
    }

    private fun initView() {
        val view = inflate(context, R.layout.menu_item, this)
        tv = view.findViewById(R.id.tv)
        iv = view.findViewById(R.id.iv)

        lrEnd = view.findViewById(R.id.lr_end)
        tvEnd = view.findViewById(R.id.tv_end)
        ivEnd = view.findViewById(R.id.iv_end)

        switchCompat = view.findViewById(R.id.switchCompat)

        btLine = view.findViewById(R.id.bt_line)
    }

    private fun initAttrs(context: Context, attrs: AttributeSet?) {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.MenuItem)
        val text = typedArray.getString(R.styleable.MenuItem_menu_text) ?: ""
        val textEnd = typedArray.getString(R.styleable.MenuItem_menu_text_end) ?: ""
        val image = typedArray.getResourceId(R.styleable.MenuItem_menu_image, 0)
        val imageEnd = typedArray.getResourceId(R.styleable.MenuItem_menu_image_end, 0)
        val isSwitch = typedArray.getBoolean(R.styleable.MenuItem_menu_switch, false)
        val isBtLine = typedArray.getBoolean(R.styleable.MenuItem_menu_bt_line, true)
        typedArray.recycle()

        tv.text = text

        tvEnd.text = textEnd

        if (image == 0) {
            iv.visibility = GONE
        } else {
            iv.visibility = VISIBLE
            iv.setImageResource(image)
        }

        if (imageEnd == 0) {
            ivEnd.visibility = GONE
        } else {
            ivEnd.visibility = VISIBLE
            ivEnd.setImageResource(imageEnd)
        }

        switchCompat.visibility = if (isSwitch) {
            lrEnd.visibility = View.GONE
            View.VISIBLE
        } else {
            lrEnd.visibility = View.VISIBLE
            View.GONE
        }

        btLine.visibility = if (isBtLine) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }

    fun setTextHint(text: String) {
        tvEnd.text = text
    }

    fun switchCompatChecked(isChecked: Boolean) {
        switchCompat.isChecked = isChecked
    }

    fun setSwitchChangeListener(listener: CompoundButton.OnCheckedChangeListener) {
        switchCompat.setOnCheckedChangeListener(listener)
    }
}