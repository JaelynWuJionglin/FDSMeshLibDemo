package com.linkiing.fdsmeshlibdemo.bean

class ModelInfo(
    /**
     * 类型名字
     */
    var name:String="",
    /**
     *  0=v2接口调用，1=v3接口调用,2=修改灯光特效中的v3接口调用
     */
    var type:Int=0,
    /**
     * 设备/组地址
     */
    var address:Int=-1,
)