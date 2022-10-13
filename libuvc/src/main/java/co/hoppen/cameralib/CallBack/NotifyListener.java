package co.hoppen.cameralib.CallBack;

import android.graphics.SurfaceTexture;

/**
 * Created by YangJianHui on 2022/10/10.
 */
public interface NotifyListener {
  void onUpdateSurface(SurfaceTexture surfaceTexture);
  void onPageStop();
  void onPageDestroy();
}
