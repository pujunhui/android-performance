package com.optimize.performance.tasks;

import com.optimize.performance.launchstarter.task.Task;
import com.tencent.mmkv.MMKV;

public class InitMMKVTask extends Task {
    @Override
    public void run() {
        MMKV.initialize(mContext);
    }
}
