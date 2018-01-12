package com.colorchen.lib.qrcode.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.colorchen.lib.qrcode.R;
import com.colorchen.lib.qrcode.view.HintViewFinderView;
import com.google.zxing.Result;
/**
 * name：SimpleCaptureActivity
 * @author: ChenQ
 * @date: 2018-1-5
 */
public class SimpleCaptureActivity extends AbstractCaptureActivity {

    private HintViewFinderView hintViewFinderView;
    private ImageView backIbtn;
    private ImageView flashButton;
    private boolean flashLightOpen = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.simple_capture);
        hintViewFinderView = (HintViewFinderView) findViewById(R.id.vfvCameraScan);
        hintViewFinderView.setHint("将二维码/条码放置于框内，即开始扫描");
        init(this, (SurfaceView) findViewById(R.id.svCameraScan), hintViewFinderView);

        backIbtn = (ImageView) findViewById(R.id.back_ibtn);
        flashButton = (ImageView) findViewById(R.id.flashButton);

        backIbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
        ((TextView) findViewById(R.id.titleTextView)).setText("扫一扫");
    }

    @Override
    public void handleResult(Result result) {
        Intent resultIntent = new Intent();
        Bundle bundle = new Bundle();
        bundle.putString("result", result.getText().trim());
        resultIntent.putExtras(bundle);
        setResult(RESULT_OK, resultIntent);
        finish();
    }

    public void toggleFlashLight() {
        if (flashLightOpen) {
            flashButton.setImageResource(R.drawable.icon_flashlight_close);
        } else {
            flashButton.setImageResource(R.drawable.icon_flashlight_open);
        }

        if (flashLightOpen) {
            setFlashLightOpen(false);
        } else {
            setFlashLightOpen(true);
        }
    }

    public void setFlashLightOpen(boolean open) {
        if (flashLightOpen == open) return;
        flashLightOpen = !flashLightOpen;
    }

}
