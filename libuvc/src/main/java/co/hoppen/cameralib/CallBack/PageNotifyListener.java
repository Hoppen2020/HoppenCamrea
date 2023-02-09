package co.hoppen.cameralib.CallBack;

import android.graphics.SurfaceTexture;

/**
 * Created by YangJianHui on 2022/10/10.
 */
public interface PageNotifyListener {
  void onUpdateSurface(SurfaceTexture surfaceTexture);
  void onSurfaceDestroyed();
  void onPageResume();
  void onPageStop();
  void onPageDestroy();
}
