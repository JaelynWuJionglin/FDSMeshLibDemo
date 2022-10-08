package com.linkiing.fdsmeshlibdemo.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.linkiing.fdsmeshlibdemo.R
import com.linkiing.fdsmeshlibdemo.ui.base.BaseActivity
import com.linkiing.fdsmeshlibdemo.utils.ConstantUtils
import kotlinx.android.synthetic.main.activity_start.*


class StartActivity: BaseActivity() {
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {

        if (!this.isTaskRoot) {
            val action = intent.action
            if (intent.hasCategory(Intent.CATEGORY_LAUNCHER) && action == Intent.ACTION_MAIN) {
                finish()
                return
            }
        }

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)

        @SuppressLint("SetTextI18n")
        tv_time_version?.text = "2022-V${ConstantUtils.getAppVerStr(this)}"

        handler.postDelayed({
            goActivity(MainActivity::class.java, true)
        }, 1000)
    }
}