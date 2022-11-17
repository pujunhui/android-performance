package com.optimize.performance;

import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Debug;
import android.os.Parcel;
import android.util.Log;
import android.widget.ImageView;

import androidx.multidex.MultiDex;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.stetho.Stetho;
import com.github.anrwatchdog.ANRError;
import com.github.anrwatchdog.ANRWatchDog;
import com.github.moduth.blockcanary.BlockCanary;
import com.optimize.performance.block.AppBlockCanaryContext;
import com.optimize.performance.memory.ImageHook;
import com.optimize.performance.utils.LaunchTimer;
import com.optimize.performance.utils.LogUtils;
import com.tencent.bugly.crashreport.CrashReport;
import com.tencent.mmkv.MMKV;
import com.umeng.commonsdk.UMConfigure;

import cn.jpush.android.api.JPushInterface;
import de.robv.android.xposed.DexposedBridge;
import de.robv.android.xposed.XC_MethodHook;

public class PerformanceApp extends Application {
    private AMapLocationClient mLocationClient = null;
    private AMapLocationClientOption mLocationOption = null;

    public void setDeviceId(String deviceId) {
        this.mDeviceId = deviceId;
    }

    public String getDeviceId() {
        return mDeviceId;
    }

    private String mDeviceId;
    private static Application mApplication;

    private AMapLocationListener mLocationListener = new AMapLocationListener() {
        @Override
        public void onLocationChanged(AMapLocation aMapLocation) {
            // 一些处理
        }
    };

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        mApplication = this;

        LaunchTimer.startRecord();
        MultiDex.install(this);
//        DexposedBridge.hookAllConstructors(Thread.class, new XC_MethodHook() {
//            @Override
//            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
//                super.afterHookedMethod(param);
//                Thread thread = (Thread) param.thisObject;
//                LogUtils.i(thread.getName()+" stack "+Log.getStackTraceString(new Throwable()));
//            }
//        });
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Debug.startMethodTracing("app");
        initAMap();
        initStetho();
        initBugly();
        initFresco();
        initJPush();
        initUmeng();
        Debug.stopMethodTracing();

//        TaskDispatcher.init(PerformanceApp.this);
//        TaskDispatcher dispatcher = TaskDispatcher.createInstance();
//        dispatcher.addTask(new InitAMapTask())
//                .addTask(new InitStethoTask())
//                .addTask(new InitBuglyTask())
//                .addTask(new InitFrescoTask())
//                .addTask(new InitJPushTask())
//                .addTask(new InitUmengTask())
//                .addTask(new GetDeviceIdTask())
//                .start();
//        dispatcher.await();

        LaunchTimer.endRecord("init end");

//        DexposedBridge.hookAllConstructors(ImageView.class, new XC_MethodHook() {
//            @Override
//            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
//                DexposedBridge.findAndHookMethod(ImageView.class, "setImageBitmap", Bitmap.class, new ImageHook());
//            }
//        });

//        try {
//            DexposedBridge.findAndHookMethod(Class.forName("android.os.BinderProxy"), "transact",
//                    int.class, Parcel.class, Parcel.class, int.class, new XC_MethodHook() {
//                        @Override
//                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
////                            LogUtils.i("BinderProxy beforeHookedMethod " + param.method.getName()
////                                    + "\n" + Log.getStackTraceString(new Throwable()));
//                            super.beforeHookedMethod(param);
//                        }
//                    });
//        } catch (ClassNotFoundException e) {
//            e.printStackTrace();
//        }

//        BlockCanary.install(this, new AppBlockCanaryContext()).start();

        initStrictMode();

        new ANRWatchDog().setANRListener(new ANRWatchDog.ANRListener() {
            @Override
            public void onAppNotResponding(ANRError error) {
                error.printStackTrace();
            }
        }).start();
    }

    private void initStrictMode() {
//        if (BuildConfig.DEBUG) {
//            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
//                    .detectCustomSlowCalls() //API等级11，使用StrictMode.noteSlowCode
//                    .detectDiskReads()
//                    .detectDiskWrites()
//                    .detectNetwork()// or .detectAll() for all detectable problems
//                    .penaltyLog() //在Logcat 中打印违规异常信息
//                    .build());
//            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
//                    .detectLeakedSqlLiteObjects()
//                    .setClassInstanceLimit(ImageBean.class, 1)
//                    .detectLeakedClosableObjects() //API等级11
//                    .penaltyLog()
//                    .build());
//        }
    }

    private void initMMKV() {
        MMKV.initialize(PerformanceApp.this);
    }

    private void initStetho() {
        Stetho.initializeWithDefaults(this);
    }

    private void initJPush() {
        JPushInterface.init(this);
        JPushInterface.setAlias(this, 0, mDeviceId);
    }

    private void initFresco() {
        Fresco.initialize(this);
    }

    private void initAMap() {
        try {
            AMapLocationClient.updatePrivacyShow(this, true, true);
            AMapLocationClient.updatePrivacyAgree(this, true);
            mLocationClient = new AMapLocationClient(this);
            mLocationClient.setLocationListener(mLocationListener);
            mLocationOption = new AMapLocationClientOption();
            mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
            mLocationOption.setOnceLocation(true);
            mLocationClient.setLocationOption(mLocationOption);
            mLocationClient.startLocation();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initUmeng() {
        UMConfigure.init(this, "58edcfeb310c93091c000be2", "umeng",
                UMConfigure.DEVICE_TYPE_PHONE, "1fe6a20054bcef865eeb0991ee84525b");
    }

    private void initBugly() {
        CrashReport.initCrashReport(getApplicationContext(), "fb7f2e66ed", false);
    }

    public static Application getApplication() {
        return mApplication;
    }

}
