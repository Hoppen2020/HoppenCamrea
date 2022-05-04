package co.hoppen.cameralib.tools.queue;

import com.blankj.utilcode.util.LogUtils;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;

public class TaskQueue implements Serializable{

    private int taskId =0;
    private String queueName ="";
    private boolean isRunning = false;
    private TaskQueueListener mTaskQueueListener;
    private LinkedBlockingQueue<Task> arrayBlockingQueue = new LinkedBlockingQueue<Task>();
    private ArrayList<Task> copyTaskList = new ArrayList<>();
    private Task currentTask;
    private int currentIndex = 0;
    private int allTaskNumber = 0;

    public TaskQueue(){
        this("queue_"+System.currentTimeMillis());
    }

    public TaskQueue(String queueName){
        this.queueName =queueName;
    }

    public boolean isRunning(){
        return isRunning;
    }

    private synchronized void start(){
        if (isRunning)return;
        if (!arrayBlockingQueue.isEmpty()){
            try{
                currentTask = arrayBlockingQueue.take();
                new Thread(currentTask).start();
                isRunning = true;
                if (mTaskQueueListener!=null){
                    mTaskQueueListener.onRunning();
                }
            }catch (Exception e){
                isRunning = false;
            }
        }
    }

//    public void reStart(){
//        isRunning = false;
//        currentIndex = 0;
//        arrayBlockingQueue = getCopyTaskQueue();
//        start();
//    }
//
//    private LinkedBlockingQueue<Task> getCopyTaskQueue(){
//        LinkedBlockingQueue<Task> queue = new LinkedBlockingQueue<>();
//        if (copyTaskList!=null){
//            for (int i = 0; i <copyTaskList.size() ; i++) {
//                try{
//                    queue.put(copyTaskList.get(i));
//                }catch (Exception e) {
//                }
//            }
//        }
//        return queue;
//    }
//    public void resume(){
//        if (!isRunning){
//            isRunning=true;
//            if (mTaskQueueListener!=null){
//                mTaskQueueListener.onResume();
//            }currentTask=arrayBlockingQueue.poll();
//            if (currentTask!=null){
//                currentIndex++;
//                new Thread(currentTask).start();
//            }
//        }
//    }

    public void cancel(){
        if (isRunning){
            isRunning=false;
            arrayBlockingQueue.clear();
            if (mTaskQueueListener!=null){
                mTaskQueueListener.onCancel();
            }
        }
    }

    public void addTask(Task task,currentTaskFinish currentTaskFinish){
        try{
            allTaskNumber++;
            task.packTask(taskId);
            taskId++;
            arrayBlockingQueue.put(task);
            copyTaskList.add(task);
            task.setTaskCallBack(new TaskCallBack() {
                @Override
                public void onFinish(Task task) {
                    try{
                        if (currentTaskFinish!=null)currentTaskFinish.onFinish();
                        if (isRunning){
                            currentTask = arrayBlockingQueue.poll();
                            currentIndex++;
                            if (currentTask!=null){
                                new Thread(currentTask).start();
                            }else{
                                isRunning = false;
                                if (mTaskQueueListener!=null){
                                    mTaskQueueListener.onFinish();
                                }
                            }
                            if (mTaskQueueListener!=null){
                                mTaskQueueListener.onProgress(getProgress(),task);
                            }
                        }
                    }catch (Exception e){
                        LogUtils.e(e.toString());
                    }
                }
            });
            if (!isRunning)start();
        }catch (Exception e){
            LogUtils.e(e.toString());
        }
    }

    public void setTaskQueueListener(TaskQueueListener listener){
        this.mTaskQueueListener=listener;
    }

    public float getProgress(){
        DecimalFormat format=new DecimalFormat("#.00");
        return Float.parseFloat(format.format(((double) currentIndex/allTaskNumber)));
    }

    public String getQueueName() {
        return queueName;
    }

    public interface currentTaskFinish{
        void onFinish();
    }

}
