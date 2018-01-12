package com.colorchen.lib.qjsbridge;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
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
 * name：WebViewActivity
 * @author: ChenQ
 * @date: 2018-1-5
 */
public class WebViewActivity extends AppCompatActivity implements JsApiCallBackListener, YTWebView.WebViewEventListener {
    public JsApi jsApi;
    public YTWebView webView;
    public ImagePicker imagePicker;
    /**
     * 显示的网页的url
     */
    public static final String EXTRA_LOAD_URL = "EXTRA_LOAD_URL";
    /**
     * 是否显示actionbar
     */
    public static final String EXTRA_SHOW_ACTIONBAR = "EXTRA_SHOW_ACTIONBAR";
    public static final String EXTRA_APP_LOGIN_INFO = "EXTRA_APP_LOGIN_INFO";
    public int needBase64;
    private boolean showActionBar;
    protected AnimationDrawable loadingAnimation;
    protected String originUrl;
    protected boolean autoLoadUrl = true;
    protected CircleProgressView circleProgressBar;
    protected FrameLayout downloadingProgressContainer;
    protected boolean needShowLoadingUrlProgress;
    protected AppLogInfo appLogInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);
        initActionBar();
        downloadingProgressContainer = (FrameLayout) findViewById(R.id.downloadingProgressContainer);
        circleProgressBar = (CircleProgressView) findViewById(R.id.circleView);
        webView = (YTWebView) findViewById(R.id.webview);
        webView.setActivity(this);
        webView.setWebViewEventListener(this);
        initJsApi();
        if (jsApi == null) {
            jsApi = new JsApi();
        }
        jsApi.registerEventBus();
        jsApi.setContext(this);
        jsApi.setActivity(this);
        jsApi.webView = webView;
        jsApi.jsApiCallBackListener = this;
        webView.setJavascriptInterface(jsApi);
        webView.clearCache(true);

        imagePicker = ImagePicker.getInstance();
        imagePicker.setImageLoader(new GlideImageLoader());
        try {
            showActionBar = getIntent().getExtras().getBoolean(EXTRA_SHOW_ACTIONBAR, true);
            if (!showActionBar) {
                getSupportActionBar().hide();
            }
            originUrl = getIntent().getExtras().getString(EXTRA_LOAD_URL);
            appLogInfo = getIntent().getExtras().getParcelable(EXTRA_APP_LOGIN_INFO);
            initAutoLoadUrl();
            if (autoLoadUrl) {
                webView.loadUrl(originUrl);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null){
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            try {
                showActionBar = getIntent().getExtras().getBoolean(EXTRA_SHOW_ACTIONBAR, true);
                if (!showActionBar) {
                    actionBar.hide();
                }
            } catch (Throwable e) {
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * 初始化JsApi，子类可以重写该方法，实现自己的JsApi
     */
    public void initJsApi() {
    }

    /**
     * 是否默认加载传递过来的url
     */
    public void initAutoLoadUrl() {
    }

    public void ShowDownloadProgressBar() {
        downloadingProgressContainer.setVisibility(View.VISIBLE);
    }

    public void hideDownloadProgressBar() {
        downloadingProgressContainer.setVisibility(View.GONE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (loadingAnimation != null && loadingAnimation.isRunning()) {
            loadingAnimation.stop();
        }
        jsApi.unRegisterEventBus();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == JsApi.REQUEST_QR_CODE) {
            CallBackResult<String> qrcodeResult = new CallBackResult<>();
            if (resultCode == RESULT_OK && requestCode == JsApi.REQUEST_QR_CODE && data != null) {
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
                    imagesResult.code = 0;
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
            webView.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void compressImages(final ArrayList<String> imagePaths) {
        Flowable.just(imagePaths)
                .observeOn(Schedulers.io())
                .map((Function<List<String>, List<ImagePickerResult>>) list -> {
                    List<File> compressedFiles = Luban.with(WebViewActivity.this).load(list).get();
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
        imagePicker.setCrop(false);
        Intent intent = new Intent(this, ImageGridActivity.class);
        intent.putExtra(ImageGridActivity.EXTRAS_TAKE_PICKERS, true);
        startActivityForResult(intent, JsApi.REQUEST_CODE_TAKE_PICTURE_FROM_CAMERA);
    }

    @Override
    public void takePictureFromGallery(int limit, int needBase64) {
        this.needBase64 = needBase64;
        imagePicker.setShowCamera(false);
        imagePicker.setCrop(false);
        imagePicker.setSelectLimit(limit);
        Intent intent = new Intent(this, ImageGridActivity.class);
        startActivityForResult(intent, JsApi.REQUEST_CODE_TAKE_PICTURE_FROM_GALLERY);
    }

    @Override
    public void onBackPressed() {
        if (jsApi.processBackPressed == 1) {
            webView.callHandler("processBackPressedAndroid", new Object[]{}, new OnReturnValue() {
                @Override
                public void onValue(String retValue) {
                    if (TextUtils.equals("1", retValue)) {
                        Log.d("MainActivity", "processBackPressedAndroid");
                    } else {
                        if (canGoback()) {
                            goback();
                        } else {
                            finish();
                        }
                    }
                }
            });
        } else {
            if (canGoback()) {
                goback();
            } else {
                super.onBackPressed();
            }
        }
    }

    public void loadUrl(final String url) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                webView.loadUrl(url);
            }
        });
    }

    public void loadUrl(final String url, final Map<String, String> additionalHttpHeaders) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                webView.loadUrl(url, additionalHttpHeaders);
            }
        });
    }

    public boolean canGoback() {
        return webView.canGoBack();
    }

    public void goback() {
        webView.goBack();
    }

    @Override
    public void getUserInfo() {
        String userInfo = getUserInfoDetail();
        if (jsApi.userInfoHandler != null) {
            jsApi.userInfoHandler.complete(userInfo);
        }
    }

    public String getUserInfoDetail() {
        return "";
    }


    @Override
    public void showNav() {
        getSupportActionBar().show();
    }

    @Override
    public void hideNav() {
        getSupportActionBar().hide();
    }

    public void sendPushMsgToJS(String msg) {
        webView.evaluateJavascript("receivePushedMessage(\'" + msg + "\');");
    }

    @Override
    public void onUrlLoadProgress(String originUrl, String url, int progress) {
        if (needShowLoadingUrlProgress && downloadingProgressContainer.getVisibility() == View.VISIBLE) {
            circleProgressBar.setValue(progress);
        }
    }

    @Override
    public void onUrlStartLoad(String url) {
    }

    @Override
    public void onUrlEndLoad(String url) {
        if (url.equals(originUrl) || downloadingProgressContainer.getVisibility() == View.VISIBLE) {
            downloadingProgressContainer.setVisibility(View.GONE);
        }
    }

    @Override
    public void onReceiveTitle(String url, String title) {
        getSupportActionBar().setTitle(title);
    }

    @Override
    public void onJumpActivity() {

    }

    @Override
    public void onLogout() {

    }

    @Override
    public AppLogInfo getAppLogInfo() {
        return appLogInfo;
    }

    @Override
    public void showRightMenu(String menuTitle, String menuFunction, String imageIcon) {

    }

    @Override
    public void changeTitle(String title) {
        getSupportActionBar().setTitle(title);
    }
}
