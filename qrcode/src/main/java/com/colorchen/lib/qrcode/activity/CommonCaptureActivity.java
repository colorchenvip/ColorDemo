package com.colorchen.lib.qrcode.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.SurfaceView;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.colorchen.lib.qrcode.R;
import com.colorchen.lib.qrcode.view.HintViewFinderView;
import com.colorchen.qbase.event.ScanQrcodeResultEvent;
import com.google.zxing.Result;
import org.greenrobot.eventbus.EventBus;

import static com.colorchen.lib.qrcode.view.HintViewFinderView.getDisplayWidth;

/**
 * Created by wangsye on 2017-11-14.
 */
public class CommonCaptureActivity extends SimpleCaptureActivity implements View.OnClickListener {
    public static final String MANUAL_INPUT_EXTRA = "manual_input";
    public static final String SCAN_HINT_EXTRA = "scan_hint_extra";
    public static final String TITLE_EXTRA = "title_extra";
    public static final String INPUT_HINT_EXTRA = "input_hint_extra";

    private HintViewFinderView hintViewFinderView;
    private ImageView backButton;
    private ImageView flashButton;
    private String title;
    private String hint;
    private String inputHint;

    private EditText inputEditText;
    private TextView titleTextView2;
    private View manualInputContainer;
    private ImageView inputFlashImageView;
    private boolean flashLightOpen = false;
    private FrameLayout inputToolbar;
    private Button inputOkButton;
    private FrameLayout barcodeHintContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.scan_activity);
        hintViewFinderView = (HintViewFinderView) findViewById(R.id.vfvCameraScan);
        hintViewFinderView.setHint("将二维码/条码放置于框内，即开始扫描");
        init(this, (SurfaceView) findViewById(R.id.svCameraScan), hintViewFinderView);

        inputToolbar = (FrameLayout) findViewById(R.id.inputToolbar);
        inputOkButton = (Button) findViewById(R.id.inputOkButton);
        barcodeHintContainer = (FrameLayout) findViewById(R.id.barcodeHintContainer);

        TypedValue typedValue = new TypedValue();
        getTheme().resolveAttribute(android.R.attr.colorPrimary, typedValue, true);
        int color = typedValue.data;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            inputToolbar.setBackgroundColor(color);
            barcodeHintContainer.setBackgroundColor(color);
        }

