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
import kotlinx.android.synthetic.main.mode_list_activity.*

/**
 * 修改灯光特效列表页
 */
class LightFXListActivity : BaseActivity() {
    private lateinit var modelAdapterV3: ModelAdapter
    private var modelListV3: MutableList<ModelInfo> = mutableListOf()
    private var address=-1//传入的地址
    private  val fdColorFadeIn:FDSColorBlockBean=FDSColorBlockBean()
    private  val fdColorFadeInList:MutableList<FDSColorBlockBean> = mutableListOf()
    private  val fdColorFlowList:MutableList<FDSColorBlockBean> = mutableListOf()
    private  val fdColorChaseList:MutableList<FDSColorBlockBean> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.light_fx_list_activity)
        initData()
        initRecyclerView()
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
                    FDSCommandApi.instance.changeLightFXFlash(address,60,5,1,0,0,0,200,60);
                }
                1->{//雷闪电
                    FDSCommandApi.instance.changeLightFXLightning(address,61,5,2,1,1,210);
                }
                2->{//多云
                    FDSCommandApi.instance.changeLightFXCloudy(address,62,7,3,20)
                }
                3->{//坏灯泡
                    FDSCommandApi.instance.changeLightFXBrokenBulb(address,63,8,4,1,300,51)
                }
                4->{//电视机
                    FDSCommandApi.instance.changeLightFXTV(address,64,9,5,1)
                }
                5->{//蜡烛
                    FDSCommandApi.instance.changeLightFXCandle(address,65,1,6)
                }
                6->{//火
                    FDSCommandApi.instance.changeLightFXFire(address,66,2,7)
                }
                7->{//烟花
                    FDSCommandApi.instance.changeLightFXFirework(address,67,3,8,1)
                }
                8->{//爆炸
                    FDSCommandApi.instance.changeLightFXExplode(address,68,4,9,2,1,1,300,50)
                }
                9->{//焊接
                    FDSCommandApi.instance.changeLightFXWelding(address,69,5,10,0,200,51)
                }
                10->{//警车
                    FDSCommandApi.instance.changeLightFXPoliceCar(address,67,6,2,2)
                }
                11->{//SOS
                    FDSCommandApi.instance.changeLightFXSOS(address,68,7,0,200,52)
                }
                12->{//彩光循环
                    FDSCommandApi.instance.changeLightFXRGBCycle(address,67,8,11,50)
                }
                13->{//激光彩灯
                    FDSCommandApi.instance.changeLightFXLaser(address,68,9,12,51)
                }
                14->{//彩光渐入
                    FDSCommandApi.instance.changeLightFXRGBFadeIn(address,69,1,13,0,2,fdColorFadeIn,fdColorFadeInList)
                }
                15->{//彩光流动
                    FDSCommandApi.instance.changeLightFXRGBFlow(address,70,2,14,1,3,fdColorFlowList)
                }
                16->{//彩光追逐
                    FDSCommandApi.instance.changeLightFXRGBChase(address,71,3,15,2,2,3,fdColorChaseList)
                }
            }
        }
    }

    private fun initData(){
        //v3添加
        modelListV3.add(modelData(2,"闪光灯",address))
        modelListV3.add(modelData(2,"雷闪电",address))
        modelListV3.add(modelData(2,"多云",address))
        modelListV3.add(modelData(2,"坏灯泡",address))
        modelListV3.add(modelData(2,"电视机",address))
        modelListV3.add(modelData(2,"蜡烛",address))
        modelListV3.add(modelData(2,"火",address))
        modelListV3.add(modelData(2,"烟花",address))
        modelListV3.add(modelData(2,"爆炸",address))
        modelListV3.add(modelData(2,"焊接",address))
        modelListV3.add(modelData(2,"警车",address))
        modelListV3.add(modelData(2,"SOS",address))
        modelListV3.add(modelData(2,"彩光循环",address))
        modelListV3.add(modelData(2,"激光彩灯",address))
        modelListV3.add(modelData(2,"彩光渐入",address))
        modelListV3.add(modelData(2,"彩光流动",address))
        modelListV3.add(modelData(2,"彩光追逐",address))

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