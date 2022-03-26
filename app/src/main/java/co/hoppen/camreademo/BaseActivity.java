package co.hoppen.camreademo;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Created by YangJianHui on 2021/3/10.
 */
public abstract class BaseActivity extends AppCompatActivity {

    //全浸式的activity
    private int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
            View.SYSTEM_UI_FLAG_FULLSCREEN |
            View.SYSTEM_UI_FLAG_IMMERSIVE;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        goneForNavBar();
        super.onCreate(savedInstanceState);
    }

    private void goneForNavBar(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(uiOptions);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                getWindow().setStatusBarColor(Color.TRANSPARENT);
            }
            decorView.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
                @Override
                public void onSystemUiVisibilityChange(int visibility) {
                    int changeOptions = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT ?
                            uiOptions | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY :
                            uiOptions | View.SYSTEM_UI_FLAG_LOW_PROFILE;
                    getWindow().getDecorView().setSystemUiVisibility(changeOptions);
                }
            });
        }
    }

}
