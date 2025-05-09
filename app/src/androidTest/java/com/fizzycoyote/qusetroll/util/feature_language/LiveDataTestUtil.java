package com.fizzycoyote.qusetroll.util.feature_language;

import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

public class LiveDataTestUtil {
    public static <T> T getOrAwaitValue(final LiveData<T> liveData, Predicate<T> condition) throws InterruptedException {
        final Object[] data = new Object[1];
        final CountDownLatch latch = new CountDownLatch(1);

        Observer<T> observer = new Observer<T>() {
            @Override
            public void onChanged(T t) {
                if (condition.test(t)) {
                    data[0] = t;
                    latch.countDown();
                    liveData.removeObserver(this);
                }
            }
        };

        new Handler(Looper.getMainLooper()).post(() -> liveData.observeForever(observer));
        latch.await(2, TimeUnit.SECONDS);
        return (T) data[0];
    }
}