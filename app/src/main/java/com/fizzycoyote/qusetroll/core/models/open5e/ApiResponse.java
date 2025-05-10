package com.fizzycoyote.qusetroll.core.models.open5e;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ApiResponse<T> {
    @SerializedName("count") private int count;
    @SerializedName("next") private String next;
    @SerializedName("previous") private String previous;
    @SerializedName("results") private List<T> results;

    public int getCount() { return count; }
    public String getNext() { return next; }
    public String getPrevious() { return previous; }
    public List<T> getResults() { return results; }
}
