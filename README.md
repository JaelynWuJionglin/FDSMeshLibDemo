# Android Mesh SDK说明⽂档
## ⼀.SDK说明
### 1，mesh核心库
GodoxMeshLib_v1.2.3  
LsTiBleMeshLib_v1.2.3   

### 2，mesh数据协议库
GodoxAgmLib_v1.1.2  

### 3，⽀持的Android版本：
Android5.0以上（包含）

### 4，开发语⾔：
kotlin Java

### 5，编译环境：
gradle-7.3.3

## ⼆.⼯程介绍
### 1.下载和集成
代码仓库：  
https://github.com/JaelynWuJionglin/FDSMeshLibDemo.git  
aar路径：  
FDSMeshLibDemo -> App -> libs

### 2.权限配置
注意：蓝牙权限动态需动态申请，并且需要开启GPS，具体参考Demo

```
<!-- 网络 -->  
<uses-permission android:name="android.permission.INTERNET" />  
<!-- 定位 蓝牙需要 -->  
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />   
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />  
<!-- 蓝牙 -->  
<uses-permission android:name="android.permission.BLUETOOTH"  
  android:maxSdkVersion="30" />  
  <uses-permission android:name="android.permission.BLUETOOTH_ADMIN"   
  android:maxSdkVersion="30" />  
<uses-permission android:name="android.permission.BLUETOOTH_SCAN" />  
<uses-permission android:name="android.permission.BLUETOOTH_CONNECT" />  
``` 

### 3.SDK集成配置
（1）build.gradle中配置

```
  //引入aar包  
  implementation fileTree(dir: 'libs', include: ['*.aar'])  
  //加密（SDK需要） 
  implementation 'com.madgag.spongycastle:core:1.58.0.0'  
  implementation 'com.madgag.spongycastle:prov:1.58.0.0'   
  //gson（SDK需要）  
  implementation "com.google.code.gson:gson:${project.gson_version}"   
```

（2）Sdk的MeshApp继承自Application，需要将app的Application继承MeshApp。

（3）MeshConfigure为sdk的初始化配置，在Application中，建议使用demo中的默认配置。

（4）初始化mesh数据

```
 App.instance.initMeshData()
 注意：会获取Android_ID等设备信息，生成唯一ID，故首次启动app，需要在用户同意隐私政策后调用，避免商店上架问题。
```

（5）混淆
```
-keep class org.spongycastle** {*;}  
-keep class com.godox.sdk** {*;}  
-keep class com.base.mesh.api** {*;}  
-keep class com.telink.ble.mesh** {*;}  
```

## 三. Api接⼝
### 1，设备搜索
接⼝类：FDSSearchDevicesApi
调⽤⽅式：类⽅法调⽤

```

/**
 * 扫描设备
 * @param filterName 基于设备的localname进行过滤，传空默认不过滤
 * @param scanOutTime 扫描超时时间
 * @param callBack 蓝牙外设对象回调
 */
fun startScanDevice(context: Context,filterName: String, scanOutTime: Long, callBack: FDSBleDevCallBack)

/**
 * 停止扫描
 */
fun stopScan()

/**
 * 销毁释放资源
 */
fun destroy()

```

### 2.设备入网和退网
接⼝类：FDSAddOrRemoveDeviceApi
调⽤⽅式：类⽅法调⽤

```

/**
 * 设备组网
 * @param advertisingDevice 蓝牙外设对象
 * @param fdsAddNetworkCallBack 组网回调
 */
fun deviceAddNetWork(
    advertisingDevice: AdvertisingDevice,
    fdsAddNetworkCallBack: FDSAddNetWorkCallBack
)

fun deviceAddNetWork(
        advertisingDeviceList: MutableList<AdvertisingDevice>,
        fdsAddNetworkCallBack: FDSAddNetWorkCallBack
)

/**
 * 移除节点(退网)
 * @param fdsNodeInfo 节点
 * @param isSupportOutOfLine 是否支持删除离线节点
 * @param fdsRemoveNodeCallBack true表示删除成功，false表示删除失败
 */
fun deviceRemoveNetWork(
    fdsNodeInfo: FDSNodeInfo,
    isSupportOutOfLine: Boolean,
    fdsRemoveNodeCallBack: FDSRemoveNodeCallBack
)

fun deviceRemoveNetWork(
    fdsNodeInfoList: MutableList<FDSNodeInfo>,
    isSupportOutOfLine: Boolean,
    fdsRemoveNodeCallBack: FDSRemoveNodeCallBack
)

/**
 * 销毁资源
 */
fun destroy()

```

