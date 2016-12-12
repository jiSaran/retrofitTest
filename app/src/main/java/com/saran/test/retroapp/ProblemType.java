package com.saran.test.retroapp;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by core I5 on 12/5/2016.
 */
public class ProblemType {
    @SerializedName("id")
    @Expose
    public String id;
    @SerializedName("title")
    @Expose
    public String title;
    @SerializedName("is_deleted")
    @Expose
    public String isDeleted;
}
