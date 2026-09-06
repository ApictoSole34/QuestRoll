package com.fizzycoyote.qusetroll.core.repository.open5e;

import androidx.annotation.Nullable;

/**
 * A generic class that holds a value with its loading status and progress information.
 * <p>
 * This is primarily used by the {@link Open5eRepository} to communicate the state of
 * data synchronization to the UI, including overall progress and specific section progress.
 * </p>
 *
 * @param <T> The type of the data being wrapped.
 */
public class Resource<T> {
    /**
     * Represents the status of the resource.
     */
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

    /**
     * Creates a LOADING Resource for a specific section.
     */
    public static <T> Resource<T> loading(@Nullable String sectionName) {
        return new Resource<>(Status.LOADING, null, null, -1, sectionName, -1);
    }

    /**
     * Creates a LOADING Resource with overall progress.
     */
    public static <T> Resource<T> loading(int progress, @Nullable String sectionName) {
        return new Resource<>(Status.LOADING, null, null, progress, sectionName, -1);
    }

    /**
     * Creates a LOADING Resource with data and overall progress.
     */
    public static <T> Resource<T> loading(@Nullable T data, int progress) {
        return new Resource<>(Status.LOADING, data, null, progress, null, -1);
    }

    /**
     * Creates a LOADING Resource with overall progress, section name, and section-specific progress.
     */
    public static <T> Resource<T> loading(int progress, @Nullable String sectionName, int sectionProgress) {
        return new Resource<>(Status.LOADING, null, null, progress, sectionName, sectionProgress);
    }

    /**
     * Creates a SUCCESS Resource.
     */
    public static <T> Resource<T> success(@Nullable T data) {
        return new Resource<>(Status.SUCCESS, data, null, 100, null, 100);
    }

    /**
     * Creates an ERROR Resource.
     */
    public static <T> Resource<T> error(@Nullable String message, @Nullable T data) {
        return new Resource<>(Status.ERROR, data, message, 0, null, 0);
    }
}
