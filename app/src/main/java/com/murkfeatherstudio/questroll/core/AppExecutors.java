package com.murkfeatherstudio.questroll.core;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import androidx.annotation.NonNull;

/**
 * Global executor pools for the whole application.
 *
 * Grouping tasks like this avoids the overhead of creating new threads constantly
 * and helps in managing thread usage.
 */
public class AppExecutors {

    // For Singleton instantiation
    private static final Object LOCK = new Object();
    private static AppExecutors sInstance;
    private final ExecutorService diskIO;
    private final Executor mainThread;
    private final Executor networkIO;

    private AppExecutors(ExecutorService diskIO, Executor networkIO, Executor mainThread) {
        this.diskIO = diskIO;
        this.networkIO = networkIO;
        this.mainThread = mainThread;
    }

    public static AppExecutors getInstance() {
        if (sInstance == null) {
            synchronized (LOCK) {
                sInstance = new AppExecutors(Executors.newSingleThreadExecutor(),
                        Executors.newFixedThreadPool(3),
                        new MainThreadExecutor());
            }
        }
        return sInstance;
    }

    public ExecutorService diskIO() {
        return diskIO;
    }

    public Executor mainThread() {
        return mainThread;
    }

    public Executor networkIO() {
        return networkIO;
    }

    private static class MainThreadExecutor implements Executor {
        private Handler mainThreadHandler = new Handler(Looper.getMainLooper());

        @Override
        public void execute(@NonNull Runnable command) {
            mainThreadHandler.post(command);
        }
    }
}
