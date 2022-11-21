package com.example.mobiletracker.retrofit;

import com.example.mobiletracker.model.Sample;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface SampleApi {

    // Interface to get request from the server and also to post to the server

    @GET("/sample/get-request")
    Call<List<Sample>> getAllSamples();

    // @Body to specify that this will be a post body
    @POST("/sample/post-request")
    Call<Sample> postSample(@Body Sample sample);
}
