package com.colorchen.qbase.widget.mutilstatelayout;

/**
 * @author :  zhangbol
 *         e-mail : zhangbol@yutong.com
 *         time   : 2017-12-14
 *         desc   : 描述信息
 *         version: 1.0
 */
import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.AnimRes;
import android.support.annotation.StringRes;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.colorchen.qbase.R;


/**
 * Android layout to show most common state templates like loading, empty, error etc. To do that all you need to is
 * wrap the target area(view) with StatefulLayout. For more information about usage look
 * <a href="https://github.com/gturedi/StatefulLayout#usage">here</a>
 */
public class MultiStateLayout extends LinearLayout {

    private static final String MSG_ONE_CHILD = "MutilStateLayout must have one child!";
    private static final boolean DEFAULT_ANIM_ENABLED = false;
    private static final int DEFAULT_IN_ANIM = android.R.anim.fade_in;
    private static final int DEFAULT_OUT_ANIM = android.R.anim.fade_out;

    /**
     * Indicates whether to place the animation on state changes
     */
    private boolean animationEnabled;
    /**
     * Animation started begin of state change
     */
    private Animation inAnimation;
    /**
     * Animation started end of state change
     */
    private Animation outAnimation;
    /**
     * to synchronize transition animations when animation duration shorter then request of state change
     */
    private int animCounter;

    private View content;
    private LinearLayout stContainer;
    private ProgressBar stProgress;
    private ImageView stImage;
    private TextView stMessage;
    private Button stButton;

    public MultiStateLayout(Context context) {
        this(context, null);
    }

    public MultiStateLayout(Context context, AttributeSet attrs) {
        super(context, attrs, 0);
        TypedArray array = context.getTheme().obtainStyledAttributes(attrs, R.styleable.MultiStateLayout, 0, 0);
        animationEnabled = array.getBoolean(R.styleable.MultiStateLayout_mslAnimationEnabled, DEFAULT_ANIM_ENABLED);
        inAnimation = anim(array.getResourceId(R.styleable.MultiStateLayout_mslInAnimation, DEFAULT_IN_ANIM));
        outAnimation = anim(array.getResourceId(R.styleable.MultiStateLayout_mslOutAnimation, DEFAULT_OUT_ANIM));
        array.recycle();
    }

    public boolean isAnimationEnabled() {
        return animationEnabled;
    }

    public void setAnimationEnabled(boolean animationEnabled) {
        this.animationEnabled = animationEnabled;
    }

    public Animation getInAnimation() {
        return inAnimation;
    }

    public void setInAnimation(Animation animation) {
        inAnimation = animation;
    }

    public void setInAnimation(@AnimRes int anim) {
        inAnimation = anim(anim);
    }

    public Animation getOutAnimation() {
        return outAnimation;
    }

    public void setOutAnimation(Animation animation) {
        outAnimation = animation;
    }

    public void setOutAnimation(@AnimRes int anim) {
        outAnimation = anim(anim);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        if (getChildCount() > 1) {
            throw new IllegalStateException(MSG_ONE_CHILD);
        }
        if (isInEditMode()) {
            return; // hide state views in designer
        }
        setOrientation(VERTICAL);
        content = getChildAt(0); // assume first child as content
        LayoutInflater.from(getContext()).inflate(R.layout.widget_msl_template, this, true);
        stContainer = (LinearLayout) findViewById(R.id.stContainer);
        stProgress = (ProgressBar) findViewById(R.id.stProgress);
        stImage = (ImageView) findViewById(R.id.stImage);
        stMessage = (TextView) findViewById(R.id.stMessage);
        stButton = (Button) findViewById(R.id.stButton);
    }

    // content //

    public void showContent() {
        if (isAnimationEnabled()) {
            stContainer.clearAnimation();
            content.clearAnimation();
            final int animCounterCopy = ++animCounter;
            if (stContainer.getVisibility() == VISIBLE) {
                outAnimation.setAnimationListener(new CustomAnimationListener() {
                    @Override
                    public void onAnimationEnd(Animation animation) {
                        if (animCounter != animCounterCopy) {
                            return;
                        }
                        stContainer.setVisibility(GONE);
                        content.setVisibility(VISIBLE);
                        content.startAnimation(inAnimation);
                    }
                });
                stContainer.startAnimation(outAnimation);
            }
        } else {
            stContainer.setVisibility(GONE);
            content.setVisibility(VISIBLE);
        }
    }

