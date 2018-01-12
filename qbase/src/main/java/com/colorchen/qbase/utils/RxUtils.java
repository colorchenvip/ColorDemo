package com.colorchen.qbase.utils;

import android.os.Build;


import com.orhanobut.logger.Logger;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableTransformer;
import io.reactivex.Observable;
import io.reactivex.ObservableTransformer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by zhaojieb9 on 2017/12/6.
 */

public class RxUtils {
    /**
     * 统一线程处理
     */
    public static <T> FlowableTransformer<T, T> rxFlowableScheduler() {    //compose简化线程
        return upstream -> upstream.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public static <T> ObservableTransformer<T, T> rxObservableScheduler() {    //compose简化线程
        return upstream -> upstream.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * 权限判断默认6.0以下都是有权限
     *
     * @return ObservableTransformer
     */
    public static ObservableTransformer<Boolean, Boolean> filterAndroidVersion() {   //compose判断结果
        return upstream -> {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                return Observable.just(true);
            } else {
                return upstream;
            }
        };
    }


    /**
     * 生成Flowable
     */
    public static <T> Flowable<T> createFlowable(final T t) {
        return Flowable.create(emitter -> {
            try {
                emitter.onNext(t);
                emitter.onComplete();
            } catch (Exception e) {
               Logger.e(e.getMessage(), e);
            }
        }, BackpressureStrategy.BUFFER);
    }
}
