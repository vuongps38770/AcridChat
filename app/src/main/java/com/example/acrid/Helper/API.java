package com.example.acrid.Helper;

import com.example.acrid.Model.NotificationBody;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface API {
    @POST("send-notification-single")
    Call<ResponseBody> sendNotification(@Body NotificationBody body);
}
