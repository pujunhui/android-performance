package com.optimize.performance;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.app.usage.NetworkStats;
import android.app.usage.NetworkStatsManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.NetworkCapabilities;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Process;
import android.os.RemoteException;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Choreographer;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.asynclayoutinflater.view.AsyncLayoutInflater;
import androidx.core.view.LayoutInflaterCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.optimize.performance.adapter.ImagesAdapter;
import com.optimize.performance.adapter.OnFeedShowCallBack;
import com.optimize.performance.async.ThreadPoolUtils;
import com.optimize.performance.bean.apiopen.ImageBean;
import com.optimize.performance.bean.apiopen.PageModel;
import com.optimize.performance.bean.apiopen.Result;
import com.optimize.performance.launchstarter.DelayInitDispatcher;
import com.optimize.performance.memory.MemoryLeakActivity;
import com.optimize.performance.memory.MemoryShakeActivity;
import com.optimize.performance.net.JobSchedulerService;
import com.optimize.performance.net.RetrofitUtils;
import com.optimize.performance.tasks.delayinittask.DelayInitTaskA;
import com.optimize.performance.tasks.delayinittask.DelayInitTaskB;
import com.optimize.performance.ui.OverDrawActivity;
import com.optimize.performance.utils.ExceptionMonitor;
import com.optimize.performance.utils.LaunchTimer;
import com.optimize.performance.utils.LogUtils;

