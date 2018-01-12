package com.colorchen.lib.qjsbridge.bridge;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Keep;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.ConsoleMessage;
import android.webkit.CookieManager;
import android.webkit.GeolocationPermissions;
import android.webkit.JavascriptInterface;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.PermissionRequest;
import android.webkit.SslErrorHandler;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebStorage;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.FrameLayout;

import com.colorchen.lib.qmap.callback.PlayPathListener;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;


public class YTWebView extends WebView {
    private static final String BRIDGE_NAME = "_dsbridge";
    private Object jsb;
    private String APP_CACAHE_DIRNAME;
    int callID = 0;
    Map<Integer, OnReturnValue> handlerMap = new HashMap<>();
    public Activity activity;
    public WebViewEventListener webViewEventListener;

    public YTWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public YTWebView(Context context) {
        super(context);
        init();
    }

    public void openDebug() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true);
        }
    }

    public Activity getActivity() {
        return activity;
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    public void setWebViewEventListener(WebViewEventListener webViewEventListener) {
        this.webViewEventListener = webViewEventListener;
    }

    @Keep
    void init() {
        openDebug();
        APP_CACAHE_DIRNAME = getContext().getFilesDir().getAbsolutePath() + "/webcache";
        WebSettings settings = getSettings();
        settings.setDomStorageEnabled(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            CookieManager.getInstance().setAcceptThirdPartyCookies(this, true);
        }
        settings.setAllowFileAccess(true);
        settings.setAppCacheEnabled(true);
        settings.setSavePassword(false);
        settings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        settings.setJavaScriptEnabled(true);
        settings.setLoadWithOverviewMode(true);
        settings.setSupportMultipleWindows(true);
        settings.setAppCachePath(APP_CACAHE_DIRNAME);
        settings.setJavaScriptEnabled(true);
        settings.setAllowFileAccessFromFileURLs(true);
        settings.setUseWideViewPort(true);
        if (Build.VERSION.SDK_INT >= 21) {
            settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            settings.setAllowFileAccessFromFileURLs(true);
            settings.setAllowUniversalAccessFromFileURLs(true);
        }
        super.setWebChromeClient(mWebChromeClient);
        super.setWebViewClient(webViewClient);

        super.addJavascriptInterface(new Object() {
            @Keep
            @JavascriptInterface
            public String call(String methodName, String args) {
                String error = "Js bridge method called, but there is " +
                        "not a JavascriptInterface object, please set JavascriptInterface object first!";
                if (jsb == null) {
                    Log.e("SynWebView", error);
                    return "";
                }

                Class<?> cls = jsb.getClass();
                try {
                    Method method;
                    boolean asyn = false;
                    JSONObject arg = new JSONObject(args);
                    String callback = "";
                    try {
                        callback = arg.getString("_dscbstub");
                        arg.remove("_dscbstub");
                        method = getMethod(cls, methodName, new Class[]{JSONObject.class, CompletionHandler.class});
                        asyn = true;
                    } catch (Exception e) {
                        method = getMethod(cls, methodName, new Class[]{JSONObject.class});
                        if (method == null) {
                            throw new NoSuchMethodException(methodName);
                        }
                    }

                    if (method == null) {
                        error = "ERROR! \n Not find method \"" + methodName + "\" implementation! ";
                        Log.e("SynWebView", error);
                        evaluateJavascript(String.format("alert(decodeURIComponent(\"%s\"})", error));
                        return "";
                    }
                    JavascriptInterface annotation = method.getAnnotation(JavascriptInterface.class);
                    if (annotation != null) {
                        Object ret;
                        method.setAccessible(true);
                        if (asyn) {
                            final String cb = callback;
                            ret = method.invoke(jsb, arg, new CompletionHandler() {

                                @Override
                                public void complete(String retValue) {
                                    complete(retValue, true);
                                }

                                @Override
                                public void complete() {
                                    complete("", true);
                                }

                                @Override
                                public void setProgressData(String value) {
                                    complete(value, false);
                                }

                                private void complete(String retValue, boolean complete) {
                                    try {
                                        if (retValue == null) {
                                            retValue = "";
                                        }
                                        retValue = URLEncoder.encode(retValue, "UTF-8").replaceAll("\\+", "%20");
                                        String script = String.format("%s(decodeURIComponent(\"%s\"));", cb, retValue);
                                        if (complete) {
                                            script += "delete window." + cb;
                                        }
                                        evaluateJavascript(script);
                                    } catch (UnsupportedEncodingException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                        } else {
                            ret = method.invoke(jsb, arg);
                        }
                        if (ret == null) {
                            ret = "";
                        }
                        return ret.toString();
                    } else {
                        error = "Method " + methodName + " is not invoked, since  " +
                                "it is not declared with JavascriptInterface annotation! ";
                        evaluateJavascript(String.format("alert('ERROR \\n%s')", error));
                        Log.e("SynWebView", error);
                    }
                } catch (Exception e) {
                    evaluateJavascript(String.format("alert('alert! \\No such method:%s')", e.getMessage()));
                    e.printStackTrace();
                }
                return "";
            }

            @Keep
            @JavascriptInterface
            public void returnValue(int id, String value) {
                final OnReturnValue handler = handlerMap.get(id);
                final String returnValue = value;
                if (handler != null) {
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            handler.onValue(returnValue);
                        }
                    });
                    handlerMap.remove(id);
                }
            }

        }, BRIDGE_NAME);

    }

    private WebViewClient webViewClient = new WebViewClient() {
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            Log.d("YTWebView", "onPageStarted:" + url);
            if (webViewEventListener != null) {
                webViewEventListener.onUrlStartLoad(url);
            }
        }

        public boolean isParse(String url) {
            if (url.startsWith("sms:")) {
                int numberEnd = url.indexOf("?&");
                int numberStart = "sms:".length();
                String number = "";
                if (numberEnd > numberStart) {
                    number = url.substring(numberStart, numberEnd);
                }
                int bodyStart = url.indexOf("?&body=");
                bodyStart += "?&body=".length();
                String content = url.substring(bodyStart);
                try {
                    content = URLDecoder.decode(content, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                if (!TextUtils.isEmpty(content)) {
                    Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:" + number));
                    intent.putExtra("sms_body", content);
                    getContext().startActivity(intent);
                }
                return true;
            } else if (url.startsWith("tel:")) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                getContext().startActivity(intent);
                return true;
            } else if (url.startsWith("wuyeapp://viewlocation")) {
                webViewEventListener.onJumpActivity();
                return true;
            }
            else if (url.startsWith("wuyeapp://userlogout")) {
                webViewEventListener.onLogout();
                return true;
            }
            return false;
        }

        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            handler.proceed();
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (isParse(url)) {
                return true;
            }
            return false;
        }

        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            super.onReceivedError(view, errorCode, description, failingUrl);
            Log.d("failingUrl", failingUrl);
            view.clearCache(true);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            Log.d("YTWebView", "onPageFinished:" + url);
            if (webViewEventListener != null) {
                webViewEventListener.onUrlEndLoad(url);
            }
        }

        @TargetApi(21)
        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
            return shouldInterceptRequest(view, request.getUrl().toString());
        }

        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
            return super.shouldInterceptRequest(view, url);
        }
    };

    @Override
    public void setWebChromeClient(WebChromeClient client) {
        webChromeClient = client;
    }

    WebChromeClient webChromeClient;
    public ValueCallback<Uri> mUploadMessage;
    public ValueCallback<Uri[]> mUploadMessageForAndroid5;
    public final static int FILECHOOSER_RESULTCODE = 50001;
    public final static int FILECHOOSER_RESULTCODE_FOR_ANDROID_5 = 50002;

    private void openFileChooserImpl(ValueCallback<Uri> uploadMsg) {
        mUploadMessage = uploadMsg;
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("*/*");
        activity.startActivityForResult(Intent.createChooser(i, "选择文件"), FILECHOOSER_RESULTCODE);
    }

    private void openFileChooserImplForAndroid5(ValueCallback<Uri[]> uploadMsg) {
        mUploadMessageForAndroid5 = uploadMsg;
        Intent contentSelectionIntent = new Intent(Intent.ACTION_GET_CONTENT);
        contentSelectionIntent.addCategory(Intent.CATEGORY_OPENABLE);
        contentSelectionIntent.setType("*/*");

        Intent chooserIntent = new Intent(Intent.ACTION_CHOOSER);
        chooserIntent.putExtra(Intent.EXTRA_INTENT, contentSelectionIntent);
        chooserIntent.putExtra(Intent.EXTRA_TITLE, "选择文件");

        activity.startActivityForResult(chooserIntent, FILECHOOSER_RESULTCODE_FOR_ANDROID_5);
    }


    private WebChromeClient mWebChromeClient = new WebChromeClient() {

        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            Log.d("chromium", "onProgressChanged");
            injectJs();
            if (webChromeClient != null) {
                webChromeClient.onProgressChanged(view, newProgress);
            } else {
                super.onProgressChanged(view, newProgress);
            }

            if (webViewEventListener != null) {
                webViewEventListener.onUrlLoadProgress(view.getOriginalUrl(), view.getUrl(), newProgress);
            }
        }

        @Override
        public void onReceivedTitle(WebView view, String title) {
            Log.d("chromium", "onReceivedTitle");
            injectJs();
            if (webChromeClient != null) {
                webChromeClient.onReceivedTitle(view, title);
            } else {
                super.onReceivedTitle(view, title);
            }
            webViewEventListener.onReceiveTitle(view.getUrl(), title);
        }

        @Override
        public void onReceivedIcon(WebView view, Bitmap icon) {
            if (webChromeClient != null) {
                webChromeClient.onReceivedIcon(view, icon);
            } else {
                super.onReceivedIcon(view, icon);
            }
        }

        @Override
        public void onReceivedTouchIconUrl(WebView view, String url, boolean precomposed) {
            if (webChromeClient != null) {
                webChromeClient.onReceivedTouchIconUrl(view, url, precomposed);
            } else {
                super.onReceivedTouchIconUrl(view, url, precomposed);
            }
        }

        @Override
        public void onShowCustomView(View view, CustomViewCallback callback) {
            if (webChromeClient != null) {
                webChromeClient.onShowCustomView(view, callback);
            } else {
                super.onShowCustomView(view, callback);
            }
        }

        @Override
        public void onShowCustomView(View view, int requestedOrientation,
                                     CustomViewCallback callback) {
            if (webChromeClient != null) {
                webChromeClient.onShowCustomView(view, requestedOrientation, callback);
            } else {
                super.onShowCustomView(view, requestedOrientation, callback);
            }
        }

        @Override
        public void onHideCustomView() {
            if (webChromeClient != null) {
                webChromeClient.onHideCustomView();
            } else {
                super.onHideCustomView();
            }
        }

        @Override
        public boolean onCreateWindow(WebView view, boolean isDialog,
                                      boolean isUserGesture, Message resultMsg) {
            if (webChromeClient != null) {
                return webChromeClient.onCreateWindow(view, isDialog,
                        isUserGesture, resultMsg);
            }
            return super.onCreateWindow(view, isDialog, isUserGesture, resultMsg);
        }

        @Override
        public void onRequestFocus(WebView view) {
            if (webChromeClient != null) {
                webChromeClient.onRequestFocus(view);
            } else {
                super.onRequestFocus(view);
            }
        }

        @Override
        public void onCloseWindow(WebView window) {
            if (webChromeClient != null) {
                webChromeClient.onCloseWindow(window);
            } else {
                super.onCloseWindow(window);
            }
        }

        @Override
        public boolean onJsAlert(WebView view, String url, final String message, final JsResult result) {
            if (webChromeClient != null) {
                if (webChromeClient.onJsAlert(view, url, message, result)) {
                    return true;
                }
            }
            Dialog alertDialog = new AlertDialog.Builder(getContext()).
                    setTitle("提示").
                    setMessage(message).
                    setCancelable(false).
                    setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            result.confirm();
                        }
                    })
                    .create();
            alertDialog.show();
            return true;
        }

        @Override
        public boolean onJsConfirm(WebView view, String url, String message,
                                   final JsResult result) {
            if (webChromeClient != null && webChromeClient.onJsConfirm(view, url, message, result)) {
                return true;
            } else {
                DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == Dialog.BUTTON_POSITIVE) {
                            result.confirm();
                        } else {
                            result.cancel();
                        }
                    }
                };
                new AlertDialog.Builder(getContext()).setTitle("提示")
                        .setMessage(message)
                        .setCancelable(false)
                        .setPositiveButton("确定", listener)
                        .setNegativeButton("取消", listener).show();
                return true;

            }

        }

        @Override
        public boolean onJsPrompt(WebView view, String url, final String message,
                                  String defaultValue, final JsPromptResult result) {
            super.onJsPrompt(view, url, message, defaultValue, result);
            if (webChromeClient != null && webChromeClient.onJsPrompt(view, url, message, defaultValue, result)) {
                return true;
            } else {
                final EditText editText = new EditText(getContext());
                editText.setText(defaultValue);
                if (defaultValue != null) {
                    editText.setSelection(defaultValue.length());
                }
                float dpi = getContext().getResources().getDisplayMetrics().density;
                DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == Dialog.BUTTON_POSITIVE) {
                            result.confirm(editText.getText().toString());
                        } else {
                            result.cancel();
                        }
                    }
                };
                new AlertDialog.Builder(getContext())
                        .setTitle(message)
                        .setView(editText)
                        .setCancelable(false)
                        .setPositiveButton("确定", listener)
                        .setNegativeButton("取消", listener)
                        .show();
                FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
                int t = (int) (dpi * 16);
                layoutParams.setMargins(t, 0, t, 0);
                layoutParams.gravity = Gravity.CENTER_HORIZONTAL;
                editText.setLayoutParams(layoutParams);
                int padding = (int) (15 * dpi);
                editText.setPadding(padding - (int) (5 * dpi), padding, padding, padding);
                return true;
            }

        }

        @Override
        public boolean onJsBeforeUnload(WebView view, String url, String message, JsResult result) {
            if (webChromeClient != null) {
                return webChromeClient.onJsBeforeUnload(view, url, message, result);
            }
            return super.onJsBeforeUnload(view, url, message, result);
        }

        @Override
        public void onExceededDatabaseQuota(String url, String databaseIdentifier, long quota,
                                            long estimatedDatabaseSize,
                                            long totalQuota,
                                            WebStorage.QuotaUpdater quotaUpdater) {
            if (webChromeClient != null) {
                webChromeClient.onExceededDatabaseQuota(url, databaseIdentifier, quota,
                        estimatedDatabaseSize, totalQuota, quotaUpdater);
            } else {
                super.onExceededDatabaseQuota(url, databaseIdentifier, quota,
                        estimatedDatabaseSize, totalQuota, quotaUpdater);
            }
        }

        @Override
        public void onReachedMaxAppCacheSize(long requiredStorage, long quota, WebStorage.QuotaUpdater quotaUpdater) {
            if (webChromeClient != null) {
                webChromeClient.onReachedMaxAppCacheSize(requiredStorage, quota, quotaUpdater);
            }
            super.onReachedMaxAppCacheSize(requiredStorage, quota, quotaUpdater);
        }

        @Override
        public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
            if (webChromeClient != null) {
                webChromeClient.onGeolocationPermissionsShowPrompt(origin, callback);
            } else {
                super.onGeolocationPermissionsShowPrompt(origin, callback);
            }
        }

        @Override
        public void onGeolocationPermissionsHidePrompt() {
            if (webChromeClient != null) {
                webChromeClient.onGeolocationPermissionsHidePrompt();
            } else {
                super.onGeolocationPermissionsHidePrompt();
            }
        }


        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void onPermissionRequest(PermissionRequest request) {
            if (webChromeClient != null) {
                webChromeClient.onPermissionRequest(request);
            } else {
                super.onPermissionRequest(request);
            }
        }

        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void onPermissionRequestCanceled(PermissionRequest request) {
            if (webChromeClient != null) {
                webChromeClient.onPermissionRequestCanceled(request);
            } else {
                super.onPermissionRequestCanceled(request);
            }
        }

        @Override
        public boolean onJsTimeout() {
            if (webChromeClient != null) {
                return webChromeClient.onJsTimeout();
            }
            return super.onJsTimeout();
        }

        @Override
        public void onConsoleMessage(String message, int lineNumber, String sourceID) {
            if (webChromeClient != null) {
                webChromeClient.onConsoleMessage(message, lineNumber, sourceID);
            } else {
                super.onConsoleMessage(message, lineNumber, sourceID);
            }
        }

        @Override
        public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
            if (webChromeClient != null) {
                return webChromeClient.onConsoleMessage(consoleMessage);
            }
            return super.onConsoleMessage(consoleMessage);
        }

        @Override
        public Bitmap getDefaultVideoPoster() {
            if (webChromeClient != null) {
                return webChromeClient.getDefaultVideoPoster();
            }
            return super.getDefaultVideoPoster();
        }

        @Override
        public View getVideoLoadingProgressView() {
            if (webChromeClient != null) {
                return webChromeClient.getVideoLoadingProgressView();
            }
            return super.getVideoLoadingProgressView();
        }

        @Override
        public void getVisitedHistory(ValueCallback<String[]> callback) {
            if (webChromeClient != null) {
                webChromeClient.getVisitedHistory(callback);
            } else {
                super.getVisitedHistory(callback);
            }
        }

        //3.0++版本
        public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType) {
            openFileChooserImpl(uploadMsg);
        }

        //3.0--版本
        public void openFileChooser(ValueCallback<Uri> uploadMsg) {
            openFileChooserImpl(uploadMsg);
        }

        public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture) {
            openFileChooserImpl(uploadMsg);
        }

        @Override
        public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
            openFileChooserImplForAndroid5(filePathCallback);
            return true;
        }

        private void injectJs() {
            evaluateJavascript("function getJsBridge(){window._dsf=window._dsf||{};return{call:function(b,a,c){\"function\"==typeof a&&(c=a,a={});if(\"function\"==typeof c){window.dscb=window.dscb||0;var d=\"dscb\"+window.dscb++;window[d]=c;a._dscbstub=d}a=JSON.stringify(a||{});return window._dswk?prompt(window._dswk+b,a):\"function\"==typeof _dsbridge?_dsbridge(b,a):_dsbridge.call(b,a)},register:function(b,a){\"object\"==typeof b?Object.assign(window._dsf,b):window._dsf[b]=a}}}dsBridge=getJsBridge();setTimeout(function(){dsBridge.register('processBackPressedAndroid',window.callBackProcess?callBackProcess:function(){});},0)");
        }
    };

    private void _evaluateJavascript(String script) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            evaluateJavascript(script, null);
        } else {
            loadUrl("javascript:" + script);
        }
    }

    //如果当前在主线程，不要直接调用post,这可能会延迟js执行
    public void evaluateJavascript(final String script) {
        if (Looper.getMainLooper() == Looper.myLooper()) {
            _evaluateJavascript(script);
        } else {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    _evaluateJavascript(script);
                }
            });
        }
    }

    @Override
    public void clearCache(boolean includeDiskFiles) {
        super.clearCache(includeDiskFiles);
        CookieManager.getInstance().removeAllCookie();
        Context context = getContext();
        //清理Webview缓存数据库
        try {
            context.deleteDatabase("webview.db");
            context.deleteDatabase("webviewCache.db");
        } catch (Exception e) {
            e.printStackTrace();
        }

        //WebView 缓存文件
        File appCacheDir = new File(APP_CACAHE_DIRNAME);
        File webviewCacheDir = new File(context.getCacheDir()
                .getAbsolutePath() + "/webviewCache");

        //删除webview 缓存目录
        if (webviewCacheDir.exists()) {
            deleteFile(webviewCacheDir);
        }
        //删除webview 缓存 缓存目录
        if (appCacheDir.exists()) {
            deleteFile(appCacheDir);
        }
    }

    public void deleteFile(File file) {
        if (file.exists()) {
            if (file.isFile()) {
                file.delete();
            } else if (file.isDirectory()) {
                File files[] = file.listFiles();
                for (int i = 0; i < files.length; i++) {
                    deleteFile(files[i]);
                }
            }
            file.delete();
        } else {
            Log.e("Webview", "delete file no exists " + file.getAbsolutePath());
        }
    }

    @Override
    public void loadUrl(final String url) {
        post(new Runnable() {
            @Override
            public void run() {
                YTWebView.super.loadUrl(url);
            }
        });
    }

    public void callHandler(String method, Object[] args) {
        callHandler(method, args, null);
    }

    public void callHandler(String method, Object[] args, final OnReturnValue handler) {
        if (args == null) {
            args = new Object[0];
        }
        String arg = new JSONArray(Arrays.asList(args)).toString();
        String script = String.format("(window._dsf.%s||window.%s).apply(window._dsf||window,%s)", method, method, arg);
        if (handler != null) {
            script = String.format("%s.returnValue(%d,%s)", BRIDGE_NAME, callID, script);
            handlerMap.put(callID++, handler);
        }
        evaluateJavascript(script);
    }

    @Override
    public void loadUrl(final String url, final Map<String, String> additionalHttpHeaders) {
        post(new Runnable() {
            @Override
            public void run() {
                YTWebView.super.loadUrl(url, additionalHttpHeaders);
            }
        });
    }

    public void setJavascriptInterface(Object object) {
        jsb = object;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == FILECHOOSER_RESULTCODE) {
            if (null == mUploadMessage) {
                return;
            }
            Uri result = data == null || resultCode != Activity.RESULT_OK ? null : data.getData();
            mUploadMessage.onReceiveValue(result);
            mUploadMessage = null;
        } else if (requestCode == FILECHOOSER_RESULTCODE_FOR_ANDROID_5) {
            if (null == mUploadMessageForAndroid5) {
                return;
            }
            Uri result = (data == null || resultCode != Activity.RESULT_OK) ? null : data.getData();
            if (result != null) {
                mUploadMessageForAndroid5.onReceiveValue(new Uri[]{result});
            } else {
                mUploadMessageForAndroid5.onReceiveValue(new Uri[]{});
            }
            mUploadMessageForAndroid5 = null;
        }
    }

    public static Method getMethod(Class clazz, String methodName, final Class[] classes) throws Exception {
        Method method = null;
        try {
            method = clazz.getDeclaredMethod(methodName, classes);
        } catch (NoSuchMethodException e) {
            try {
                method = clazz.getMethod(methodName, classes);
            } catch (NoSuchMethodException ex) {
                if (clazz.getSuperclass() == null) {
                    return method;
                } else {
                    method = getMethod(clazz.getSuperclass(), methodName, classes);
                }
            }
        }
        return method;
    }

    public interface WebViewEventListener {
        void onUrlLoadProgress(String originUrl, String url, int progress);

        void onUrlStartLoad(String url);

        void onUrlEndLoad(String url);

        void onReceiveTitle(String url, String title);

        void onJumpActivity();

        void onLogout();
    }
}
