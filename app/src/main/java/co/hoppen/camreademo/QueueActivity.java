package co.hoppen.camreademo;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.blankj.utilcode.util.LogUtils;

import co.hoppen.cameralib.tools.queue.ConnectMcuDeviceTask;
import co.hoppen.cameralib.tools.queue.Task;
import co.hoppen.cameralib.tools.queue.TaskQueue;
import co.hoppen.cameralib.tools.queue.TaskQueueListener;

/**
 * Created by YangJianHui on 2022/5/4.
 */
public class QueueActivity extends AppCompatActivity {
   private TaskQueue taskQueue;

   @Override
   protected void onCreate(@Nullable Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_queue);
      taskQueue = new TaskQueue();
      taskQueue.setTaskQueueListener(new TaskQueueListener() {
         @Override
         public void onRunning() {
            LogUtils.e("onRunning");
         }

         @Override
         public void onCancel() {
            LogUtils.e("onCancel");
         }

         @Override
         public void onProgress(float progress, Task task) {
            LogUtils.e("progress "+ progress);
         }

         @Override
         public void onFinish() {
            LogUtils.e("onFinish");
         }
      });
   }

   public void addTask(View view){


   }

   public void back(View view){
      startActivity(new Intent(this,MainActivity.class));
      finish();
   }

}
