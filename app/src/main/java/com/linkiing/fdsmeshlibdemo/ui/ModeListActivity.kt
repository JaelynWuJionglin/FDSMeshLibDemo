package com.linkiing.fdsmeshlibdemo.ui

import android.os.Bundle
import android.os.PersistableBundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.godox.sdk.api.FDSCommandApi
import com.linkiing.fdsmeshlibdemo.R
import com.linkiing.fdsmeshlibdemo.adapter.ModelAdapter
import com.linkiing.fdsmeshlibdemo.bean.ModelInfo
import com.linkiing.fdsmeshlibdemo.ui.base.BaseActivity
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.mode_list_activity.*

/**
 * 功能列表页
 */
class ModeListActivity:BaseActivity() {
    private lateinit var modelAdapter: ModelAdapter
    private lateinit var modelAdapterV3: ModelAdapter
    private var modelList: MutableList<ModelInfo> = mutableListOf()
    private var modelListV3: MutableList<ModelInfo> = mutableListOf()
    private var address=-1//传入的地址
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.mode_list_activity)

        initData()
        initRecyclerView()
    }

    override fun onResume() {
        super.onResume()
//        modelAdapter.updateModeList(modelList)
//        modelAdapterV3.updateModeList(modelListV3)

    }
    private fun initRecyclerView() {
        modelAdapter= ModelAdapter(this,modelList)
        val manager = LinearLayoutManager(this)
        manager.orientation = LinearLayoutManager.VERTICAL
        recyclerView_v2.layoutManager = manager
        recyclerView_v2.adapter = modelAdapter
        modelAdapter.setOnItemClickListener{it,address->
            when(it){
                0->{//修改灯光开关
                    FDSCommandApi.instance.changeLightSwitch(address,false)
                }
                1->{//修改灯光RGBW
                    FDSCommandApi.instance.changeLightRGBW(address,50,5,200,200,200,0)
                }
                2->{//修改灯光CCT
                    FDSCommandApi.instance.changeLightCCT(address,51,6,50,25,1,1)
                }
                3->{//修改灯光HSI
                    FDSCommandApi.instance.changeLightHSI(address,52,7,200,50,0)
                }
                4->{//修改灯光色卡
                    FDSCommandApi.instance.changeLightCard(address,53,8,0,200,1,2)
                }
                5->{//修改灯光特效
                    FDSCommandApi.instance.changeLightFX(address,54,9,12,1)
                }

            }
        }

        modelAdapterV3= ModelAdapter(this,modelListV3)
        val managerV3 = LinearLayoutManager(this)
        managerV3.orientation = LinearLayoutManager.VERTICAL
        recyclerView_v3.layoutManager = managerV3
        recyclerView_v3.adapter = modelAdapterV3
        modelAdapterV3.setOnItemClickListener{it,address->
            when(it){
                0->{//修改灯光特效，跳转
                    goActivity(LightFXListActivity::class.java, false)
                }
                1->{//修改灯光色卡
                    FDSCommandApi.instance.changeLightCardEx(address,55,5,1,220,1,0,0);
                }
                2->{//修改灯光XY
                    FDSCommandApi.instance.changeLightXY(address,56,6,1100,2200)
                }
                3->{//修改灯光RGBW
                    FDSCommandApi.instance.changeLightRGBWEx(address,57,7,0,50,50,50,50,0xff,0xff)
                }
                4->{//修改灯光RGBWW
                    FDSCommandApi.instance.changeLightRGBWEx(address,58,8,1,60,60,60,60,60,0xff)
                }
                5->{//修改灯光RGBACL
                    FDSCommandApi.instance.changeLightRGBWEx(address,59,9,0,70,70,70,70,70,70)
                }
                6->{//修改亮度偏移
                    FDSCommandApi.instance.changeBrightnessOffset(address,25,5)
                }

            }
        }
    }

    private fun initData(){

        //v2添加
        modelList.add(modelData(0,"修改灯光开关",address))
        modelList.add(modelData(0,"修改灯光RGBW",address))
        modelList.add(modelData(0,"修改灯光CCT",address))
        modelList.add(modelData(0,"修改灯光HSI",address))
        modelList.add(modelData(0,"修改灯光色卡",address))
        modelList.add(modelData(0,"修改灯光特效",address))

        //v3添加
        modelListV3.add(modelData(1,"修改灯光特效",address))
        modelListV3.add(modelData(1,"修改灯光色卡",address))
        modelListV3.add(modelData(1,"修改灯光XY",address))
        modelListV3.add(modelData(1,"修改灯光RGBW",address))
        modelListV3.add(modelData(1,"修改灯光RGBWW",address))
        modelListV3.add(modelData(1,"修改灯光RGBACL",address))
        modelListV3.add(modelData(1,"修改亮度偏移",address))

    }

}