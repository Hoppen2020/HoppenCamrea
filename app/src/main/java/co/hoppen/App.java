package co.hoppen;

import android.app.Application;

/**
 * Created by YangJianHui on 2022/2/26.
 */
public class App extends Application {
   public static App app;

   @Override
   public void onCreate() {
      super.onCreate();
      app = this;
   }


}
