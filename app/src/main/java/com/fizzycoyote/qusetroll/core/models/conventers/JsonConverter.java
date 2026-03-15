package com.fizzycoyote.qusetroll.core.models.conventers;

import androidx.room.TypeConverter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class JsonConverter {
    @TypeConverter
    public static String fromJsonObject(JSONObject jsonObject) {
        if (jsonObject == null) {
            return null;
        }
        return jsonObject.toString();
    }

    @TypeConverter
    public static JSONObject toJsonObject(String jsonString) {
        if (jsonString == null) {
            return new JSONObject();
        }
        try {
            return new JSONObject(jsonString);
        } catch (JSONException e) {
            return new JSONObject();
        }
    }

    @TypeConverter
    public static String fromJsonArray(JSONArray jsonArray) {
        if (jsonArray == null) {
            return null;
        }
        return jsonArray.toString();
    }

    @TypeConverter
    public static JSONArray toJsonArray(String jsonString) {
        if (jsonString == null) {
            return new JSONArray();
        }
        try {
            return new JSONArray(jsonString);
        } catch (JSONException e) {
            return new JSONArray();
        }
    }
}