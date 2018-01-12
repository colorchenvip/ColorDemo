package com.colorchen.lib.qjsbridge;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.colorchen.lib.qimagepicker.ImagePicker;
import com.colorchen.lib.qimagepicker.bean.ImageItem;
import com.colorchen.lib.qimagepicker.ui.ImageGridActivity;
import com.colorchen.lib.qjsbridge.bridge.OnReturnValue;
import com.colorchen.lib.qjsbridge.bridge.YTWebView;
import com.colorchen.lib.qjsbridge.imagepicker.GlideImageLoader;
import com.colorchen.lib.qjsbridge.util.Base64Util;
import com.colorchen.qbase.model.AppLogInfo;
import com.google.gson.Gson;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import at.grabner.circleprogress.CircleProgressView;
import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import top.zibin.luban.Luban;

/**
 * Created by wangsye on 2017-11-28.
 */

public class WebViewFragment extends Fragment implements JsApiCallBackListener, YTWebView.WebViewEventListener {
    /**
     * 显示的网页的url
     */
    public static final String EXTRA_LOAD_URL = "EXTRA_LOAD_URL";
    /**
     * 是否显示actionbar
     */
    public static final String EXTRA_SHOW_ACTIONBAR = "EXTRA_SHOW_ACTIONBAR";
    public static final String EXTRA_APP_LOGIN_INFO = "EXTRA_APP_LOGIN_INFO";
    public View rootView;
    public YTWebView ytWebView;
    public JsApi jsApi;
    public ImagePicker imagePicker;
    public int needBase64;
    public CircleProgressView circleProgressBar;
    public FrameLayout downloadingProgressContainer;
    public boolean needShowLoadingUrlProgress;
    public ActivityListener activityListener;
    protected AnimationDrawable loadingAnimation;
    protected String originUrl;
    protected boolean autoLoadUrl = true;
    private boolean showActionBar;

