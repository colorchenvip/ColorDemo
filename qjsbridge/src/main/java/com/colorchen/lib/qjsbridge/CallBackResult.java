package com.colorchen.lib.qjsbridge;


import java.util.HashMap;

/**
 * 异步回调结果
 *
 * @param <T>
 */
public class CallBackResult<T> {
    /**
     * 是否成功，0：成功，1：不成功
     */
    public int code;
    /**
     * 数据
     */
    public T data;
    /**
     * 额外的信息
     */
    public HashMap<String, Object> extras;
}
