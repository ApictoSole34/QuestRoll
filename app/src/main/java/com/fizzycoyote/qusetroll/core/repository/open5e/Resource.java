package com.fizzycoyote.qusetroll.core.repository.open5e;

public class Resource<T> {
    public final Status status;
    public final T data;
    public final String message;
    public final int progress;
    public final String sectionName; // ← NOWE

    public enum Status { SUCCESS, ERROR, LOADING }

    private Resource(Status status, T data, String message, int progress, String sectionName) {
        this.status = status;
        this.data = data;
        this.message = message;
        this.progress = progress;
        this.sectionName = sectionName;
    }

    public static <T> Resource<T> success(T data) {
        return new Resource<>(Status.SUCCESS, data, null, 100, null);
    }

    public static <T> Resource<T> error(String message, T data) {
        return new Resource<>(Status.ERROR, data, message, 0, null);
    }

    public static <T> Resource<T> loading(T data, int progress) {
        return new Resource<>(Status.LOADING, data, null, progress, null);
    }

    public static <T> Resource<T> loading(T data, int progress, String sectionName) {
        return new Resource<>(Status.LOADING, data, null, progress, sectionName);
    }
}