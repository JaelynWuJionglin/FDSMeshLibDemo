# FDSMeshLibDemo
Godox SDK  demo

# 混淆规则
## spongycastle
-keep class org.spongycastle** {*;}  

## SDK不混淆
-keep class com.godox.sdk** {*;}  
-keep class com.base.mesh.api** {*;}  
-keep class com.telink.ble.mesh** {*;}  