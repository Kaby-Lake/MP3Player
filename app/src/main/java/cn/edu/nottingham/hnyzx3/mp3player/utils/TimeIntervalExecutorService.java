package cn.edu.nottingham.hnyzx3.mp3player.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * the service to execute the task at the specified time interval
 */
public class TimeIntervalExecutorService {
    private static final ScheduledExecutorService timeProgressExecutor = Executors.newScheduledThreadPool(1);

    private static Map<Integer, TimeIntervalExecutorServiceCallback> callbackMap = new HashMap<>();

    /**
     * callback interface provided to cancel or restart the given task
     */
    public interface TimeIntervalExecutorServiceCallback {
        /**
         * cancel the task
         */
        void cancel();

        /**
         * restart the task
         */
        void restart();

    }

    /**
     * execute the given task at the specified time interval
     *
     * @param runnable the task to be executed
     * @param period   the time interval in milliseconds
     * @return the callback object to cancel or restart the task
     */
    public static TimeIntervalExecutorServiceCallback scheduleSingletonAtFixedTime(int id, Runnable runnable, long period) {
        if (callbackMap.containsKey(id)) {
            callbackMap.get(id).cancel();
        }
        final ScheduledFuture[] future = {timeProgressExecutor.scheduleAtFixedRate(runnable, 0, period, TimeUnit.MILLISECONDS)};
        TimeIntervalExecutorServiceCallback callback = new TimeIntervalExecutorServiceCallback() {
            @Override
            public void cancel() {
                future[0].cancel(true);
            }

            @Override
            public void restart() {
                future[0].cancel(true);
                future[0] = timeProgressExecutor.scheduleAtFixedRate(runnable, 0, period, TimeUnit.MILLISECONDS);
            }
        };

        callbackMap.put(id, callback);
        return callback;
    }
}
