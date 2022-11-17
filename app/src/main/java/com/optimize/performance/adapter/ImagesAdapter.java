package com.optimize.performance.adapter;

import android.net.Uri;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.optimize.performance.R;
import com.optimize.performance.bean.apiopen.ImageBean;
import com.optimize.performance.net.ConfigManager;
import com.optimize.performance.utils.LaunchTimer;
import com.optimize.performance.wakelock.WakeLockUtils;

import java.util.List;

public class ImagesAdapter extends RecyclerView.Adapter<ImagesAdapter.ViewHolder> {
    private List<ImageBean> mItems;
    private boolean mHasRecorded;
    private OnFeedShowCallBack mCallBack;

    public void setItems(List<ImageBean> items) {
        this.mItems = items;
        notifyDataSetChanged();
    }

    public void setOnFeedShowCallBack(OnFeedShowCallBack callBack) {
        this.mCallBack = callBack;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_images_constrainlayout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        if (position == 0 && !mHasRecorded) {
            mHasRecorded = true;
            holder.itemView.getViewTreeObserver()
                    .addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                        @Override
                        public boolean onPreDraw() {
                            holder.itemView.getViewTreeObserver().removeOnPreDrawListener(this);
                            LaunchTimer.endRecord("FeedShow");
                            if (mCallBack != null) {
                                mCallBack.onFeedShow();
                            }
                            return true;
                        }
                    });
        }

        ImageBean bean = mItems.get(position);
        holder.bind(bean);
    }

    @Override
    public int getItemCount() {
        return mItems == null ? 0 : mItems.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final SimpleDraweeView imageIv;
        private final TextView titleTv;
        private final TextView typeTv;

        public ViewHolder(View view) {
            super(view);
            imageIv = view.findViewById(R.id.image_iv);
            titleTv = view.findViewById(R.id.title_tv);
            typeTv = view.findViewById(R.id.type_tv);
            itemView.setOnClickListener(this);
        }

        public void bind(ImageBean bean) {
            Uri uri = Uri.parse(bean.getUrl());
            imageIv.setImageURI(uri);
            titleTv.setText(bean.getTitle());
            typeTv.setText(bean.getType());
        }

        @Override
        public void onClick(View view) {
            // ConfigManager.sOpenClick模拟的是功能的开关
            if (ConfigManager.sOpenClick) {
                // 此处模拟的是WakeLock使用的兜底策略
                WakeLockUtils.acquire(view.getContext());
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        WakeLockUtils.release();
                    }
                }, 200);
            }
//            //以下代码是为了演示Luban这个库对图片压缩对流量方面的影响
//            Luban.with(view.getContext())
//                    .load(Environment.getExternalStorageDirectory() + "/Android/1.jpg")
//                    .setTargetDir(Environment.getExternalStorageDirectory() + "/Android")
//                    .launch();
//
//            //以下代码是为了演示解决过度绘制问题，可以换成解决内存抖动等方面的代码
//            Intent intent = new Intent(view.getContext(), SolveOverDrawActivity.class);
//            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            view.getContext().startActivity(intent);
        }
    }
}
