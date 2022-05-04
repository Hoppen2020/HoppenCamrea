package co.hoppen.cameralib.tools.queue;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Process;

import com.blankj.utilcode.util.LogUtils;

/**
 * Created by tianjiangwei on 2017/9/28.
 */

public abstract class Task implements Runnable {

    private int id;
    private TaskCallBack mTaskCallBack;
    // Thread Helper Functions
    private static final Handler mainHandler = new Handler(Looper.getMainLooper());
    private Handler threadHandler;
    private HandlerThread handlerThread;
    private boolean isFinish=false;
    private int priority = Process.THREAD_PRIORITY_DEFAULT;

    public Task(){}

    public void packTask(int taskId){
        this.id = taskId;
        handlerThread = new HandlerThread("task "+id,priority);
        handlerThread.start();
        threadHandler = new Handler(handlerThread.getLooper());
    }

    public String getTaskThreadName(){
        return handlerThread==null?"":handlerThread.getName();
    }

    @Override
    public void run() {
        onTask();
    }

    public abstract void taskContent();

    public synchronized void onTask(){
        taskContent();
        finish();
    }

    public boolean isFinish(){
        return isFinish;
    }

    private void finish(){
        isFinish=true;
        try {
            handlerThread.quit();
            threadHandler.removeCallbacks(null);
        }catch (Exception e){
        }
        runOnMainThread(() -> {
            if (mTaskCallBack!=null){
                mTaskCallBack.onFinish(Task.this);
            }
        });
    }

    public void runOnMainThread(Runnable runnable){
        mainHandler.post(runnable);
    }

    public void runOnMainThreadOnDelay(Runnable runnable,long delay){
        mainHandler.postDelayed(runnable,delay);
    }

    public void runOnThread(Runnable runnable){
        threadHandler.post(runnable);
    }

    public void runOnThreadOnDelay(Runnable runnable,long delay){
        threadHandler.postDelayed(runnable,delay);
    }

    public void sleep(long sleepTime){
        try{
            Thread.sleep(sleepTime);
        }catch (Exception e){
        }
    }

    public void setTaskCallBack(TaskCallBack callBack){
        this.mTaskCallBack=callBack;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task that = (Task) o;
        return this.id == that.id;
    }

    @Override
    public int hashCode() {
        int result = hashCode();
        result = 31 * result + id;
        return result;
    }


}
