package com.make.develop.domi.Remote;

import android.database.Observable;

import okhttp3.ResponseBody;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ICloudFunctions {
    @GET("getCustomToken")
    Observable<ResponseBody> getCustomToken(@Query("access_token") String accessToken);
}
