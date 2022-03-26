package co.hoppen.camreademo;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ResolveInfo;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

import co.hoppen.App;

/**
 * Created by YangJianHui on 2021/6/4.
 */
public class WaxSystemUtlis {

    public static void fileManager(AppCompatActivity appCompatActivity){

        ResolveInfo rInfo = null;
        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
            mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> mApps = App.app.getPackageManager().queryIntentActivities(
                mainIntent, 0);
            for (ResolveInfo resolveInfo : mApps) {
            if (resolveInfo.activityInfo.packageName.equals("com.estrongs.android.pop")){
                rInfo = resolveInfo;
                break;
            }
            String nam = (resolveInfo.activityInfo
                    .loadLabel(App.app.getPackageManager()) + "").trim();
            if (nam.indexOf("文件") != -1) {
                rInfo = resolveInfo;
                break;
            }
        }
            if (rInfo != null) {
            mainIntent.setComponent(new ComponentName(
                    rInfo.activityInfo.packageName, rInfo.activityInfo.name));
                appCompatActivity.startActivity(mainIntent);
        }
    }

}
