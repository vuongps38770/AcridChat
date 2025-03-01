package com.example.acrid.Helper;

import com.example.acrid.Model.ImageSingle;
import com.example.acrid.Model.NotificationBody;

import java.io.File;
import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface API {
    @POST("send-notification-single")
    Call<ResponseBody> sendNotification(@Body NotificationBody body);
    @Multipart
    @POST("image/upload")
    Call<Map<String,Object>> sendImage(@Part MultipartBody.Part image);

}
