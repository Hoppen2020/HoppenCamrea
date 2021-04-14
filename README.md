# HoppenCamera
#### 准备工作
```
allprojects {
    repositories {
	...
	maven { url 'https://jitpack.io' }
	}
}
```
```
dependencies {
    implementation 'com.github.Hoppen2020:HoppenCamrea:1.0.1'
}
```
#### 使用
***在Activity或者在继承LifecycleOwner中直接使用（已遵循android Lifecycle）***
```
HoppenController controller =
            HoppenCameraHelper.createController(Activity, TextureView,OnDeviceListener);
```
#### 设置手柄实体按钮回调
```
controller.setDeviceButton(new OnButtonListener() {
            @Override
            public void onButton(int state) {
               // 1:按下
               // 0:松开
            }
        });
```
#### 设置水分值回调
```
controller.setWaterListener(new OnWaterListener() {
            @Override
            public void onWaterCallback(float water) {
                // 水分值
            }
        });
```
#### 发送指令
```
controller.sendInstructions(Instruction);
```
***指令***

`water返回值说明:-1（没有此装置） 0（没有接触皮肤）`

|指令|说明|
|---|:---:|
|LIGHT_RGB|rgb灯光|
|LIGHT_UV|UV灯光|
|LIGHT_POLARIZED|偏振光|
|LIGHT_CLOSE|关闭灯光|
|WATER|水分值|
