package com.linkiing.fdsmeshlibdemo.ui

import android.os.Bundle
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.recyclerview.widget.LinearLayoutManager
import com.base.mesh.api.utils.SendQueueUtils
import com.godox.agm.GodoxCommandApi
import com.godox.agm.callback.*
import com.godox.sdk.model.FDSNodeInfo
import com.linkiing.fdsmeshlibdemo.R
import com.linkiing.fdsmeshlibdemo.adapter.ModelAdapter
import com.linkiing.fdsmeshlibdemo.bean.ModelInfo
import com.linkiing.fdsmeshlibdemo.bean.SeekBarBean
import com.linkiing.fdsmeshlibdemo.ui.base.BaseActivity
import com.linkiing.fdsmeshlibdemo.utils.ConstantUtils
import com.linkiing.fdsmeshlibdemo.view.dialog.LoadingDialog
import com.base.mesh.api.log.LOGUtils
import kotlinx.android.synthetic.main.mode_list_activity.*

/**
 * 功能列表页
 */
class ModeListActivity : BaseActivity(), FirmwareCallBack, BatteryPowerCallBack, MCUCallBack {
    private lateinit var modelAdapter: ModelAdapter
    private lateinit var modelAdapterV3: ModelAdapter
    private var modelList: MutableList<ModelInfo> = mutableListOf()
    private var modelListV3: MutableList<ModelInfo> = mutableListOf()
    private var address = -1//传入的地址
    private var typeName = ""//传入的设备名称与组名称
    private var isTestOnOffStart = false
    private lateinit var loadingDialog: LoadingDialog
    private val fdsCommandApi = GodoxCommandApi.instance
    private val sendQueueUtils = SendQueueUtils.instance

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.mode_list_activity)

        initData()
        initView()
        initRecyclerView()
        initSeekBar()
        initLister()
    }

    private fun initView() {
        loadingDialog = LoadingDialog(this)
        mode_titleBar.initTitleBar(typeName, 0)
    }

    private fun initData() {
        val bundle = intent.extras
        address = bundle!!.getInt("address")
        typeName = bundle.getString("typeName")!!
        //v2添加
        modelList.add(modelData("修改灯光开关", address))
        modelList.add(modelData("修改灯光RGBW", address))
        modelList.add(modelData("修改灯光CCT", address))
        modelList.add(modelData("修改灯光HSI", address))
        modelList.add(modelData("修改灯光色卡", address))
        modelList.add(modelData("修改灯光特效", address))
        modelList.add(modelData("修改设备风扇", address))
        modelList.add(modelData("获取蓝牙固件版本", address))
        modelList.add(modelData("获取电池电量信息", address))

        //v3添加
        modelListV3.add(modelData("修改灯光特效", address))
        modelListV3.add(modelData("修改灯光色卡", address))
        modelListV3.add(modelData("修改灯光XY", address))
        modelListV3.add(modelData("修改灯光RGBW", address))
        modelListV3.add(modelData("修改灯光RGBWW", address))
        modelListV3.add(modelData("修改灯光RGBACL", address))
        modelListV3.add(modelData("修改亮度偏移", address))
        modelListV3.add(modelData("获取mucu固件版本", address))
        modelListV3.add(modelData("修改灯光RGBW-2", address)) //changeLightRGBWEx2
        modelListV3.add(modelData("修改灯光色卡-2", address)) //changeLightCardEx2
        modelListV3.add(modelData("修改灯光XY-EX", address)) //changeLightXYEx
    }

    private fun initRecyclerView() {
        modelAdapter = ModelAdapter(this, modelList)
        val manager = LinearLayoutManager(this)
        manager.orientation = LinearLayoutManager.VERTICAL
        recyclerView_v2.layoutManager = manager
        recyclerView_v2.adapter = modelAdapter
        var isSwitch = true;
        modelAdapter.setOnItemClickListener { it, address ->
            when (it) {
                0 -> {//修改灯光开关
                    isSwitch = !isSwitch
                    fdsCommandApi.changeLightSwitch(address, isSwitch)
                }

                1 -> {//修改灯光RGBW
                    fdsCommandApi.changeLightRGBW(address, 50, 5, 200, 200, 200, 0)
                }

                2 -> {//修改灯光CCT
                    fdsCommandApi.changeLightCCT(address, 51, 6, 50, 20, 1, 1)
                }

                3 -> {//修改灯光HSI
                    fdsCommandApi.changeLightHSI(address, 52, 7, 300, 50, 1)
                }

                4 -> {//修改灯光色卡
                    fdsCommandApi.changeLightCard(address, 53, 8, 0, 200, 1, 2)
                }

                5 -> {//修改灯光特效
                    fdsCommandApi.changeLightFX(address, 54, 9, 15, 1)
                }

                6 -> {//修改设备风扇
                    fdsCommandApi.changeElectricFan(address, 2, 1)
                }

                7 -> {//获取蓝牙固件版本
                    loadingDialog.showDialog(3000)
                    fdsCommandApi.getFirmwareVersion(address, this)
                }

                8 -> {//获取电池电量信息
                    loadingDialog.showDialog(3000)
                    fdsCommandApi.getBatteryPower(address, this)
                }
            }
            if (it in 0..6) {
                ConstantUtils.toast(this, getString(R.string.sending_completed_text))
            }
        }

        modelAdapterV3 = ModelAdapter(this, modelListV3)
        val managerV3 = LinearLayoutManager(this)
        managerV3.orientation = LinearLayoutManager.VERTICAL
        recyclerView_v3.layoutManager = managerV3
        recyclerView_v3.adapter = modelAdapterV3
        modelAdapterV3.setOnItemClickListener { it, address ->
            when (it) {
                0 -> {//修改灯光特效，跳转
                    val bundle = Bundle()
                    bundle.putInt("address", address)
                    bundle.putString("typeName", typeName);
                    goActivityBundle(LightFXListActivity::class.java, false, bundle)
                }

                1 -> {//修改灯光色卡
                    fdsCommandApi.changeLightCardEx(address, 55, 5, 1, 10, 1, 0, 0)
                }

                2 -> {//修改灯光XY
                    fdsCommandApi.changeLightXY(address, 56, 6, 1100, 2200)
                }

                3 -> {//修改灯光RGBW
                    fdsCommandApi.changeLightRGBWEx(address, 57, 7, 0, 50, 50, 50, 50, 0xff, 0xff)
                }

                4 -> {//修改灯光RGBWW
                    fdsCommandApi.changeLightRGBWEx(address, 58, 8, 1, 60, 60, 60, 60, 60, 0xff)
                }

                5 -> {//修改灯光RGBACL
                    fdsCommandApi.changeLightRGBWEx(address, 59, 9, 2, 70, 70, 70, 70, 70, 70)
                }

                6 -> {//修改亮度偏移
                    fdsCommandApi.changeBrightnessOffset(address, 25, 5)
                }

                7 -> {//获取mucu固件版本
                    loadingDialog.showDialog(3000)
                    fdsCommandApi.getMcuVersion(address, this)
                }

                8 -> {//修改灯光RGBW-2
                    fdsCommandApi.changeLightRGBWEx2(
                        address, 57, 7, 0, 0x50, 0x3050, 0x4090, 0xff50, 0xffee, 0x10ff
                    )
                }

                9 -> {//修改灯光色卡-2
                    fdsCommandApi.changeLightCardEx2(address, 55, 5, 1, 10, 1, 0, 0, 0, 1)
                }

                10 -> {//修改灯光XY-EX
                    fdsCommandApi.changeLightXYEx(address, 56, 6, 1100, 2200, 1)
                }
            }
            if (it in 1..6 || it in 8..10) {
                ConstantUtils.toast(this, getString(R.string.sending_completed_text))
            }
        }
    }

    private fun initSeekBar() {/*
         * 注：非直连节点，同步性时间间隔，取决于固件命令处理时间间隔。
         */
        sendQueueUtils.setSamplingTime(200)//数据采样间隔
            .start {
                if (it is SeekBarBean) {
                    when (it.model) {
                        0 -> {
                            val color = (0xFFFFFF * (it.value / 100.0f)).toInt()
                            fdsCommandApi.changeLightRGBW(
                                address,
                                10,
                                0,
                                (color shr 16) and 0xFF,
                                (color shr 8) and 0xFF,
                                color and 0xFF,
                                0
                            )
                        }

                        1 -> {
                            fdsCommandApi.changeLightXY(
                                address,
                                it.value,
                                0,
                                (9999 * (it.value / 100.0f)).toInt(),
                                (9999 * ((100 - it.value) / 100.0f)).toInt(),
                            )
                        }

                        3 -> {
                            fdsCommandApi.changeLightSwitch(address, it.value % 2 == 0)
                        }
                    }
                }
            }

        seekbarBrightness_v2?.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                //isSmall 是否是小包数据
                sendQueueUtils.addDataSampling(SeekBarBean(0, progress))
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                val progress = seekBar?.progress ?: 0
                sendQueueUtils.clearQueueData()
                sendQueueUtils.addData(SeekBarBean(0, progress))
            }
        })

        seekbarBrightness_v3?.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                sendQueueUtils.addDataSampling(SeekBarBean(1, progress))
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                val progress = seekBar?.progress ?: 0
                sendQueueUtils.clearQueueData()
                sendQueueUtils.addData(SeekBarBean(1, progress))
            }
        })
    }

    private fun initLister() {
        bt_test1?.setOnClickListener {
            testOnOff()
        }
    }

    private fun testOnOff() {
        if (isTestOnOffStart) {
            return
        }
        isTestOnOffStart = true

        Thread {
            var c = 0
            var p = 0
            while (c < 200) {
                if (sendQueueUtils.addDataSampling(SeekBarBean(3, p))) {
                    p++
                    c++
                }
            }
            isTestOnOffStart = false
        }.start()
    }

    /**
     * 电量回调
     *
     * @param fdsNodeInfo 节点信息
     * @param state 1-未充电 2-充电中 其它-未知
     * @param hour 使用时间小时部分
     * @param minute 使用时间分钟部分
     * @param option 0-电量百分比，1-电量格子
     * @param power 如果option为0则范围0-100，如果option为1则范围0-3
     */
    override fun onSuccess(
        fdsNodeInfo: FDSNodeInfo, state: Int, hour: Int, minute: Int, option: Int, power: Int
    ) {
        loadingDialog.dismissDialog()
        val msg =
            "设备地址：${fdsNodeInfo.macAddress} 充电状态：${state} 使用时间小时部分：${hour} 使用时间分钟部分：${minute}，电量格式：${option}，电量：${power}"
        LOGUtils.d(msg)
        ConstantUtils.toast(this, msg)
    }

    /**
     * 蓝牙固件版本回调
     * @param fdsNodeInfo 节点信息
     * @param version 固件版本
     */
    override fun onSuccess(fdsNodeInfo: FDSNodeInfo, version: Int, isPa: Boolean) {
        loadingDialog.dismissDialog()
        val msg = "固件版本:$version"
        LOGUtils.d(msg)
        ConstantUtils.toast(this, msg)
    }

    /**
     * MCU固件版本回调
     * @param fdsNodeInfo 节点信息
     * @param productVersion 产品版本 （表示产品的版本信息）
     * @param mcuVersion MCU方案版本 （用于区分同一个型号产品使用的不同的MCU平台）
     */
    override fun onSuccess(fdsNodeInfo: FDSNodeInfo, productVersion: String, mcuVersion: String) {
        loadingDialog.dismissDialog()
        val msg = "产品版本:$productVersion  MCU方案版本:$mcuVersion"
        LOGUtils.d(msg)
        ConstantUtils.toast(this, msg)
    }

    override fun onDestroy() {
        super.onDestroy()
        sendQueueUtils.destroy()
    }
}