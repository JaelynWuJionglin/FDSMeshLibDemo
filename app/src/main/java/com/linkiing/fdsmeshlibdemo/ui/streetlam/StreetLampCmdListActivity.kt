package com.linkiing.fdsmeshlibdemo.ui.streetlam

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.recyclerview.widget.LinearLayoutManager
import com.godox.sdk.model.FDSNodeInfo
import com.linkiing.fdsmeshlibdemo.R
import com.linkiing.fdsmeshlibdemo.adapter.ModelAdapter
import com.linkiing.fdsmeshlibdemo.bean.ModelInfo
import com.linkiing.fdsmeshlibdemo.ui.base.BaseActivity
import com.linkiing.fdsmeshlibdemo.utils.ConstantUtils
import com.linkiing.fdsmeshlibdemo.view.dialog.LoadingDialog
import com.street.lamp.StreetLampCmdApi
import com.street.lamp.callback.SLBatterySwitchCallback
import com.street.lamp.callback.SLBatteryValueCallback
import com.street.lamp.callback.SLDimmingValueCallback
import com.street.lamp.callback.SLSamplingValueCallback
import com.street.lamp.callback.SLSbBatterySwitchCallback
import com.street.lamp.callback.SLTempValueCallback
import kotlinx.android.synthetic.main.street_lamp_activity.*

/**
 * 路灯测试 - 功能列表页
 */
