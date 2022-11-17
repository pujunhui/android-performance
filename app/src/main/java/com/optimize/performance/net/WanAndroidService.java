package com.optimize.performance.net;

import com.optimize.performance.bean.wanandroid.ArticleBean;
import com.optimize.performance.bean.wanandroid.BannerBean;
import com.optimize.performance.bean.wanandroid.PageModel;
import com.optimize.performance.bean.wanandroid.Result;

import io.reactivex.rxjava3.core.Single;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface WanAndroidService {

    @GET("banner/json")
    public Single<Result<BannerBean>> getBanner();

    @GET("article/list/{size}/json")
    public Single<Result<PageModel<ArticleBean>>> getMainArticle(@Path("size") int size, @Query("page_size ") int pageSize);
}
