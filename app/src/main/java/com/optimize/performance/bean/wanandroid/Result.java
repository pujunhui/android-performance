package com.optimize.performance.bean.wanandroid;

public class Result<T> {
    private final T data;
    private final int errorCode;
    private final String errorMsg;

    public Result(T data, int errorCode, String errorMsg) {
        this.data = data;
        this.errorCode = errorCode;
        this.errorMsg = errorMsg;
    }

    public T getData() {
        return data;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public String getErrorMsg() {
        return errorMsg;
    }
}