    // loading //

    public void showLoading() {
        showLoading(R.string.widget_msl_loading);
    }

    public void showLoading(@StringRes int resId) {
        showLoading(str(resId));
    }

    public void showLoading(String message) {
        showCustom(new CustomStateOptions()
                .message(message)
                .loading());
    }

    // empty //

    public void showEmpty() {
        showEmpty(R.string.widget_msl_empty);
    }

    public void showEmpty(@StringRes int resId) {
        showEmpty(str(resId));
    }

    public void showEmpty(String message) {
        showCustom(new CustomStateOptions()
                .message(message)
                .image(R.drawable.ic_launcher_foreground));
    }

    // error //

    public void showError(OnClickListener clickListener) {
        showError(R.string.widget_msl_error, clickListener);
    }

    public void showError(@StringRes int resId, OnClickListener clickListener) {
        showError(str(resId), clickListener);
    }

    public void showError(String message, OnClickListener clickListener) {
        showCustom(new CustomStateOptions()
                .message(message)
                .image(R.drawable.ic_launcher_foreground)
                .buttonText(str(R.string.widget_msl_retry))
                .buttonClickListener(clickListener));
    }

    // offline

    public void showOffline(OnClickListener clickListener) {
        showOffline(R.string.widget_msl_offline, clickListener);
    }

    public void showOffline(@StringRes int resId, OnClickListener clickListener) {
        showOffline(str(resId), clickListener);
    }

    public void showOffline(String message, OnClickListener clickListener) {
        showCustom(new CustomStateOptions()
                .message(message)
                .image(R.drawable.ic_launcher_foreground)
                .buttonText(str(R.string.widget_msl_retry))
                .buttonClickListener(clickListener));
    }


    // custom //

    /**
     * Shows custom state for given options. If you do not set buttonClickListener, the button will not be shown
     *
     * @param options customization options
     * @see CustomStateOptions
     */
    public void showCustom(final CustomStateOptions options) {
        if (isAnimationEnabled()) {
            stContainer.clearAnimation();
            content.clearAnimation();
            final int animCounterCopy = ++animCounter;
            if (stContainer.getVisibility() == GONE) {
                outAnimation.setAnimationListener(new CustomAnimationListener() {
                    @Override
                    public void onAnimationEnd(Animation animation) {
                        if (animCounterCopy != animCounter) {
                            return;
                        }
                        content.setVisibility(GONE);
                        stContainer.setVisibility(VISIBLE);
                        stContainer.startAnimation(inAnimation);
                    }
                });
                content.startAnimation(outAnimation);
                state(options);
            } else {
                outAnimation.setAnimationListener(new CustomAnimationListener() {
                    @Override
                    public void onAnimationEnd(Animation animation) {
                        if (animCounterCopy != animCounter){
                            return;
                        }
                        state(options);
                        stContainer.startAnimation(inAnimation);
                    }
                });
                stContainer.startAnimation(outAnimation);
            }
        } else {
            content.setVisibility(GONE);
            stContainer.setVisibility(VISIBLE);
            state(options);
        }
    }

    // helper methods //

    private void state(CustomStateOptions options) {
        if (!TextUtils.isEmpty(options.getMessage())) {
            stMessage.setVisibility(VISIBLE);
            stMessage.setText(options.getMessage());
        } else {
            stMessage.setVisibility(GONE);
        }

        if (options.isLoading()) {
            stProgress.setVisibility(VISIBLE);
            stImage.setVisibility(GONE);
            stButton.setVisibility(GONE);
        } else {
            stProgress.setVisibility(GONE);
            if (options.getImageRes() != 0) {
                stImage.setVisibility(VISIBLE);
                stImage.setImageResource(options.getImageRes());
            } else {
                stImage.setVisibility(GONE);
            }

            if (options.getClickListener() != null) {
                stButton.setVisibility(VISIBLE);
                stButton.setOnClickListener(options.getClickListener());
                if (!TextUtils.isEmpty(options.getButtonText())) {
                    stButton.setText(options.getButtonText());
                }
            } else {
                stButton.setVisibility(GONE);
            }
        }
    }

    private String str(@StringRes int resId) {
        return getContext().getString(resId);
    }

    private Animation anim(@AnimRes int resId) {
        return AnimationUtils.loadAnimation(getContext(), resId);
    }

}