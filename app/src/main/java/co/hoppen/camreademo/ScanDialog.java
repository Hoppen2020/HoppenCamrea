package co.hoppen.camreademo;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.view.animation.LinearInterpolator;

import androidx.annotation.NonNull;

import co.hoppen.camreademo.databinding.DialogScanBinding;

import static android.view.animation.Animation.INFINITE;

/**
 * Created by YangJianHui on 2021/5/25.
 */
public class ScanDialog extends BaseDataBindingDialog<DialogScanBinding> implements DialogInterface.OnDismissListener {
    public ValueAnimator valueAnimator;
    public ScanDialog(@NonNull Context context) {
        super(context,R.style.CommonDialogLight);
        setOnDismissListener(this);
    }

    @Override
    protected boolean canceledOnTouchOutside() {
        return false;
    }

    @Override
    protected int layoutId() {
        return R.layout.dialog_scan;
    }

    @Override
    protected void onBindView(DialogScanBinding viewDataBinding) {
        int height = 177;
        valueAnimator = ValueAnimator.ofInt(0,height,0);
        valueAnimator.setRepeatCount(INFINITE);
        valueAnimator.setDuration(3500);
        valueAnimator.setInterpolator(new LinearInterpolator());
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int value = (int) animation.getAnimatedValue();
                viewDataBinding.ivScan.setVisibility(View.VISIBLE);
                viewDataBinding.ivScan.setTranslationY(value);
            }
        });

    }

    @Override
    public void show() {
        super.show();
        valueAnimator.start();
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        if (valueAnimator!=null&&valueAnimator.isRunning()){
            valueAnimator.cancel();
        }
    }
}
