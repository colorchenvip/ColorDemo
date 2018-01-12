package com.colorchen.qbase.utils;

import android.app.Activity;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by color on 2018/1/7 10:25.
 */

public class PermissionUtils {

    //注解的作用域
    @Target(ElementType.METHOD)
    //注解的有效时
    @Retention(RetentionPolicy.RUNTIME)
    public @interface PermissionHelper {
        boolean permissionResult();
        int requestCode();
    }

    public static void injectActivity(Activity activity, boolean permissionResult, int requestCode) {

        Class clazz = activity.getClass();
//      clazz.getMethods();
        Method[] methods = clazz.getDeclaredMethods();

        for (Method method : methods) {
            if (method.isAnnotationPresent(PermissionHelper.class)) {
                PermissionHelper annotation = method.getAnnotation(PermissionHelper.class);
                if (permissionResult == annotation.permissionResult() && annotation.requestCode() == requestCode) {
                    try {
                        method.setAccessible(true);
                        method.invoke(activity);
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                    break;
                }
            }
        }
    }
}
