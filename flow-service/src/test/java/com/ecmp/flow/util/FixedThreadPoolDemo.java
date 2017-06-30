package com.ecmp.flow.util;

/**
 * *************************************************************************************************
 * <p/>
 * 实现功能：
 * <p>
 * ------------------------------------------------------------------------------------------------
 * 版本          变更时间             变更人                     变更原因
 * ------------------------------------------------------------------------------------------------
 * 1.0.00      2017/6/27 10:43      谭军(tanjun)                    新建
 * <p/>
 * *************************************************************************************************
 */
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class FixedThreadPoolDemo {

    public static void main(String[] args) {

        ExecutorService es  = Executors.newSingleThreadExecutor();
//      ExecutorService es = Executors.newCachedThreadPool();

        for (int i = 0; i < 10; i++) {
            final int count = i;
            //过两秒启动线程运行里面的run方法
            es.submit(new Runnable() {
                @Override
                public void run() {
                    System.out.println(Thread.currentThread().getName() + "----------" + count);
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });
        }

        System.out.println(es.isShutdown());
        //等待线程运行完毕之后再停止线程。
        es.shutdown();
        //强制停止线程，如果当前线程正在执行，则被强制停止。
//      es.shutdownNow();
        System.out.println(es.isShutdown());

    }

}