    public void setActivityListener(ActivityListener webViewActionListener) {
        this.activityListener = webViewActionListener;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.activity_webview, container, false);
        downloadingProgressContainer = (FrameLayout) rootView.findViewById(R.id.downloadingProgressContainer);
        circleProgressBar = (CircleProgressView) rootView.findViewById(R.id.circleView);
        ytWebView = (YTWebView) rootView.findViewById(R.id.webview);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ytWebView.setActivity(getActivity());
        ytWebView.setWebViewEventListener(this);
        initJsApi();
        if (jsApi == null) {
            jsApi = new JsApi();
        }
        jsApi.setContext(getActivity());
        jsApi.setActivity(getActivity());
        ytWebView.setActivity(getActivity());
        jsApi.jsApiCallBackListener = this;
        ytWebView.setJavascriptInterface(jsApi);
        ytWebView.clearCache(true);
        imagePicker = ImagePicker.getInstance();
        imagePicker.setImageLoader(new GlideImageLoader());
        try {
            Bundle exBundle = getActivity().getIntent().getExtras();
            if (exBundle!=null){
                showActionBar = exBundle.getBoolean(EXTRA_SHOW_ACTIONBAR, true);
                originUrl = exBundle.getString(EXTRA_LOAD_URL);
            }
            if (!showActionBar) {
                if (activityListener != null) {
                    activityListener.hideNav();
                }
            }

            initAutoLoadUrl();
            if (autoLoadUrl) {
                ytWebView.loadUrl(originUrl);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 初始化JsApi，子类可以重写该方法，实现自己的JsApi
     */
    public void initJsApi() {
        if (activityListener != null) {
            jsApi = activityListener.initJsApi();
        }
    }

    /**
     * 是否默认加载传递过来的url
     */
    public void initAutoLoadUrl() {
        if (activityListener != null) {
            autoLoadUrl = activityListener.initAutoLoadUrl();
        }
    }

    public void ShowDownloadProgressBar() {
        downloadingProgressContainer.setVisibility(View.VISIBLE);
    }

    public void hideDownloadProgressBar() {
        downloadingProgressContainer.setVisibility(View.GONE);
    }

    public void loadUrl(final String url) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                ytWebView.loadUrl(url);
            }
        });
    }

    public void loadUrl(final String url, final Map<String, String> additionalHttpHeaders) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                ytWebView.loadUrl(url, additionalHttpHeaders);
            }
        });
    }

    public boolean canGoback() {
        return ytWebView.canGoBack();
    }

    public void goback() {
        ytWebView.goBack();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == JsApi.REQUEST_QR_CODE) {
            CallBackResult<String> qrcodeResult = new CallBackResult<>();
            if (resultCode == Activity.RESULT_OK && requestCode == JsApi.REQUEST_QR_CODE && data != null) {
                String result = data.getStringExtra("result");
                qrcodeResult.code = TextUtils.isEmpty(result) ? 1 : 0;
                qrcodeResult.data = TextUtils.isEmpty(result) ? "" : result;
                qrcodeResult.extras = new HashMap<>();
                qrcodeResult.extras.put("scanType", data.getIntExtra("scanType", 1) + "");
            } else {
                qrcodeResult.code = 1;
            }
            if (jsApi.scanQrcodeHandler != null) {
                jsApi.scanQrcodeHandler.complete(new Gson().toJson(qrcodeResult));
            }
        }
        if (requestCode == JsApi.REQUEST_CODE_TAKE_PICTURE_FROM_CAMERA || requestCode == JsApi.REQUEST_CODE_TAKE_PICTURE_FROM_GALLERY) {
            CallBackResult<ArrayList<String>> imagesResult = new CallBackResult<>();
            if (resultCode == ImagePicker.RESULT_CODE_ITEMS && data != null) {
                ArrayList<ImageItem> images = (ArrayList<ImageItem>) data.getSerializableExtra(ImagePicker.EXTRA_RESULT_ITEMS);
                ArrayList<String> imagePaths = new ArrayList<>();
                for (ImageItem imageItem : images) {
                    if (imageItem == null || TextUtils.isEmpty(imageItem.path)) {
                        continue;
                    }
                    imagePaths.add(imageItem.path);
                }
                if (imagePaths.size() == 0) {
                    imagesResult.code = 1;
                    if (jsApi.imagePickerHandler != null) {
                        jsApi.imagePickerHandler.complete(new Gson().toJson(imagesResult));
                    }
                    return;
                }
                if (needBase64 == 0) {
                    imagesResult.code = 1;
                    imagesResult.data = imagePaths;
                    if (jsApi.imagePickerHandler != null) {
                        jsApi.imagePickerHandler.complete(new Gson().toJson(imagesResult));
                    }
                    return;
                }
                //压缩图片
                compressImages(imagePaths);
            } else {
                imagesResult.code = 1;
                if (jsApi.imagePickerHandler != null) {
                    jsApi.imagePickerHandler.complete(new Gson().toJson(imagesResult));
                }
            }
        }
        if (requestCode == YTWebView.FILECHOOSER_RESULTCODE || requestCode == YTWebView.FILECHOOSER_RESULTCODE_FOR_ANDROID_5) {
            ytWebView.onActivityResult(requestCode, resultCode, data);
        }
    }


    private void compressImages(final ArrayList<String> imagePaths) {
        Flowable.just(imagePaths)
                .observeOn(Schedulers.io())
                .map(new Function<List<String>, List<ImagePickerResult>>() {
                    @Override
                    public List<ImagePickerResult> apply(@NonNull List<String> list) throws Exception {
                        List<File> compressedFiles = Luban.with(WebViewFragment.this.getActivity()).load(list).get();
                        ArrayList<ImagePickerResult> results = new ArrayList<ImagePickerResult>();
                        for (File file : compressedFiles) {
                            String encodedImage = Base64Util.encodeFileToBase64(file);
                            if (!TextUtils.isEmpty(encodedImage)) {
                                ImagePickerResult result = new ImagePickerResult();
                                result.data = encodedImage;
                                result.filepath = file.getAbsolutePath();
                                result.ext = file.getAbsolutePath().substring(file.getAbsolutePath().lastIndexOf(".") + 1);
                                results.add(result);
                            }
                        }
                        return results;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<List<ImagePickerResult>>() {
                    @Override
                    public void accept(@NonNull List<ImagePickerResult> results) throws Exception {
                        CallBackResult<List<ImagePickerResult>> imagesResult = new CallBackResult<>();
                        if (results.size() == 0) {
                            imagesResult.code = 1;
                        } else {
                            imagesResult.code = 0;
                            imagesResult.data = results;
                        }
                        if (jsApi.imagePickerHandler != null) {
                            jsApi.imagePickerHandler.complete(new Gson().toJson(imagesResult));
                        }
                    }
                });
    }

    @Override
    public void takePictureFromCamera(int needBase64) {
        this.needBase64 = needBase64;
        Intent intent = new Intent(getActivity(), ImageGridActivity.class);
        // 是否是直接打开相机
        intent.putExtra(ImageGridActivity.EXTRAS_TAKE_PICKERS, true);
        startActivityForResult(intent, JsApi.REQUEST_CODE_TAKE_PICTURE_FROM_CAMERA);
    }

    @Override
    public void takePictureFromGallery(int limit, int needBase64) {
        this.needBase64 = needBase64;
        imagePicker.setShowCamera(false);
        imagePicker.setCrop(false);
        imagePicker.setSelectLimit(limit);
        Intent intent = new Intent(getActivity(), ImageGridActivity.class);
        startActivityForResult(intent, JsApi.REQUEST_CODE_TAKE_PICTURE_FROM_GALLERY);
    }

    /**
     * 返回键处理，需要Activity使用
     */
    public void onBackPressed() {
        if (jsApi.processBackPressed == 1) {
            ytWebView.callHandler("processBackPressedAndroid", new Object[]{}, new OnReturnValue() {
                @Override
                public void onValue(String retValue) {
                    if (TextUtils.equals("1", retValue)) {
                        Log.d("MainActivity", "processBackPressedAndroid");
                    } else {
                        new Handler().post(new Runnable() {
                            @Override
                            public void run() {
                                if (canGoback()) {
                                    goback();
                                } else {
                                    getActivity().finish();
                                }
                            }
                        });
                    }
                }
            });
        } else {
            if (canGoback()) {
                goback();
            } else {
                getActivity().finish();
            }
        }
    }

    /**
     * 获取用户信息
     */
    @Override
    public void getUserInfo() {
        String userInfo = "";
        if (activityListener != null) {
            userInfo = activityListener.getUserDetail();
        }
        if (jsApi.userInfoHandler != null) {
            jsApi.userInfoHandler.complete(userInfo);
        }
    }


    @Override
    public void showNav() {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                if (activityListener != null) {
                    activityListener.showNav();
                }
            }
        });
    }

    @Override
    public void hideNav() {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                if (activityListener != null) {
                    activityListener.hideNav();
                }
            }
        });
    }

    @Override
    public void onUrlLoadProgress(String originUrl, String url, int progress) {
        if (downloadingProgressContainer.getVisibility() == View.VISIBLE) {
            circleProgressBar.setValue(progress);
        }
    }

    @Override
    public void onUrlStartLoad(String url) {
        if (!TextUtils.isEmpty(url) && downloadingProgressContainer.getVisibility() == View.GONE) {
            downloadingProgressContainer.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onUrlEndLoad(String url) {
        if (url.equals(originUrl) || downloadingProgressContainer.getVisibility() == View.VISIBLE) {
            downloadingProgressContainer.setVisibility(View.GONE);
        }
    }

    @Override
    public void onReceiveTitle(String url, String title) {
        if (activityListener != null) {
            activityListener.receiveTitle(url, title);
        }
    }

    @Override
    public void onJumpActivity() {
        if (activityListener != null){
            activityListener.goToActivity();
        }
    }

    @Override
    public void onLogout() {
        if (activityListener != null){
            activityListener.onLogout();
        }
    }

    @Override
    public AppLogInfo getAppLogInfo() {
        if (activityListener != null) {
            return activityListener.getAppLogInfo();
        }
        return new AppLogInfo();
    }

    @Override
    public void showRightMenu(String menuTitle, String menuFunction, String imageIcon) {
        if (activityListener != null) {
            activityListener.showRightMenu(menuTitle, menuFunction, imageIcon);
        }
    }

    @Override
    public void changeTitle(String title) {
        if (activityListener != null) {
            activityListener.changeTitle(title);
        }
    }

    public interface ActivityListener {
        /**
         * 获取用户信息
         *
         * @return
         */
        String getUserDetail();

        /**
         * 隐藏导航栏
         */
        void hideNav();

        /**
         * 显示导航栏
         */
        void showNav();

        /**
         * 初始JsApi
         */
        JsApi initJsApi();

        /**
         * 是否自动加载url
         */
        boolean initAutoLoadUrl();

        void receiveTitle(String url, String title);

        AppLogInfo getAppLogInfo();

        /**
         * 右上角操作
         *
         * @param menuTitle
         * @param menuFunction
         * @param imageIcon
         */
        void showRightMenu(String menuTitle, String menuFunction, String imageIcon);

        /**
         * 修改title
         *
         * @param title
         */
        void changeTitle(String title);


        /**
         * 拦截跳转回调
         */
        void goToActivity();

        /**
         * 拦截退出回调
         */
        void onLogout();
    }
}
