package com.optimize.performance.net;

import com.optimize.performance.bean.apiopen.HaoKanBean;
import com.optimize.performance.bean.apiopen.ImageBean;
import com.optimize.performance.bean.apiopen.KuaiShouBean;
import com.optimize.performance.bean.apiopen.PageModel;
import com.optimize.performance.bean.apiopen.Result;
import com.optimize.performance.bean.apiopen.SixRoomsBean;

import io.reactivex.rxjava3.core.Single;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ApiOpenService {

    @GET("getImages")
    Single<Result<PageModel<ImageBean>>> getImages(@Query("type") String type, @Query("page") int page, @Query("size") int size);

    @GET("getHaoKanVideo")
    Single<Result<PageModel<HaoKanBean>>> getHaoKanVideo(@Query("page") int page, @Query("size") int size);

    @GET("getMiniVideo")
    Single<Result<PageModel<SixRoomsBean>>> getSixRoomsVideo(@Query("page") int page, @Query("size") int size);

    @GET("getShortVideo")
    Single<Result<PageModel<KuaiShouBean>>> getKuaiShouVideo(@Query("page") int page, @Query("size") int size);
}