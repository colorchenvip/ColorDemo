package com.colorchen.lib.qrcode.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Display;
import android.view.WindowManager;


import com.colorchen.lib.qrcode.camera.CameraManager;

import java.lang.reflect.Method;

/**
 * Created by wangsye on 2017-11-7.
 */

public class HintViewFinderView extends ViewfinderView {
    public static final int TRADE_MARK_TEXT_SIZE_SP = 15;
    public final Paint PAINT = new Paint();
    String hint;

    public HintViewFinderView(Context context, String hint) {
        super(context);
        this.hint = hint;
        init();
    }

    public HintViewFinderView(Context context) {
        super(context);
        init();
    }

    public void setHint(String hint) {
        this.hint = hint;
        invalidate();
    }

    public HintViewFinderView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        PAINT.setColor(Color.WHITE);
        PAINT.setAntiAlias(true);
        float textPixelSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,
                TRADE_MARK_TEXT_SIZE_SP, getResources().getDisplayMetrics());
        PAINT.setTextSize(textPixelSize);
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawTradeMark(canvas);
    }

    private void drawTradeMark(Canvas canvas) {
        Rect framingRect = CameraManager.get().getFramingRect();
        float tradeMarkTop;
        float tradeMarkLeft;
        float fontSize = PAINT.getTextSize();
        if (framingRect != null) {
            tradeMarkTop = framingRect.bottom + PAINT.getTextSize() + 10;
        } else {
            tradeMarkTop = 10;
        }
        int screenWidth = getDisplayWidth(getContext());
        tradeMarkLeft = (float) (screenWidth / 2 - hint.length() * fontSize / 2);
        canvas.drawText(hint, tradeMarkLeft, tradeMarkTop, PAINT);
    }


    public static int getDisplayWidth(Context context) {
        if (context == null) {
            return 0;
        }
        int width = 0;
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        try {
            Class<?> cls = Display.class;
            Class<?>[] parameterTypes = {
                    Point.class
            };
            Point parameter = new Point();
            Method method = cls.getMethod("getSize", parameterTypes);
            method.invoke(display, parameter);
            width = parameter.x;
        } catch (Exception e) {
            width = display.getWidth();
        }
        return width;
    }
}
