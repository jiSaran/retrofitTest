package com.saran.test.retroapp;

import java.util.List;
import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.PartMap;

/**
 * Created by core I5 on 12/5/2016.
 */

public interface ApiService {

    @FormUrlEncoded
    @POST("get_problem_types")
    Call<ProblemObject> getProblemDetails(@Field("test") String test);

    @Multipart
    @POST("feedback")
    Call<ResponseBody> submitForm(@PartMap()Map<String, RequestBody> partMap, @Part MultipartBody.Part imgfile);
}
