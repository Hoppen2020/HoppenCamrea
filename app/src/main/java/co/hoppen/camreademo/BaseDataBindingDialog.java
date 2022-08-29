package co.hoppen.camreademo;

import android.app.Dialog;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;


/**
 * 为了适配自定义的dialog 配合autozie使用
 * Created by YangJianHui on 2019/12/27.
 */
public abstract class BaseDataBindingDialog<DB extends ViewDataBinding> extends Dialog {

    private DB viewDataBinding;

    public View dialogView;

    public BaseDataBindingDialog(@NonNull Context context) {
        this(context,0);
    }

    public BaseDataBindingDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId!=0?themeResId: R.style.CommonDialog);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dialogView = bulidDialogView();
        setOnTouchOutsideClick(dialogView);
        setContentView(dialogView);
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        if (viewDataBinding!=null)onBindView(viewDataBinding);
    }
    /**
     * 点击窗口以外消失
     * @return
     */
    protected abstract boolean canceledOnTouchOutside();

    protected abstract int layoutId();

    protected abstract void onBindView(DB viewDataBinding);


    private void setOnTouchOutsideClick(final View view){
        if (view!=null){
            if (canceledOnTouchOutside()){
                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dismiss();
                    }
                });
            }else{
                setCancelable(false);
            }
        }
    }

    private View bulidDialogView(){
        int layoutId = layoutId();
        viewDataBinding = DataBindingUtil.inflate(LayoutInflater.from(getContext()), layoutId, null, false);
        LinearLayout parentLayout = new LinearLayout(getContext());
        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT);
        parentLayout.setLayoutParams(params);
        parentLayout.setGravity(Gravity.CENTER);
        parentLayout.setSoundEffectsEnabled(false);//取消点击声效
        if (layoutId!=0){
            View childView = viewDataBinding.getRoot();
            parentLayout.addView(childView);
            childView.setClickable(true);
            childView.setSoundEffectsEnabled(false);
        }
        return parentLayout;
    }

    //沉浸式的显示
    public void fullScreenImmersive(View view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_FULLSCREEN;
            view.setSystemUiVisibility(uiOptions);
        }
    }

    @Override
    public void show() {
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
        super.show();
        fullScreenImmersive(getWindow().getDecorView());
        this.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
    }

    public DB getDataBinding() {
        return viewDataBinding;
    }


}