import java.util.Calendar;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity implements OnFeedShowCallBack {
    private AlphaAnimation alphaAnimation;
    private RecyclerView mRecyclerView;
    private ImagesAdapter mImagesAdapter = new ImagesAdapter();

    private long mStartFrameTime = 0;
    private int mFrameCount = 0;
    private static final long MONITOR_INTERVAL = 160L; //单次计算FPS使用160毫秒
    private static final long MONITOR_INTERVAL_NANOS = MONITOR_INTERVAL * 1000L * 1000L;
    private static final long MAX_INTERVAL = 1000L; //设置计算fps的单位时间间隔1000ms,即fps/s;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 以下代码是为了演示修改任务的名称
        ThreadPoolUtils.getService().execute(new Runnable() {
            @Override
            public void run() {
                Process.setThreadPriority(Process.THREAD_PRIORITY_DEFAULT);
                String oldName = Thread.currentThread().getName();
                Thread.currentThread().setName("new Name");
                LogUtils.i("");
                Thread.currentThread().setName(oldName);
            }
        });

        // 以下代码是为了演示Msg导致的主线程卡顿
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                LogUtils.i("Msg 执行");
//                try {
//                    Thread.sleep(1000);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
            }
        });

        LayoutInflaterCompat.setFactory2(getLayoutInflater(), new LayoutInflater.Factory2() {
            @Override
            public View onCreateView(View parent, String name, Context context, AttributeSet attrs) {
                if (TextUtils.equals(name, "TextView")) {
                    // 生成自定义TextView
                }
                long time = System.currentTimeMillis();
                View view = getDelegate().createView(parent, name, context, attrs);
//                LogUtils.i(name + " cost " + (System.currentTimeMillis() - time));
                return view;
            }

            @Override
            public View onCreateView(String name, Context context, AttributeSet attrs) {
                return null;
            }
        });

        new AsyncLayoutInflater(MainActivity.this).inflate(R.layout.activity_main, null, new AsyncLayoutInflater.OnInflateFinishedListener() {
            @Override
            public void onInflateFinished(@NonNull View view, int i, @Nullable ViewGroup viewGroup) {
                setContentView(view);
                initViews();
            }
        });

        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//        initViews();

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_BATTERY_CHANGED);
        Intent intent = registerReceiver(null, filter);
        LogUtils.i("battery " + intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1));

        getImages();
        getFPS();


        // 以下代码是为了演示业务不正常场景下的监控
        try {
            // 一些业务处理
            Log.i("", "");
        } catch (Exception e) {
            ExceptionMonitor.monitor(Log.getStackTraceString(e));
        }

        boolean flag = true;
        if (flag) {
            // 正常，继续执行流程
        } else {
            ExceptionMonitor.monitor("");
        }
    }

    private void getNetStats(){
        if (Build.VERSION.SDK_INT<Build.VERSION_CODES.M){
            return;
        }
        long newtDataRx = 0;//接收
        long newDataTx = 0;//发送
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        String subId =telephonyManager.getSubscriberId();
        NetworkStatsManager manager = (NetworkStatsManager) getSystemService(Context.NETWORK_STATS_SERVICE);
        NetworkStats networkStats = null;
        NetworkStats.Bucket bucket = new NetworkStats.Bucket();
        try {
            networkStats = manager.querySummary(NetworkCapabilities.TRANSPORT_WIFI,subId,getTimesMonthMorning(),System.currentTimeMillis());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        while (networkStats.hasNextBucket()){
            networkStats.getNextBucket(bucket);
            int uid = bucket.getUid();
            if (getUidByPackageName() == uid){
                newtDataRx += bucket.getRxBytes();
                newDataTx += bucket.getTxBytes();
            }
        }
        Log.i("pmx","appnetuse "+ (newDataTx+newtDataRx));
    }

    private int getTimesMonthMorning(){
        Calendar cal = Calendar.getInstance();
        cal.set(cal.get(Calendar.YEAR),cal.get(Calendar.MONTH),cal.get(Calendar.DAY_OF_MONTH));
        cal.set(Calendar.DAY_OF_MONTH,cal.getActualMinimum(Calendar.DAY_OF_MONTH));
        return (int) cal.getTimeInMillis();
    }

    private int getUidByPackageName(){
        int uid = -1;
        PackageManager packageManager = getPackageManager();
        PackageInfo packageInfo = null;
        try {
            packageInfo = packageManager.getPackageInfo(getPackageName(),0);
            uid = packageInfo.applicationInfo.uid;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return uid;
    }

    private void initViews() {
        mRecyclerView = findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
        mRecyclerView.setAdapter(mImagesAdapter);
        mImagesAdapter.setOnFeedShowCallBack(MainActivity.this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.memory_leak) {
            startActivity(new Intent(this, MemoryLeakActivity.class));
            return true;
        } else if (item.getItemId() == R.id.memory_shake) {
            startActivity(new Intent(this, MemoryShakeActivity.class));
            return true;
        } else if (item.getItemId() == R.id.over_draw) {
            startActivity(new Intent(this, OverDrawActivity.class));
            return true;
        }
        return false;
    }

    /**
     * 演示JobScheduler的使用
     */
    private void startJobScheduler() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            JobScheduler jobScheduler = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
            JobInfo.Builder builder = new JobInfo.Builder(1, new ComponentName(getPackageName(), JobSchedulerService.class.getName()));
            builder.setRequiresCharging(true)
                    .setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED);
            jobScheduler.schedule(builder.build());
        }
    }

    private void getFPS() {
        Choreographer.getInstance().postFrameCallback(new Choreographer.FrameCallback() {
            @Override
            public void doFrame(long frameTimeNanos) {
                if (mStartFrameTime == 0) {
                    mStartFrameTime = frameTimeNanos;
                }
                long interval = frameTimeNanos - mStartFrameTime;
                if (interval > MONITOR_INTERVAL_NANOS) {
                    double fps = (((double) (mFrameCount * 1000L * 1000L)) / interval) * MAX_INTERVAL;
//                    LogUtils.i("fps--" + fps);
                    mFrameCount = 0;
                    mStartFrameTime = 0;
                } else {
                    ++mFrameCount;
                }
                Choreographer.getInstance().postFrameCallback(this);
            }
        });
    }

    private void getImages() {
        //animal, beauty, car, comic, food, game, movie, person, phone, scenery
        RetrofitUtils.getAPIOpenService().getImages("animal", 0, 20)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Consumer<Result<PageModel<ImageBean>>>() {
                    @Override
                    public void accept(Result<PageModel<ImageBean>> result) throws Throwable {
                        List<ImageBean> images = null;
                        PageModel<ImageBean> pageModel = result.getResult();
                        if (pageModel != null) {
                            images = pageModel.getList();
                        }
                        mImagesAdapter.setItems(images);
                    }
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 以下代码是为了演示电量优化中对动画的处理
//      alphaAnimation.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // 以下代码是为了演示电量优化中对动画的处理
//        alphaAnimation.cancel();
    }

    @Override
    public void onFeedShow() {
        // 以下两行是原有方式
//        new DispatchRunnable(new DelayInitTaskA()).run();
//        new DispatchRunnable(new DelayInitTaskB()).run();

        DelayInitDispatcher delayInitDispatcher = new DelayInitDispatcher();
        delayInitDispatcher.addTask(new DelayInitTaskA())
                .addTask(new DelayInitTaskB())
                .start();

        // 一系列操作 10s
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        LaunchTimer.endRecord("onWindowFocusChanged");
    }
}
