package com.linkiing.fdsmeshlibdemo.ui

import android.os.Bundle
import android.os.PersistableBundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.godox.sdk.api.FDSCommandApi
import com.godox.sdk.bean.FDSColorBlockBean
import com.linkiing.fdsmeshlibdemo.R
import com.linkiing.fdsmeshlibdemo.adapter.ModelAdapter
import com.linkiing.fdsmeshlibdemo.bean.ModelInfo
import com.linkiing.fdsmeshlibdemo.ui.base.BaseActivity
import com.linkiing.fdsmeshlibdemo.utils.ConstantUtils
import com.linkiing.fdsmeshlibdemo.view.dialog.LoadingDialog
import kotlinx.android.synthetic.main.light_fx_list_activity.*
import kotlinx.android.synthetic.main.mode_list_activity.*
import kotlinx.android.synthetic.main.mode_list_activity.recyclerView_v3

/**
 * 修改灯光特效列表页
 */
class LightFXListActivity : BaseActivity() {
    private lateinit var modelAdapterV3: ModelAdapter
    private var modelListV3: MutableList<ModelInfo> = mutableListOf()
    private var address = -1//传入的地址
    private var typeName=""//传入的设备名称与组名称
    private  val fdColorFadeIn:FDSColorBlockBean=FDSColorBlockBean()
    private  val fdColorFadeInList:MutableList<FDSColorBlockBean> = mutableListOf()
    private  val fdColorFlowList:MutableList<FDSColorBlockBean> = mutableListOf()
    private  val fdColorChaseList:MutableList<FDSColorBlockBean> = mutableListOf()
    private val fdsCommandApi:FDSCommandApi=FDSCommandApi.instance

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.light_fx_list_activity)

        initData()
        initView()
        initRecyclerView()
    }

    private fun initView() {
        titleBar.initTitleBar(typeName,0)
    }

    private fun initRecyclerView() {
        modelAdapterV3= ModelAdapter(this,modelListV3)
        val manager = LinearLayoutManager(this)
        manager.orientation = LinearLayoutManager.VERTICAL
        recyclerView_v3.layoutManager = manager
        recyclerView_v3.adapter = modelAdapterV3
        modelAdapterV3.setOnItemClickListener{it,address->
            when(it){
                0->{//闪光灯
                    fdsCommandApi.changeLightFXFlash(address,60,5,1,0,0,0,200,60);
                }
                1->{//雷闪电
                    fdsCommandApi.changeLightFXLightning(address,61,5,2,0,0,210);
                }
                2->{//多云
                    fdsCommandApi.changeLightFXCloudy(address,62,7,3,20)
                }
                3->{//坏灯泡
                    fdsCommandApi.changeLightFXBrokenBulb(address,63,8,4,1,300,51)
                }
                4->{//电视机
                    fdsCommandApi.changeLightFXTV(address,64,9,5,1)
                }
                5->{//蜡烛
                    fdsCommandApi.changeLightFXCandle(address,65,1,6)
                }
                6->{//火
                    fdsCommandApi.changeLightFXFire(address,66,2,7)
                }
                7->{//烟花
                    fdsCommandApi.changeLightFXFirework(address,67,3,8,1)
                }
                8->{//爆炸
                    fdsCommandApi.changeLightFXExplode(address,68,4,9,2,0,1,300,50)
                }
                9->{//焊接
                    fdsCommandApi.changeLightFXWelding(address,69,5,10,1,200,51)
                }
                10->{//警车
                    fdsCommandApi.changeLightFXPoliceCar(address,67,6,2,0)
                }
                11->{//SOS
                    fdsCommandApi.changeLightFXSOS(address,68,7,0,200,52)
                }
                12->{//彩光循环
                    fdsCommandApi.changeLightFXRGBCycle(address,67,8,11,50)
                }
                13->{//激光彩灯
                    fdsCommandApi.changeLightFXLaser(address,68,9,12,51)
                }
                14->{//彩光渐入
                    fdsCommandApi.changeLightFXRGBFadeIn(address,69,1,13,1,2,fdColorFadeIn,fdColorFadeInList)
                }
                15->{//彩光流动
                    fdsCommandApi.changeLightFXRGBFlow(address,70,2,14,1,3,fdColorFlowList)
                }
                16->{//彩光追逐
                    fdsCommandApi.changeLightFXRGBChase(address,71,3,15,2,2,3,fdColorChaseList)
                }
            }
            ConstantUtils.toast(this,getString(R.string.sending_completed_text))
        }
    }

    private fun initData(){
        val bundle=intent.extras
        address= bundle!!.getInt("address")
        typeName= bundle.getString("typeName")!!

        //v3添加
        modelListV3.add(modelData("闪光灯",address))
        modelListV3.add(modelData("雷闪电",address))
        modelListV3.add(modelData("多云",address))
        modelListV3.add(modelData("坏灯泡",address))
        modelListV3.add(modelData("电视机",address))
        modelListV3.add(modelData("蜡烛",address))
        modelListV3.add(modelData("火",address))
        modelListV3.add(modelData("烟花",address))
        modelListV3.add(modelData("爆炸",address))
        modelListV3.add(modelData("焊接",address))
        modelListV3.add(modelData("警车",address))
        modelListV3.add(modelData("SOS",address))
        modelListV3.add(modelData("彩光循环",address))
        modelListV3.add(modelData("激光彩灯",address))
        modelListV3.add(modelData("彩光渐入",address))
        modelListV3.add(modelData("彩光流动",address))
        modelListV3.add(modelData("彩光追逐",address))

        //彩光渐入背景色
        fdColorFadeIn.option=0
        fdColorFadeIn.optionValue=200
        fdColorFadeIn.sat=50

        //彩光渐入色快集合
        fdColorFadeInList.add(setFDSColorBlockBean(1,300,50))
        //彩光流动集合
        fdColorFlowList.add(setFDSColorBlockBean(0,200,51))
        fdColorFlowList.add(setFDSColorBlockBean(1,310,52))
        //彩光追逐集合
        fdColorChaseList.add(setFDSColorBlockBean(0,210,53))
        fdColorChaseList.add(setFDSColorBlockBean(1,320,54))
        fdColorChaseList.add(setFDSColorBlockBean(2,0,0))

    }

    private fun setFDSColorBlockBean(option:Int,optionValue:Int,sat:Int):FDSColorBlockBean{
        val fdsColor=FDSColorBlockBean()
        fdsColor.option=option
        fdsColor.optionValue=optionValue
        fdsColor.sat=sat
        return fdsColor
    }
}