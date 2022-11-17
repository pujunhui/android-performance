package com.optimize.performance.handler;

import android.os.Message;

import java.util.concurrent.ConcurrentHashMap;

public class GetDetailHandlerHelper {

    private static final ConcurrentHashMap<Message, String> sMsgDetail = new ConcurrentHashMap<>();

    public static ConcurrentHashMap<Message, String> getMsgDetail() {
        return sMsgDetail;
    }

}
