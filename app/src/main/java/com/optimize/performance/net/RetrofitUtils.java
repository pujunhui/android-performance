package com.optimize.performance.net;

import com.optimize.performance.PerformanceApp;

import okhttp3.Cache;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitUtils {
    private static volatile RetrofitUtils sInstance;
    public static final String WAN_ANDROID_BASE_URL = "https://www.wanandroid.com/";
    public static final String API_OPEN_BASE_URL = "https://api.apiopen.top/api/";

    private final WanAndroidService mWanAndroidService;
    private final ApiOpenService mApiOpenService;

    private static RetrofitUtils getInstance() {
        if (sInstance == null) {
            synchronized (RetrofitUtils.class) {
                if (sInstance == null) {
                    sInstance = new RetrofitUtils();
                }
            }
        }
        return sInstance;
    }

    private RetrofitUtils() {
        Cache cache = new Cache(PerformanceApp.getApplication().getCacheDir(), 10 * 1024 * 1024);

        OkHttpClient client = new OkHttpClient.Builder()
                .eventListenerFactory(OkHttpEventListener.FACTORY).
                dns(OkHttpDNS.getIns(PerformanceApp.getApplication())).
                addInterceptor(new NoNetInterceptor())
                .addInterceptor(new HttpLoggingInterceptor()
                        .setLevel(HttpLoggingInterceptor.Level.BODY))
                .build();

        mWanAndroidService = createApiService(client, WAN_ANDROID_BASE_URL, WanAndroidService.class);
        mApiOpenService = createApiService(client, API_OPEN_BASE_URL, ApiOpenService.class);
    }

    private <T> T createApiService(OkHttpClient client, String baseUrl, Class<T> clazz) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
                .client(client)
                .build();
        return retrofit.create(clazz);
    }

    public static WanAndroidService getWanAndroidService() {
        return getInstance().mWanAndroidService;
    }

    public static ApiOpenService getAPIOpenService() {
        return getInstance().mApiOpenService;
    }
}