class StreetLampCmdListActivity : BaseActivity(),
    OnSeekBarChangeListener,
    SLDimmingValueCallback,
    SLBatteryValueCallback,
    SLTempValueCallback,
    SLBatterySwitchCallback,
    SLSbBatterySwitchCallback,
    SLSamplingValueCallback {
    private lateinit var modelAdapter: ModelAdapter
    private var modelList: MutableList<ModelInfo> = mutableListOf()
    private var address = -1//传入的地址
    private var typeName = ""//传入的设备名称与组名称
    private lateinit var loadingDialog: LoadingDialog
    private val streetLampCmdApi = StreetLampCmdApi.instance

    private var dimming1 = 0
    private var dimming2 = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.street_lamp_activity)

        initData()
        initView()
        initRecyclerView()
        initListener()
    }

    private fun initView() {
        loadingDialog = LoadingDialog(this)
        mode_titleBar.initTitleBar(typeName, 0)
    }

    private fun initData() {
        val bundle = intent.extras
        address = bundle!!.getInt("address")
        typeName = bundle.getString("typeName")!!
        modelList.add(modelData("获取调光参数", address))
        modelList.add(modelData("获取电参数", address))
        modelList.add(modelData("获取温度", address))
        modelList.add(modelData("获取开关量", address))
        modelList.add(modelData("获取采样参数", address))
    }

    private fun initRecyclerView() {
        modelAdapter = ModelAdapter(this, modelList)
        val manager = LinearLayoutManager(this)
        manager.orientation = LinearLayoutManager.VERTICAL
        recyclerView_v2.layoutManager = manager
        recyclerView_v2.adapter = modelAdapter
        modelAdapter.setOnItemClickListener { it, address ->
            when (it) {
                0 -> {
                    //获取调光参数
                    streetLampCmdApi.getDimmingValue(address, this)
                }

                1 -> {
                    //获取电参数
                    streetLampCmdApi.getBatteryValue(address, this)
                }

                2 -> {
                    //获取温度
                    streetLampCmdApi.getTempValue(address, this)
                }

                3 -> {
                    //获取开关量
                    streetLampCmdApi.getBatterySwitch(address, this)
                }

                4 -> {
                    //获取采样参数
                    streetLampCmdApi.getSamplingValue(address, this)
                }
            }
            if (it in 0..6) {
                ConstantUtils.toast(this, getString(R.string.sending_completed_text))
            }
        }
    }

    private fun initListener() {
        seekbarBrightness1.setOnSeekBarChangeListener(this)
        seekbarBrightness2.setOnSeekBarChangeListener(this)

        streetLampCmdApi.setSLSbBatterySwitchCallback(this)

        bt_cy_set?.setOnClickListener {
            val value1 = et_dianzu?.text?.toString()?.trim()?.toInt() ?: 0
            val value2 = et_hugan?.text?.toString()?.trim()?.toInt() ?: 0
            val value3 = et_dianya?.text?.toString()?.trim()?.toInt() ?: 0
            val value4 = et_qianduan?.text?.toString()?.trim()?.toInt() ?: 0
            val value5 = et_bianbi?.text?.toString()?.trim()?.toInt() ?: 0

            streetLampCmdApi.setSamplingValue(address,value1,value2,value3,value4,value5)
        }
    }

    override fun onDimmingValue(fdsNodeInfo: FDSNodeInfo, dimming1: Int, dimming2: Int) {
        if (fdsNodeInfo.meshAddress == address) {
            this.dimming1 = dimming1
            this.dimming2 = dimming2

            seekbarBrightness1?.progress = dimming1
            tv_seekbarBrightness1?.text = "$dimming1"
            seekbarBrightness2?.progress = dimming2
            tv_seekbarBrightness2?.text = "$dimming2"
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onBatteryValue(
        fdsNodeInfo: FDSNodeInfo,
        voltage: Float,
        electricCurrent: Float,
        power: Float,
        powerFactor: Int
    ) {
        if (fdsNodeInfo.meshAddress == address) {
            tv_voltage?.text = "电压:${voltage}V"
            tv_electricCurrent?.text = "电流:${electricCurrent}A"
            tv_power?.text = "有功功率:${power}kw"
            tv_powerFactor?.text = "功率因子:$powerFactor"
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onTempValue(fdsNodeInfo: FDSNodeInfo, temp: Float) {
        if (fdsNodeInfo.meshAddress == address) {
            tv_temp?.text = "温度: $temp ℃"
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onBatterySwitch(
        fdsNodeInfo: FDSNodeInfo,
        switchStatus1: Int,
        switchStatus2: Int,
        switchStatus3: Int
    ) {
        if (fdsNodeInfo.meshAddress == address) {
            tv_kaiguan1?.text = "获取: $switchStatus1"
            tv_kaiguan2?.text = "获取: $switchStatus2"
            tv_kaiguan3?.text = "获取: $switchStatus3"
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onSbBatterySwitch(
        fdsNodeInfo: FDSNodeInfo,
        switchStatus1: Int,
        switchStatus2: Int,
        switchStatus3: Int
    ) {
        if (fdsNodeInfo.meshAddress == address) {
            tv_sb_kaiguan1?.text = "上报: $switchStatus1"
            tv_sb_kaiguan2?.text = "上报: $switchStatus2"
            tv_sb_kaiguan3?.text = "上报: $switchStatus3"
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onSamplingValue(
        fdsNodeInfo: FDSNodeInfo,
        value1: Int,
        value2: Int,
        value3: Int,
        value4: Int,
        value5: Int
    ) {
        if (fdsNodeInfo.meshAddress == address) {
            et_dianzu?.setText("$value1")
            et_hugan?.setText("$value2")
            et_dianya?.setText("$value3")
            et_qianduan?.setText("$value4")
            et_bianbi?.setText("$value5")
        }
    }

    //SeekBar ------------------------------------------------------------------------------------
    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {}

    override fun onStartTrackingTouch(seekBar: SeekBar?) {}

    override fun onStopTrackingTouch(seekBar: SeekBar?) {
        if (seekBar == null) {
            return
        }
        when (seekBar.id) {
            R.id.seekbarBrightness1 -> {
                dimming1 = seekBar.progress
                tv_seekbarBrightness1?.text = "$dimming1"
                streetLampCmdApi.setDimmingValue(address, dimming1, dimming2)
            }

            R.id.seekbarBrightness2 -> {
                dimming2 = seekBar.progress
                tv_seekbarBrightness2?.text = "$dimming2"
                streetLampCmdApi.setDimmingValue(address, dimming1, dimming2)
            }
        }
    }

    //SeekBar ------------------------------------------------------------------------------------

    override fun onDestroy() {
        super.onDestroy()
        streetLampCmdApi.removeSLSbBatterySwitchCallback()
    }
}