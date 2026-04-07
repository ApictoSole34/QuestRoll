package com.fizzycoyote.qusetroll.core.repository.open5e;

import androidx.annotation.Nullable;

public class Resource<T> {
    public enum Status { LOADING, SUCCESS, ERROR }

    public final Status status;
    @Nullable public final T data;
    @Nullable public final String message;
    public final int progress;
    @Nullable public final String sectionName;
    public final int sectionProgress;

    private Resource(Status status, @Nullable T data, @Nullable String message,
                     int progress, @Nullable String sectionName, int sectionProgress) {
        this.status = status;
        this.data = data;
        this.message = message;
        this.progress = progress;
        this.sectionName = sectionName;
        this.sectionProgress = sectionProgress;
    }

    public static <T> Resource<T> loading(@Nullable String sectionName) {
        return new Resource<>(Status.LOADING, null, null, -1, sectionName, -1);
    }

    public static <T> Resource<T> loading(int progress, @Nullable String sectionName) {
        return new Resource<>(Status.LOADING, null, null, progress, sectionName, -1);
    }

    public static <T> Resource<T> loading(@Nullable T data, int progress) {
        return new Resource<>(Status.LOADING, data, null, progress, null, -1);
    }

    public static <T> Resource<T> loading(int progress, @Nullable String sectionName, int sectionProgress) {
        return new Resource<>(Status.LOADING, null, null, progress, sectionName, sectionProgress);
    }

    public static <T> Resource<T> success(@Nullable T data) {
        return new Resource<>(Status.SUCCESS, data, null, 100, null, 100);
    }

    public static <T> Resource<T> error(@Nullable String message, @Nullable T data) {
        return new Resource<>(Status.ERROR, data, message, 0, null, 0);
    }
}