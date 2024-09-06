package co.hoppen.camreademo;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;

import co.hoppen.cameralib.HoppenController;

/**
 * Created by YangJianHui on 2024/8/13.
 */
public class ZoomDialog extends Dialog implements DialogInterface.OnDismissListener {

    private final ZoomCameraListener zoomCameraListener;

    public ZoomDialog(@NonNull Context context,ZoomCameraListener zoomCameraListener) {
        super(context);
        this.zoomCameraListener = zoomCameraListener;
        zoomCameraListener.onStopPreview();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_zoom);
        setOnDismissListener(this);
    }

    public void addView(View view){
        FrameLayout parent = findViewById(R.id.parent);
        parent.addView(view);
        zoomCameraListener.onStartPreview();
    }

    @Override
    public void onDismiss(DialogInterface dialogInterface) {
        zoomCameraListener.onStopPreview();
        FrameLayout parent = findViewById(R.id.parent);
        View childAt = parent.getChildAt(0);
        parent.removeView(childAt);
        zoomCameraListener.onDialogDismiss(childAt);
    }

    public interface ZoomCameraListener{
        void onStopPreview();
        void onStartPreview();
        void onDialogDismiss(View view);
    }

}
