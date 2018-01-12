package com.colorchen.qbase.utils;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore.Images;
import android.support.v4.content.FileProvider;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ListAdapter;
import android.widget.ListView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Random;

public class SystemUtil {
    public static final String DEVICE_INFO = "deviceInfo";
    public static final String RANDOM_DEVICE_ID = "random_device_id";
    public static final String HAS_COPY_TO_SDCARD = "has_copy_to_sdcard";

    /**
     * 计算listview高度
     *
     * @param listView
     */
    public static void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
            return;
        }

        int totalHeight = 0;
        for (int i = 0; i < listAdapter.getCount(); i++) {
            View listItem = listAdapter.getView(i, null, listView);
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
    }

    /**
     * 安装apk
     *
     * @param activity
     */
    public static void installApk(Activity activity, String filePath, String applicationId) {
        if (TextUtils.isEmpty(filePath))
            return;
        File apkFile = new File(filePath);
        if (!apkFile.exists())
            return;
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Uri uri;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            uri = FileProvider.getUriForFile(activity, applicationId + ".fileprovider", apkFile);
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } else {
            uri = Uri.fromFile(apkFile);
        }
        intent.setDataAndType(uri, "application/vnd.android.package-archive");
        activity.startActivityForResult(intent, 1);
    }

    /**
     * 移除保存的随机设备id信息
     *
     * @param context
     */
    public static void removeRandomDeviceId(Context context) {
        try {
            SharedPreferences sharedPre = context.getSharedPreferences(DEVICE_INFO, Context.MODE_PRIVATE);
            sharedPre.edit().remove(RANDOM_DEVICE_ID).commit();
            if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                return;
            }
            File externalFile = Environment.getExternalStorageDirectory();
            String filePath = externalFile.getAbsolutePath() + "/" + context.getPackageName() + "/files/randomDeviceInfo.txt";
            File deviceInfoFile = new File(filePath);
            if (!deviceInfoFile.exists()) {
                return;
            } else {
                deviceInfoFile.delete();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getDeviceId(Context context) {
        SharedPreferences sharedPre = context.getSharedPreferences(DEVICE_INFO, Context.MODE_PRIVATE);
        String oldRandomDeviceId = sharedPre.getString(RANDOM_DEVICE_ID, "");
        try {
            String oldRandomDeviceIdFromSdcard = getRandomDeviceIdFromSdCard(context);
            if (TextUtils.isEmpty(oldRandomDeviceId)) {
                if (!TextUtils.isEmpty(oldRandomDeviceIdFromSdcard)) {
                    oldRandomDeviceId = oldRandomDeviceIdFromSdcard;
                } else {
                    oldRandomDeviceId = getRandomDeviceId();
                    sharedPre.edit().putString(RANDOM_DEVICE_ID, oldRandomDeviceId).commit();
                    saveRandomIdToSdCard(context);
                }
            }
            String deviceId = "";
            try {
                TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                deviceId = tm.getDeviceId();
            } catch (Exception e) {
            }
            return deviceId + oldRandomDeviceId;
        } catch (Exception e) {
            return oldRandomDeviceId;
        }
    }

    public static boolean isEmptyDeviceId(Context context) {
        try {
            TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            return TextUtils.isEmpty(tm.getDeviceId());
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * 判断apk是否debugable
     *
     * @param context
     * @return
     */
    public static boolean isApkDebugable(Context context) {
        try {
            ApplicationInfo info = context.getApplicationInfo();
            return (info.flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
        } catch (Exception e) {

        }
        return false;
    }

    /**
     * 显示软键盘
     *
     * @param context
     * @param edit
     */
    public static void showSoftInput(Context context, View edit) {
        if (context == null || edit == null) {
            return;
        }
        InputMethodManager inputManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputManager != null && inputManager.isActive(edit)) {
            inputManager.showSoftInput(edit, 0);
        }
    }

    /**
     * 隐藏软键盘
     *
     * @param context
     * @param edit
     */
    public static void hideSoftInput(Context context, View edit) {
        if (context == null || edit == null) {
            return;
        }
        InputMethodManager inputManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputManager != null && inputManager.isActive(edit)) {
            inputManager.hideSoftInputFromWindow(edit.getWindowToken(), 0);
        }
    }

    /**
     * 隐藏软键盘
     */
    public static void hideSoftInput(Activity activity) {
        if (activity == null) {
            return;
        }
        InputMethodManager inputManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputManager != null && inputManager.isActive()) {
            View focusView = activity.getCurrentFocus();
            if (focusView != null) {
                inputManager.hideSoftInputFromWindow(focusView.getWindowToken(), 0);
            }
        }
    }

    public static boolean isSoftInputShow(Activity activity) {
        boolean isShowed = false;
        try {
            if (activity.getWindow().getAttributes().softInputMode == WindowManager.LayoutParams.SOFT_INPUT_STATE_UNSPECIFIED) {
                isShowed = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return isShowed;
    }

    /**
     * 切换软键盘
     */
    public static void toggleSoftInput(Activity activity) {
        InputMethodManager inputManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
    }

    /**
     * 获取当前系统每个app的内存等级，即最大使用内存
     *
     * @param context
     * @return
     */
    public static int getMemoryClass(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        return activityManager.getMemoryClass();
    }

    /**
     * 获取进程ID
     *
     * @return
     */
    public static int myPid() {
        return android.os.Process.myPid();
    }

    /**
     * 获取进程名
     *
     * @param context
     * @return
     */
    public static String getCurProcessName(Context context) {
        if (context != null) {
            ActivityManager mActivityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            for (ActivityManager.RunningAppProcessInfo appProcess : mActivityManager.getRunningAppProcesses()) {
                if (appProcess.pid == myPid()) {
                    return appProcess.processName;
                }
            }
        }
        return "";
    }

    /**
     * 是否在wifi网络中
     *
     * @return
     */
    public static boolean isWifiConnected(Context context) {
        if (context == null) {
            return false;
        }
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return (netInfo != null && netInfo.getType() == ConnectivityManager.TYPE_WIFI);
    }

    /**
     * 是否连网
     *
     * @param context
     * @return
     */
    public static boolean isNetworkConected(Context context) {
        if (context != null) {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm != null) {
                NetworkInfo netInfo = cm.getActiveNetworkInfo();
                return (netInfo != null && netInfo.isAvailable());
            }
        }
        return false;
    }

    /**
     * 检测储存卡是否可用
     *
     * @return
     */
    public static boolean isSDCardAvailable() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    /**
     * 获取IMEI码
     *
     * @param context
     * @return
     */
    public static String getImei(Context context) {
        if (context == null) {
            return "0";
        }
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        String imei = tm.getDeviceId();
        if (TextUtils.isEmpty(imei)) {
            imei = "0";
        }
        return imei;
    }

    /**
     * 获取系统型号
     *
     * @return
     */
    public static String getSysModel() {
        return Build.MODEL;
    }

    /**
     * 获取手机屏幕密度
     *
     * @return
     */
    public static float getDensity(Context context) {
        DisplayMetrics dm = new DisplayMetrics();
        dm = context.getResources().getDisplayMetrics();
        return dm.density; // 屏幕密度（像素比例：0.75/1.0/1.5/2.0）
    }

    /**
     * 获取系统版本号
     *
     * @return
     */
    public static String getSysVersion() {
        return Build.VERSION.RELEASE;
    }

    /**
     * 获取当前app版本号
     *
     * @return
     */
    public static int getVersionCode(Context context) {
        try {
            String packageName = context.getPackageName();
            PackageInfo info = context.getPackageManager().getPackageInfo(packageName, 0);
            return info.versionCode;
        } catch (NameNotFoundException e) {
            return 0;
        }
    }

    /**
     * 获取当前app版本号
     *
     * @return
     */
    public static String getVersionName(Context context) {
        try {
            String packageName = context.getPackageName();
            PackageInfo info = context.getPackageManager().getPackageInfo(packageName, 0);
            return info.versionName;
        } catch (NameNotFoundException e) {
            return "";
        }
    }

    public static String getAppName(Context context) {
        try {
            String packageName = context.getPackageName();
            PackageInfo info = context.getPackageManager().getPackageInfo(packageName, 0);
            return info.applicationInfo.name;
        } catch (NameNotFoundException e) {
            return "";
        }
    }

    /**
     * 获取手机IP地址
     *
     * @return
     */
    public static String getLocalIpAddress() {
        try {
            Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();
            while (en.hasMoreElements()) {
                NetworkInterface intf = en.nextElement();
                Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses();
                while (enumIpAddr.hasMoreElements()) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        return inetAddress.getHostAddress().toString();
                    }
                }
            }
        } catch (SocketException ex) {
        }
        return "";
    }

    /**
     * 获取系统相册路径, 耗时操作
     *
     * @param context
     * @return
     */
    public static String getAlbumPath(Context context) {
        if (context == null) {
            return null;
        }
        ContentResolver cr = context.getContentResolver();
        ContentValues values = new ContentValues();
        values.put(Images.Media.TITLE, "title");
        values.put(Images.Media.DESCRIPTION, "description");
        values.put(Images.Media.MIME_TYPE, "image/jpeg");
        Uri url = cr.insert(Images.Media.EXTERNAL_CONTENT_URI, values);
        // 查询系统相册数据
        Cursor cursor = Images.Media.query(cr, url, new String[]{
                Images.Media.DATA
        });
        String albumPath = null;
        if (cursor != null && cursor.moveToFirst()) {
            int column_index = cursor.getColumnIndexOrThrow(Images.Media.DATA);
            albumPath = cursor.getString(column_index);
            try {
                cursor.close();
            } catch (Exception e) {
            }
        }
        cr.delete(url, null, null);
        if (albumPath == null) {
            return null;
        }

        File albumDir = new File(albumPath);
        if (albumDir.isFile()) {
            albumDir = albumDir.getParentFile();
        }
        // 如果系统相册目录不存在,则创建此目录
        if (!albumDir.exists()) {
            albumDir.mkdirs();
        }
        albumPath = albumDir.getAbsolutePath();
        return albumPath;
    }

    public static int getDisplayWidth(Activity context) {
        DisplayMetrics dm = new DisplayMetrics();
        context.getWindowManager().getDefaultDisplay().getMetrics(dm);
        return dm.widthPixels;
    }

    public static int getDisplayHeight(Activity context) {
        DisplayMetrics dm = new DisplayMetrics();
        context.getWindowManager().getDefaultDisplay().getMetrics(dm);
        return dm.heightPixels;
    }

/*
    public static String getDeviceID(Context context){
       WifiManager manager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = manager.getConnectionInfo();
        return info.getMacAddress();
    }*/

    public static String getRandomDeviceId() {
        int max = 999999;
        int min = 100000;
        Random random = new Random();
        //6 位数字
        int randomInt = random.nextInt(max) % (max - min + 1) + min;
        String randomString = randomInt + "";
        char[] chararray = randomString.toCharArray();
        // 取三个随机的位置
        int randomPos = random.nextInt(6);
        int randomPos2 = random.nextInt(6);
        int randomPos3 = random.nextInt(6);
        char[] charsets = new char[]{'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'G', 'H', 'I', 'J', 'H', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
                'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'};
        int randomPosChar1 = random.nextInt(52);
        int randomPosChar2 = random.nextInt(52);
        int randomPosChar3 = random.nextInt(52);
        //产生
        chararray[randomPos] = charsets[randomPosChar1];
        chararray[randomPos2] = charsets[randomPosChar2];
        chararray[randomPos3] = charsets[randomPosChar3];
        String randomDeviceId = String.copyValueOf(chararray);
        return randomDeviceId;
    }

    public static void saveRandomIdToSdCard(Context context) {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            try {
                SharedPreferences sharedPre = context.getSharedPreferences(DEVICE_INFO, Context.MODE_PRIVATE);
                String oldRandomDeviceId = sharedPre.getString(RANDOM_DEVICE_ID, "");
                if (TextUtils.isEmpty(oldRandomDeviceId)) {
                    return;
                }

                File externalFile = Environment.getExternalStorageDirectory();
                String folderPath = externalFile.getAbsolutePath() + "/" + context.getPackageName() + "/files/";
                File deviceFolder = new File(folderPath);

                if (!deviceFolder.exists()) {
                    deviceFolder.mkdirs();
                }
                String filePath = folderPath + "randomDeviceInfo.txt";
                File deviceFile = new File(filePath);
                if (!deviceFile.exists()) {
                    deviceFile.createNewFile();
                }
                FileOutputStream fos = new FileOutputStream(deviceFile);
                fos.write(oldRandomDeviceId.getBytes());
                fos.close();

                //设置已经保存到sd卡上了
                SharedPreferences.Editor editor = sharedPre.edit();
                editor.putLong(HAS_COPY_TO_SDCARD, 1);
                editor.commit();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 获取sd卡上保存的随机deviceId信息
     *
     * @param context
     * @return
     */
    public static String getRandomDeviceIdFromSdCard(Context context) {
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            return "";
        }
        File externalFile = Environment.getExternalStorageDirectory();
        String filePath = externalFile.getAbsolutePath() + "/" + context.getPackageName() + "/files/randomDeviceInfo.txt";
        File deviceInfoFile = new File(filePath);
        if (!deviceInfoFile.exists()) {
            return "";
        }

        StringBuilder text = new StringBuilder();
        try {
            BufferedReader br = new BufferedReader(new FileReader(deviceInfoFile));
            String line;
            while ((line = br.readLine()) != null) {
                text.append(line);
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        String deviceId = "";
        if (!TextUtils.isEmpty(text)) {
            deviceId = text.toString().replace("\n", "");
        }
        return deviceId;
    }
}