//        GradientDrawable gradientDrawable = (GradientDrawable) inputOkButton.getBackground();
//        gradientDrawable.setColor(color);

        inputEditText = (EditText) findViewById(R.id.inputEditText);
        titleTextView2 = (TextView) findViewById(R.id.titleTextView2);
        manualInputContainer = findViewById(R.id.manualInputContainer);
        inputFlashImageView = (ImageView) findViewById(R.id.inputFlashImageView);
        backButton = (ImageView) findViewById(R.id.back_ibtn);
        flashButton = (ImageView) findViewById(R.id.flashButton);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ScanQrcodeResultEvent event = new ScanQrcodeResultEvent(ScanQrcodeResultEvent.Status.fail, 2, "");
                finish();
            }
        });
        flashButton.setClickable(true);
        flashButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchLight(!flashLightOpen);
                toggleFlashLight();
            }
        });

        Bundle data = getIntent().getExtras();
        int showInputButton = 0;
        if (data != null) {
            showInputButton = data.getInt(MANUAL_INPUT_EXTRA);
            title = data.getString(TITLE_EXTRA);
            hint = data.getString(SCAN_HINT_EXTRA);
            inputHint = data.getString(INPUT_HINT_EXTRA);
            if (!TextUtils.isEmpty(hint)) {
                hintViewFinderView.setHint(hint);
            }
            if (!TextUtils.isEmpty(title)) {
                ((TextView) findViewById(R.id.titleTextView)).setText(title);
            } else {
                ((TextView) findViewById(R.id.titleTextView)).setText("扫一扫");
            }
            if (!TextUtils.isEmpty(inputHint)) {
                ((TextView) findViewById(R.id.inputEditText)).setHint(inputHint);
                ((TextView) findViewById(R.id.inputHintTextView)).setText(inputHint);
            }
        } else {
            ((TextView) findViewById(R.id.titleTextView)).setText("扫一扫");
            titleTextView2.setText("手动输入");
        }
        final ImageView manualInputButton = (ImageView) findViewById(R.id.manualInputButton);
        manualInputButton.setClickable(true);
        if (showInputButton == 1) {
            findViewById(R.id.manualInputButtonContainer).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.manualInputButtonContainer).setVisibility(View.GONE);
        }
        manualInputButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                manualInputContainer.setVisibility(View.VISIBLE);
                manualInputContainer.animate()
                        .translationX(0)
                        .setDuration(400)
                        .setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                super.onAnimationEnd(animation);
                                manualInputContainer.clearAnimation();
                            }
                        });
            }
        });
        manualInputContainer.postDelayed(new Runnable() {
            @Override
            public void run() {
                int screenWidth = getDisplayWidth(CommonCaptureActivity.this);
                manualInputContainer.animate()
                        .translationX(screenWidth)
                        .setDuration(400)
                        .setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                super.onAnimationEnd(animation);
                                manualInputContainer.setVisibility(View.GONE);
                                manualInputContainer.clearAnimation();
                            }
                        });
            }
        }, 500);
    }

    @Override
    public void handleResult(Result result) {
        ScanQrcodeResultEvent event = new ScanQrcodeResultEvent(ScanQrcodeResultEvent.Status.Success, 1, result.getText().trim());
        EventBus.getDefault().post(event);
        Intent resultIntent = new Intent();
        Bundle bundle = new Bundle();
        bundle.putString("result", result.getText().trim());
        bundle.putInt("scanType", 1);
        resultIntent.putExtras(bundle);
        setResult(RESULT_OK, resultIntent);
        finish();
    }

    @Override
    public void onClick(View v) {
    }

    public void toggleFlashLight() {
        if (flashLightOpen) {
            flashButton.setImageResource(R.drawable.icon_flashlight_close);
        } else {
            flashButton.setImageResource(R.drawable.icon_flashlight_open);
        }
        if (flashLightOpen) {
            inputFlashImageView.setImageResource(R.drawable.icon_input_flashlight_line_closed);
        } else {
            inputFlashImageView.setImageResource(R.drawable.icon_input_flashlight_line_open);
        }
        if (flashLightOpen) {
            setFlashLightOpen(false);
        } else {
            setFlashLightOpen(true);
        }
    }

    @Override
    public void setFlashLightOpen(boolean open) {
        if (flashLightOpen == open) return;
        flashLightOpen = !flashLightOpen;
    }

    public void onBackButtonClicked(View view) {
        hideManualInputContainer();
    }

    public void onClearButtonClicked(View view) {
        inputEditText.setText("");
    }

    public void onOkButtonClicked(View view) {
        String text = inputEditText.getText().toString();
        if (TextUtils.isEmpty(text.trim())) {
            Toast.makeText(this, "手动输入的信息为空", Toast.LENGTH_SHORT).show();
            return;
        }
        if (getCurrentFocus() != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
        handleManualInputText(text.trim());
    }

    public void handleManualInputText(String text) {
        ScanQrcodeResultEvent event = new ScanQrcodeResultEvent(ScanQrcodeResultEvent.Status.Success, 2, text);
        EventBus.getDefault().post(event);
        Intent resultIntent = new Intent();
        Bundle bundle = new Bundle();
        bundle.putString("result", text.trim());
        bundle.putInt("scanType", 2);
        resultIntent.putExtras(bundle);
        setResult(RESULT_OK, resultIntent);
        finish();
    }

    private void hideManualInputContainer() {
        manualInputContainer.animate()
                .translationX(manualInputContainer.getWidth())
                .setDuration(400)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        manualInputContainer.setVisibility(View.GONE);
                        manualInputContainer.clearAnimation();
                    }
                });
    }

    public void onScanButtonClicked(View view) {
        hideManualInputContainer();
    }

    public void onFlashLightButtonClicked(View view) {
        switchLight(!flashLightOpen);
        toggleFlashLight();
    }

    @Override
    public void onBackPressed() {
        if (manualInputContainer.getVisibility() == View.VISIBLE) {
            hideManualInputContainer();
        } else {
            ScanQrcodeResultEvent event = new ScanQrcodeResultEvent(ScanQrcodeResultEvent.Status.fail, 2, "");
            EventBus.getDefault().post(event);
            super.onBackPressed();
        }
    }
}
