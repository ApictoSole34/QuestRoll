package com.fizzycoyote.qusetroll.core.repository.open5e;

import androidx.annotation.Nullable;

public class Resource<T> {
    public enum Status { LOADING, SUCCESS, ERROR }

    public final Status status;
    public final T data;
    public final String message;
    public final int progress;

    private Resource(Status status, @Nullable T data, @Nullable String message, int progress) {
        this.status = status;
        this.data = data;
        this.message = message;
        this.progress = progress;
    }

    public static <T> Resource<T> loading(@Nullable T data, int progress) {
        return new Resource<>(Status.LOADING, data, null, progress);
    }

    public static <T> Resource<T> success(@Nullable T data) {
        return new Resource<>(Status.SUCCESS, data, null, 100);
    }

    public static <T> Resource<T> error(String msg, @Nullable T data) {
        return new Resource<>(Status.ERROR, data, msg, 0);
    }
}