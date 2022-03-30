package it.nethesis.utils;

import android.util.Log;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class TimerSingleton {

    private static final String JOB_NAME = "Timer refresh";
    private static final long JOB_START_DELAY = 0L;
    private static final long JOB_FREQUENCY = 3 * 1000L;

    private volatile static TimerSingleton timerSingleton;
    private volatile static boolean isRunning;
    private volatile static TimerTaskJob task;
    private volatile static TimerTaskJob timerTask;
    private volatile static Timer timer;
    private static RefreshAction refreshAction;

    private TimerSingleton() {}

    public static TimerSingleton initialize(RefreshAction action) {
        refreshAction = action;

        if (timerSingleton == null) {

            synchronized (TimerSingleton.class) {
                if (timerSingleton == null) {
                    timerSingleton = new TimerSingleton();
                    task = new TimerTaskJob();
                    isRunning = false;
                }
            }
        }

        return timerSingleton;
    }

    public static void start() {
        if (!isRunning) {

            isRunning = true;
            task.start();
            Log.e("Starting", JOB_NAME + " " + new Date());
        }
    }

    public static void stop() {
        if (isRunning) {

            isRunning = false;
            task.stop();
            Log.e("Stopping", JOB_NAME + " " + new Date());
        }
    }

    public static boolean isRunning() {
        Log.e("Running", isRunning + JOB_NAME + " " + new Date());
        return isRunning;
    }

    private static class TimerTaskJob extends TimerTask {

        @Override
        public void run() {
            Log.e("Running", JOB_NAME + "... " + new Date());
            doTask();
        }

        void start() {
            timerTask = new TimerTaskJob();
            timer = new Timer(true);
            timer.scheduleAtFixedRate(timerTask, JOB_START_DELAY, JOB_FREQUENCY);
        }

        void stop() {
            timerTask.cancel();
            timer.cancel();
        }

        private void doTask() {
            Log.e("Working", JOB_NAME + "... " + new Date());
            try {
                refreshAction.refreshContactList();
                Thread.sleep(0);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
