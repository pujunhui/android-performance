package com.optimize.performance.aop;

import com.optimize.performance.utils.LogUtils;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

@Aspect
public class PerformanceAop {

    @Around("call(* com.optimize.performance.PerformanceApp.init**(..))")
    public void getTime(ProceedingJoinPoint joinPoint) {
        Signature signature = joinPoint.getSignature();
        String name = signature.getName();
        long time = System.currentTimeMillis();
        try {
            joinPoint.proceed();
        } catch (Throwable e) {
            e.printStackTrace();
        }
        LogUtils.i(name + " cost " + (System.currentTimeMillis() - time) + "ms");
    }

    @Around("call(* androidx.appcompat.app.AppCompatActivity.setContentView(..))")
    public void getSetContentViewTime(ProceedingJoinPoint joinPoint) {
        Signature signature = joinPoint.getSignature();
        String name = signature.toShortString();
        long time = System.currentTimeMillis();
        try {
            joinPoint.proceed();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        LogUtils.i(name + " cost " + (System.currentTimeMillis() - time));
    }
}
