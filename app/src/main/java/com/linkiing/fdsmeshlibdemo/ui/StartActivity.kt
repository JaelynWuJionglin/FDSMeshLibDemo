package com.linkiing.fdsmeshlibdemo.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.base.mesh.api.log.LOGUtils
import com.base.mesh.api.utils.SystemUtils
import com.linkiing.fdsmeshlibdemo.databinding.ActivityStartBinding
import com.linkiing.fdsmeshlibdemo.ui.base.BaseActivity
import com.linkiing.fdsmeshlibdemo.utils.ConstantUtils


class StartActivity: BaseActivity<ActivityStartBinding>() {
    private val handler = Handler(Looper.getMainLooper())

    override fun initBind(): ActivityStartBinding {
        return ActivityStartBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        if (!this.isTaskRoot) {
            val action = intent.action
            if (intent.hasCategory(Intent.CATEGORY_LAUNCHER) && action == Intent.ACTION_MAIN) {
                finish()
                return
            }
        }

        super.onCreate(savedInstanceState)

        @SuppressLint("SetTextI18n")
        binding.tvTimeVersion.text = "2022-V${ConstantUtils.getAppVerStr(this)}"

        handler.postDelayed({
            goActivity(MainActivity::class.java, true)
        }, 1000)

        LOGUtils.d("StartActivity systemStr ------------------------------------------> " +
                "\nisHarmonyOS:${SystemUtils.isHuawei()}" +
                "\nsystemStr:${SystemUtils.getSystemStr()}")
    }
}