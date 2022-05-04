package co.hoppen.cameralib.tools.queue;

import java.io.Serializable;

/**
 * Created by tianjiangwei on 2017/9/28.
 */

public interface TaskQueueListener extends Serializable{
    void onRunning();
    void onCancel();
    void onProgress(float progress,Task task);
    void onFinish();

}