### 3，MeshApi
接⼝类： FDSMeshApi
调⽤⽅式：单例模式

```
/**
 * 设置接入使用需要申请AppId
 * @param appId 应用AppId
 */
fun setWithAppId(appId: String)

/**
 * 获取SDK版本号
 * @return 版本x.x.x
 */
fun getVersion(): String

/**
 * 设置mesh参数配置，初始化时调用。不设置则为默认。
 */
fun setMeshConfigure(meshConfigure: MeshConfigure)

/**
 * 设置Mesh发送数据包承载模式（需要固件支持）
 * NONE:默认都不使用长包
 * GATT:直连节点长包
 * GATT_ADV:全部长包
 */
fun resetExtendBearerMode(extendBearerMode: ExtendBearerMode)

/**
 * 启用SDK日志
 * @param isOpen true表示启用，false表示关闭
 * @param isSave true表示保存日志，false表示不保存日志
 */
fun initSdkLog(isOpen: Boolean, isSave: Boolean)

/**
 * 获取节点列表
 * @return 节点列表
 */
fun getFDSNodes(): MutableList<FDSNodeInfo>

/**
 * 获取组列表
 * @return 组列表
 */
fun getGroups(): MutableList<FDSGroupInfo>

/**
 * 获取订阅组的设备节点列表
 * @param address 组地址
 * @return 节点列表
 */
fun getGroupFDSNodes(address: Int): MutableList<FDSNodeInfo>

/**
 * 获取未订阅组的节点列表
 * @return 节点列表
 */
fun getFDSNodeWhitOutGroup(): MutableList<FDSNodeInfo>

/**
 * 获取直连节点
 * @return 直连节点
 */
fun getConnectedFDSNodeInfo(): FDSNodeInfo?

/**
 * 根据MAC地址获取指定节点
 * @param macAddress 节点MAC地址
 * @return
 */
fun getFDSNodeInfoByMacAddress(macAddress: String): FDSNodeInfo?

/**
 * 根据节点mesh地址获取指定节点
 * @param meshAddress 节点mesh地址
 * @return FDSNodeInfo
 */
fun getFDSNodeInfoByMeshAddress(meshAddress: Int): FDSNodeInfo?

/**
 * 根据组地址获取组
 * @param address 组地址
 * @return
 */
fun getGroupByAddress(address: Int): FDSGroupInfo?

/**
 * 获取设备订阅的组
 * @param meshAddress 设备地址
 * @return
 */
fun getGroupByDeviceAddressFirst(meshAddress: Int): FDSGroupInfo?

/**
 * 重命名节点名称和类型
 * @param renameList 要修改的设备信息列表
 *        renameBean.meshAddress 节点地址
 *        renameBean.name 节点名称
 *        renameBean.type 节点类型
 * @return true表示重命名成功，false表示重命名失败
 */
fun renameFDSNodeInfo(renameList: MutableList<RenameBean>): Boolean

/**
 * 创建组
 * @param name 组名称
 * @return 组对象，不为null则为创建成功
 */
fun createGroup(name: String): FDSGroupInfo?

/**
 * 重命名组
 * @param groupInfo 组
 * @param name 组名称
 * @return true表示重命名成功，false表示重命名失败
 */
fun renameGroup(groupInfo: FDSGroupInfo, name: String): Boolean

/**
 * 移除组
 * @param groupInfo 组
 * @return true表示删除成功，false表示删除失败
 */
fun removeGroup(groupInfo: FDSGroupInfo): Boolean

/**
 * 配置节点“订阅/取消订阅”组
 * @param fdsNodeInfo 节点
 * @param groupInfo 组
 * @param isSubscribe true表示订阅到组，false表示取消订阅
 * @param subscribeListener true表示成功，false表示失败
 */
fun configSubscribe(
    fdsNodeInfo: FDSNodeInfo,
    groupInfo: FDSGroupInfo,
    isSubscribe: Boolean,
    subscribeListener: (Boolean) -> Unit
)

/**
 * 检测和刷新节点在线状态
 */
fun refreshFDSNodeInfoState(): Boolean


/**
 * 设置节点在线状态改变的监听
 */
fun addFDSNodeStatusChangeCallBack(
fdsNodeStatusChangeListener: NodeStatusChangeListener)

/**
 * 取消节点在线状态改变的监听
 */
fun removeFDSNodeStatusChangeCallBack(
fdsNodeStatusChangeListener: NodeStatusChangeListener)

/**
 * 配置节点自动上报在线状态
 * @param isOn true表示开启自动上报，false表示取消自动上报
 * @param fdsNodeInfo 节点
 */
fun configFDSNodePublishState(isOn: Boolean, fdsNodeInfo: FDSNodeInfo): Boolean

/**
 * 发送消息数据
 * @param address 发送地址
 * @param data 消息数据
 * @param responseOpcode 回响应操作码 - #{SendCmdUtils.RES_OPCODE_F0，SendCmdUtils.RES_OPCODE_F1}
 */
fun sendData(address: Int, data: ByteArray, responseOpcode: Int): Boolean

/**
 * 发送数据响应
 * @param fdsResponseCallBack 发送数据响应回调
 */
fun setResponseDataCallBack(fdsResponseCallBack: FDSResponseCallBack)

/**
 * 获取当前使用的Mesh信息
 */
fun getCurrentMeshInfo(): MeshInfo

/**
 * 获取app唯一uuid
 */
fun getAppLocalUUID(): String

/**
 * 根据json解析出部分有用的信息
 * @param meshJson mesh导出的json数据
 */
fun getMeshInfoByJson(meshJson: String): MeshJsonInfo

/**
 * 更新json中的ProvisionAddress
 * @param meshJson 组网JSON信息
 * @param newProvisionerAddress 新的provisionAddress （<=Ox7FFF）
 */
fun updateMeshJsonProvisionerAddress(meshJson: String, newProvisionerAddress: Int): String

/**
 * 更新ProvisionAddress后再导入/替换Mesh信息
 * @param meshJson 组网JSON信息
 * @param newProvisionerAddress 新的provisionAddress （<=Ox7FFF）
 */
fun updateAndImportMeshJson(meshJson: String, newProvisionerAddress: Int): Boolean

/**
 * 导入/替换Mesh信息
 * @param meshJson 组网JSON信息
 */
fun importMeshJson(meshJson: String): Boolean

/**
 * 获取初始Mesh信息（新增场景时需要）
 * @return Mesh Json字符串
 */
fun getInitMeshJson(): String

/**
 * 获取当前Mesh信息（保存或分享场景时需要）
 * @return Mesh Json字符串
 */
fun getCurrentMeshJson(): String

/**
 * 开启OTA升级
 * @param otaData 固件数据
 * @param fdsNodeInfo 节点
 * @param listener OTA升级回调
 * @return true表示开启成功，false表示开启失败
 */
fun startOTAWithOtaData(
    otaData: ByteArray,
    fdsNodeInfo: FDSNodeInfo,
    listener: MeshOtaListener
): Boolean

/**
 * 结束OTA升级
 */
fun stopOTA()

/**
 * 开启MCU OTA升级
 * @param otaData 固件数据
 * @param version 固件版本
 * @param fdsNodeInfo 节点
 * @param listener MCU OTA升级回调
 * @return  true表示开启成功，false表示开启失败
 */
fun startMcuOTAWithOtaData(
    otaData: ByteArray,
    version: Int,
    fdsNodeInfo: FDSNodeInfo,
    listener: MeshOtaListener
): Boolean

/**
 * 结束MCU OTA升级
 */
fun stopMcuOTA()

/**
 * 销毁并释放资源
 */
fun destroy()

```
