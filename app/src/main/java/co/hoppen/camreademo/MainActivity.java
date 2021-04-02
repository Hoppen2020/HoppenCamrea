package co.hoppen.camreademo;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import co.hoppen.cameralib.HoppenCameraHelper;
import co.hoppen.cameralib.HoppenController;
import co.hoppen.cameralib.Instruction;
import co.hoppen.cameralib.OnButtonListener;
import co.hoppen.cameralib.OnDeviceListener;
import co.hoppen.cameralib.OnWaterListener;
import co.hoppen.cameralib.widget.UVCCameraTextureView;

public class MainActivity extends AppCompatActivity implements OnDeviceListener {

    HoppenController controller;

    TextView tv_water,tv_status;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        UVCCameraTextureView ttv_display = findViewById(R.id.ttv_display);
        tv_water = findViewById(R.id.tv_water);
        tv_status = findViewById(R.id.tv_status);
        controller = HoppenCameraHelper.createController(this, ttv_display,this);
        controller.setDeviceButton(new OnButtonListener() {
            @Override
            public void onButton(int state) {
                if (state==1){
                    controller.sendInstructions(Instruction.WATER);
                }
            }
        });
        controller.setWaterListener(new OnWaterListener() {
            @Override
            public void onWaterCallback(float water) {
                Log.e("xxxxx",""+ (Looper.getMainLooper().getThread() == Thread.currentThread()));
                tv_water.setText(""+water);
            }
        });

    }
    @Override
    public void onConnected() {
        Log.e("onConnected",""+ (Looper.getMainLooper().getThread() == Thread.currentThread()));
        tv_status.setText("已连接");
    }

    @Override
    public void onDisconnect() {
        Log.e("onDisconnect",""+ (Looper.getMainLooper().getThread() == Thread.currentThread()));
        tv_status.setText("已断开");
    }

    public void onClick(View view){
        switch (view.getId()){
            case R.id.btn_polarized:
                controller.sendInstructions(Instruction.LIGHT_POLARIZED);
                break;
            case R.id.btn_uv:
                controller.sendInstructions(Instruction.LIGHT_UV);
                break;
            case R.id.btn_rgb:
                controller.sendInstructions(Instruction.LIGHT_RGB);
                break;
        }
    }
}