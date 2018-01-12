package com.colorchen.qbase.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.annotation.DrawableRes;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutCompat;
import android.support.v7.widget.TintTypedArray;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.colorchen.qbase.R;

import static android.support.v7.appcompat.R.styleable;
import static com.colorchen.qbase.utils.DensityUtil.dp2px;

/**
 * 通用title bar 组件
 *
 * @author ChenQ
 * @time 2017/8/18 12:36
 * @email：colorchenvip@163.com
 */
public class TitleToolbar extends FrameLayout implements View.OnClickListener {

    private LinearLayoutCompat mTitleLayout;
    private TextView mTitleTextView;
    private CharSequence mTitleText;
    private boolean mTitleVisible;

    private TextView mSubtitleTextView;
    private CharSequence mSubTitleText;
    private boolean mSubTitleVisible;

    private TextView mCloseTextView;
    private CharSequence mCloseText;
    private boolean mCloseVisible;

    private TextView mBackTextView;
    private CharSequence mBackText;
    private boolean mBackVisible;

    /**
     * right third icon
     */
    private TextView mRightThirdTextView;
    private CharSequence mRightThirdText;
    private boolean mRightThirdVisible;
    private boolean isRightThirdChecked;

    /**
     * right second icon
     */
    private TextView mRightSecondTextView;
    private CharSequence mRightSecondText;
    private boolean mRightSecondVisible = false;
    private boolean isRightSecondChecked = false;

    /**
     * ok right icon
     */
    private TextView mOkTextView;
    private CharSequence mOkText;
    private boolean mOkVisible = false;
    private boolean isOkChecked = true;

    private static final int DEFAULT_BACK_MARGIN_RIGHT = 6;
    private Context context;

    public TitleToolbar(Context context) {
        this(context,null);
    }

    public TitleToolbar(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public TitleToolbar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        initCustomView(context,attrs,defStyleAttr);
    }

    @SuppressLint("RestrictedApi")
    protected void initCustomView(Context context, AttributeSet attrs, int defStyleAttr) {
        @SuppressLint("RestrictedApi") TintTypedArray a = TintTypedArray.obtainStyledAttributes(getContext(), attrs,
                styleable.Toolbar, defStyleAttr, 0);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.TitleToolbar);
        setBackgroundColor(ContextCompat.getColor(context, android.R.color.white));

        if (mTitleLayout == null) {
            mTitleLayout = new LinearLayoutCompat(context);
            mTitleLayout.setOrientation(LinearLayoutCompat.VERTICAL);
            mTitleLayout.setGravity(typedArray.getInt(
                    R.styleable.TitleToolbar_title_gravity, Gravity.CENTER_VERTICAL));

            addView(mTitleLayout, new LayoutParams(
                    LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT, Gravity.CENTER));
        }

        if (mTitleTextView == null) {
            mTitleTextView = new TextView(context);
            mTitleTextView.setSingleLine();
            mTitleTextView.setEllipsize(TextUtils.TruncateAt.END);
            mTitleTextView.setGravity(Gravity.CENTER);

            int titleTextAppearance = a.getResourceId(styleable.Toolbar_titleTextAppearance, 0);
            if (titleTextAppearance != 0) {
                mTitleTextView.setTextAppearance(context, titleTextAppearance);
            }

            if (a.hasValue(styleable.Toolbar_titleTextColor)) {
                int titleColor = a.getColor(styleable.Toolbar_titleTextColor, Color.BLACK);
                mTitleTextView.setTextColor(titleColor);
            }

            if (typedArray.hasValue(R.styleable.TitleToolbar_titleTextSize)) {
                mTitleTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                        typedArray.getDimensionPixelSize(R.styleable.TitleToolbar_titleTextSize, 32));
            }

            setTitle(a.getText(styleable.Toolbar_title));
            setTitleVisible(typedArray.getBoolean(R.styleable.TitleToolbar_titleVisible, true));

            if (mTitleLayout != null){
                mTitleLayout.addView(mTitleTextView,
                        new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
            }
        }

        if (mSubtitleTextView == null) {
            mSubtitleTextView = new TextView(context);
            mSubtitleTextView.setSingleLine();
            mSubtitleTextView.setEllipsize(TextUtils.TruncateAt.END);
            mSubtitleTextView.setGravity(Gravity.CENTER);

            int subTextAppearance = a.getResourceId(styleable.Toolbar_subtitleTextAppearance, 0);
            if (subTextAppearance != 0) {
                mSubtitleTextView.setTextAppearance(context, subTextAppearance);
            }

            if (a.hasValue(styleable.Toolbar_subtitleTextColor)) {
                int subTitleColor = a.getColor(styleable.Toolbar_subtitleTextColor, Color.WHITE);
                mSubtitleTextView.setTextColor(subTitleColor);
            }

            if (typedArray.hasValue(R.styleable.TitleToolbar_subtitleTextSize)) {
                mSubtitleTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                        typedArray.getDimensionPixelSize(
                                R.styleable.TitleToolbar_subtitleTextSize, 0));
            }

            setSubtitle(a.getText(styleable.Toolbar_subtitle));
            setSubtitleVisible(
                    typedArray.getBoolean(R.styleable.TitleToolbar_subtitleVisible, false));

            if (mTitleLayout!= null){
                mTitleLayout.addView(mSubtitleTextView,
                        new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
            }

        }
        setPadding(0,0,0,0);

        if (mBackTextView == null) {
            mBackTextView = new TextView(context);
            mBackTextView.setId(R.id.title_back);
            mBackTextView.setSingleLine();
            mBackTextView.setEllipsize(TextUtils.TruncateAt.END);
            mBackTextView.setGravity(Gravity.CENTER_VERTICAL);
            mBackTextView.setPadding(dp2px(15),0,dp2px(15),0);

            int backTextAppearance =
                    typedArray.getResourceId(R.styleable.TitleToolbar_backTextAppearance, 0);
            if (backTextAppearance != 0) {
                mBackTextView.setTextAppearance(context, backTextAppearance);
            }

            if (typedArray.hasValue(R.styleable.TitleToolbar_backTextColor)) {
                int backTextColor =
                        typedArray.getColor(R.styleable.TitleToolbar_backTextColor, Color.WHITE);
                mBackTextView.setTextColor(backTextColor);
            }

            if (typedArray.hasValue(R.styleable.TitleToolbar_backTextSize)) {
                mBackTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                        typedArray.getDimensionPixelSize(R.styleable.TitleToolbar_backTextSize, 0));
            }

            Drawable drawable = typedArray.getDrawable(R.styleable.TitleToolbar_backIcon);
            if (drawable != null) {
//                if (TextUtils.isEmpty(typedArray.getText(R.styleable.TitleToolbar_backText))) {
//                    mBackTextView.setBackground(drawable);
//                } else {
                mBackTextView.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);
                mBackTextView.setCompoundDrawablePadding(20);
//                }
            }

            setBackText(typedArray.getText(R.styleable.TitleToolbar_backText));
            setBackVisible(typedArray.getBoolean(R.styleable.TitleToolbar_backVisible, false));

            mBackTextView.setClickable(true);
            mBackTextView.setOnClickListener(this);

            LayoutParams layoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT,
                    LayoutParams.MATCH_PARENT, Gravity.START | Gravity.CENTER_VERTICAL);

            layoutParams.rightMargin = typedArray.getDimensionPixelSize(
                    R.styleable.TitleToolbar_backMarginRight, dp2px(DEFAULT_BACK_MARGIN_RIGHT));
            layoutParams.leftMargin = 0;

            addView(mBackTextView, layoutParams);
        }

        if (mCloseTextView == null) {
            mCloseTextView = new TextView(context);
            mCloseTextView.setId(R.id.title_close);
            mCloseTextView.setSingleLine();
            mCloseTextView.setEllipsize(TextUtils.TruncateAt.END);
            mCloseTextView.setGravity(Gravity.CENTER_VERTICAL);

            int closeTextAppearance =
                    typedArray.getResourceId(R.styleable.TitleToolbar_closeTextAppearance, 0);

            if (closeTextAppearance != 0) {
                mCloseTextView.setTextAppearance(context, closeTextAppearance);
            }

            if (typedArray.hasValue(R.styleable.TitleToolbar_closeTextColor)) {
                int closeTextColor =
                        typedArray.getColor(R.styleable.TitleToolbar_closeTextColor, Color.WHITE);
                mCloseTextView.setTextColor(closeTextColor);
            }

            if (typedArray.hasValue(R.styleable.TitleToolbar_closeTextSize)) {
                mCloseTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                        typedArray.getDimensionPixelSize(
                                R.styleable.TitleToolbar_closeTextSize, 0));
            }

            setCloseText(typedArray.getText(R.styleable.TitleToolbar_closeText));
            setCloseVisible(typedArray.getBoolean(R.styleable.TitleToolbar_closeVisible, false));

            mCloseTextView.setClickable(true);
            mCloseTextView.setOnClickListener(this);

            addView(mCloseTextView, new LayoutParams(LayoutParams.WRAP_CONTENT,
                    LayoutParams.MATCH_PARENT, Gravity.LEFT | Gravity.CENTER_VERTICAL));
        }


        if (mOkTextView== null) {
            mOkTextView = new TextView(context);
            mOkTextView.setId(R.id.title_ok);
            mOkTextView.setSingleLine();
            mOkTextView.setEllipsize(TextUtils.TruncateAt.END);
            mOkTextView.setGravity(Gravity.CENTER_VERTICAL);
            mOkTextView.setPadding(30, 20, 30, 20);
            mOkTextView.setCompoundDrawablePadding(10);

            int backTextAppearance =
                    typedArray.getResourceId(R.styleable.TitleToolbar_okTextAppearance, 0);
            if (backTextAppearance != 0) {
                mOkTextView.setTextAppearance(context, backTextAppearance);
            }

            if (typedArray.hasValue(R.styleable.TitleToolbar_okTextColor)) {
                int textColor =
                        typedArray.getColor(R.styleable.TitleToolbar_okTextColor, Color.WHITE);
                mOkTextView.setTextColor(textColor);
            }

            if (typedArray.hasValue(R.styleable.TitleToolbar_okTextSize)) {
                mOkTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                        typedArray.getDimensionPixelSize(R.styleable.TitleToolbar_okTextSize, 0));
            }

            Drawable drawable = typedArray.getDrawable(R.styleable.TitleToolbar_okIcon);
            if (drawable != null) {
                if (TextUtils.isEmpty(typedArray.getText(R.styleable.TitleToolbar_okIcon))) {
                    mOkTextView.setBackground(drawable);
                } else {
//                    mOkTextView.setBackground(ContextCompat.getDrawable(context, R.drawable.bg_progress_circle_white));
                    mOkTextView.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);
                }
            }

            setOkText(typedArray.getText(R.styleable.TitleToolbar_okText));
            setOkVisible(typedArray.getBoolean(R.styleable.TitleToolbar_okVisible, false));

            mOkTextView.setClickable(true);
            mOkTextView.setOnClickListener(this);

            LayoutParams layoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT,
                    LayoutParams.WRAP_CONTENT, Gravity.RIGHT | Gravity.CENTER_VERTICAL);

            layoutParams.rightMargin = typedArray.getDimensionPixelSize(
                    R.styleable.TitleToolbar_okMarginRight, dp2px(DEFAULT_BACK_MARGIN_RIGHT));
            layoutParams.setMargins(30, 0, 30, 0);
            addView(mOkTextView, layoutParams);
            setOkChecked(true);
        }

        if (mRightSecondTextView== null) {
            mRightSecondTextView = new TextView(context);
            mRightSecondTextView.setId(R.id.title_right_second_menu);
            mRightSecondTextView.setSingleLine();
            mRightSecondTextView.setEllipsize(TextUtils.TruncateAt.END);
            mRightSecondTextView.setGravity(Gravity.CENTER);
            mRightSecondTextView.setPadding(10, 30, 10, 20);

            int textAppearance =
                    typedArray.getResourceId(R.styleable.TitleToolbar_rightSecondTextAppearance, 0);
            if (textAppearance != 0) {
                mRightSecondTextView.setTextAppearance(context, textAppearance);
            }

            if (typedArray.hasValue(R.styleable.TitleToolbar_rightSecondTextColor)) {
                int textColor =
                        typedArray.getColor(R.styleable.TitleToolbar_rightSecondTextColor, Color.WHITE);
                mRightSecondTextView.setTextColor(textColor);
            }

            if (typedArray.hasValue(R.styleable.TitleToolbar_rightSecondTextSize)) {
                mRightSecondTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                        typedArray.getDimensionPixelSize(R.styleable.TitleToolbar_rightSecondTextSize, 0));
            }

            Drawable drawable = typedArray.getDrawable(R.styleable.TitleToolbar_rightSecondIcon);
            if (drawable != null) {
                if (TextUtils.isEmpty(typedArray.getText(R.styleable.TitleToolbar_rightSecondIcon))) {
                    mRightSecondTextView.setBackground(drawable);
                } else {
                    mRightSecondTextView.setCompoundDrawablesWithIntrinsicBounds(null, drawable, null, null);
                    mRightSecondTextView.setCompoundDrawablePadding(10);
                }
            }

            setRightSecondText(typedArray.getText(R.styleable.TitleToolbar_rightSecondText));
            setRightSecondVisible(typedArray.getBoolean(R.styleable.TitleToolbar_rightSecondVisible, false));

            mRightSecondTextView.setClickable(true);
            mRightSecondTextView.setOnClickListener(this);

            LayoutParams layoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT,
                    LayoutParams.MATCH_PARENT, Gravity.RIGHT | Gravity.CENTER);

            layoutParams.rightMargin = typedArray.getDimensionPixelSize(
                    R.styleable.TitleToolbar_rightSecondMarginRight, dp2px(DEFAULT_BACK_MARGIN_RIGHT));


            addView(mRightSecondTextView, layoutParams);
        }

        if (mRightThirdTextView == null) {
            mRightThirdTextView = new TextView(context);
            mRightThirdTextView.setId(R.id.title_right_third_menu);
            mRightThirdTextView.setSingleLine();
            mRightThirdTextView.setEllipsize(TextUtils.TruncateAt.END);
            mRightThirdTextView.setGravity(Gravity.CENTER);
            mRightThirdTextView.setPadding(10, 30, 10, 20);

            int textAppearance =
                    typedArray.getResourceId(R.styleable.TitleToolbar_rightThirdTextAppearance, 0);
            if (textAppearance != 0) {
                mRightThirdTextView.setTextAppearance(context, textAppearance);
            }

            if (typedArray.hasValue(R.styleable.TitleToolbar_rightThirdTextColor)) {
                int textColor =
                        typedArray.getColor(R.styleable.TitleToolbar_rightThirdTextColor, Color.WHITE);
                mRightThirdTextView.setTextColor(textColor);
            }

            if (typedArray.hasValue(R.styleable.TitleToolbar_rightThirdTextSize)) {
                mRightThirdTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                        typedArray.getDimensionPixelSize(R.styleable.TitleToolbar_rightThirdTextSize, 0));
            }

            Drawable drawable = typedArray.getDrawable(R.styleable.TitleToolbar_rightThirdIcon);
            if (drawable != null) {
                mRightThirdTextView.setCompoundDrawablesWithIntrinsicBounds(null, drawable, null, null);
                mRightThirdTextView.setCompoundDrawablePadding(10);
            }

            setRightThirdText(typedArray.getText(R.styleable.TitleToolbar_rightThirdText));
            setRightThirdVisible(typedArray.getBoolean(R.styleable.TitleToolbar_rightThirdVisible, false));

            mRightThirdTextView.setClickable(true);
            mRightThirdTextView.setOnClickListener(this);

            LayoutParams layoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT,
                    LayoutParams.MATCH_PARENT, Gravity.RIGHT | Gravity.CENTER_VERTICAL);

            layoutParams.rightMargin = typedArray.getDimensionPixelSize(
                    R.styleable.TitleToolbar_rightThirdMarginRight, dp2px(DEFAULT_BACK_MARGIN_RIGHT));

            addView(mRightThirdTextView, layoutParams);
        }

        typedArray.recycle();
        a.recycle();
    }

    public void setTitle(CharSequence title) {
        mTitleText = title;
        if (mTitleTextView != null) {
            mTitleTextView.setText(title);
        }
    }

    public CharSequence getTitle() {
        return mTitleText;
    }

    public void setTitleTextAppearance(Context context, int resId) {
        if (mTitleTextView != null) {
            mTitleTextView.setTextAppearance(context, resId);
        }
    }

    public void setTitleTextColor(int color) {
        if (mTitleTextView != null) {
            mTitleTextView.setTextColor(color);
        }
    }

    public void setTitleVisible(boolean visible) {
        mTitleVisible = visible;
        mTitleTextView.setVisibility(mTitleVisible ? VISIBLE : GONE);
    }

    public boolean getTitleVisible() {
        return mTitleVisible;
    }

    public void setSubtitle(CharSequence subtitle) {
        mSubTitleText = subtitle;
        if (mSubtitleTextView != null) {
            mSubtitleTextView.setText(subtitle);
        }
    }

    public CharSequence getSubtitle() {
        return mSubTitleText;
    }

    public void setSubtitleTextAppearance(Context context, int resId) {
        if (mSubtitleTextView != null) {
            mSubtitleTextView.setTextAppearance(context, resId);
        }
    }

    public void setSubtitleTextColor(int color) {
        if (mSubtitleTextView != null) {
            mSubtitleTextView.setTextColor(color);
        }
    }

    public void setSubtitleVisible(boolean visible) {
        mSubTitleVisible = visible;
        mSubtitleTextView.setVisibility(visible ? VISIBLE : GONE);
    }

    public LinearLayoutCompat getmTitleLayout() {
        return mTitleLayout;
    }


    public void setRightThirdChecked(boolean rightThirdChecked) {
        isRightThirdChecked = rightThirdChecked;
        if (isRightThirdChecked) {
            mRightThirdTextView.setBackgroundResource(android.R.color.white);
//            mRightThirdTextView.setCompoundDrawablesWithIntrinsicBounds(null, ContextCompat.getDrawable(context, R.mipmap.filter), null, null);
        } else {
            mRightThirdTextView.setBackgroundResource(0);
//            mRightThirdTextView.setCompoundDrawablesWithIntrinsicBounds(null, ContextCompat.getDrawable(context, R.mipmap.filter_disable), null, null);
        }
    }

    public void setRightSecondChecked(boolean rightSecondChecked) {
        isRightSecondChecked = rightSecondChecked;
        if (isRightSecondChecked) {
            mRightSecondTextView.setBackgroundResource(android.R.color.white);
        } else {
            mRightSecondTextView.setBackgroundResource(0);
        }
    }

    public void setOkChecked(boolean okChecked) {
        isOkChecked = okChecked;
        if (isOkChecked) {
            mOkTextView.setTextColor(ContextCompat.getColor(getContext(), android.R.color.black));
        } else {
            mOkTextView.setTextColor(ContextCompat.getColor(getContext(), android.R.color.darker_gray));
        }
    }

    public boolean isRightThirdChecked() {
        return isRightThirdChecked;
    }

    public boolean isRightSecondChecked() {
        return isRightSecondChecked;
    }

    public boolean isOkChecked() {
        return isOkChecked;
    }

    public boolean getSubtitleVisible() {
        return mSubTitleVisible;
    }

    public void setCloseText(int resId) {
        setCloseText(getContext().getText(resId));
    }

    public void setCloseText(CharSequence closeText) {
        mCloseText = closeText;
        if (mCloseTextView != null) {
            mCloseTextView.setText(closeText);
        }
    }

    public CharSequence getCloseText() {
        return mCloseText;
    }

    public void setCloseTextColor(int color) {
        mCloseTextView.setTextColor(color);
    }

    public void setCloseVisible(boolean visible) {
        mCloseVisible = visible;
        mCloseTextView.setVisibility(mCloseVisible ? VISIBLE : GONE);
    }

    public boolean isCloseVisible() {
        return mCloseVisible;
    }

    public void setBackText(int resId) {
        setBackText(getContext().getText(resId));
    }

    public void setBackText(CharSequence backText) {
        mBackText = backText;
        if (mBackTextView != null) {
            mBackTextView.setText(backText);
        }
    }

    public CharSequence getBackText() {
        return mBackText;
    }

    public void setBackTextColor(int color) {
        mBackTextView.setTextColor(color);
    }

    public void setBackVisible(boolean visible) {
        mBackVisible = visible;
        mBackTextView.setVisibility(mBackVisible ? VISIBLE : GONE);
    }

    public boolean isBackVisible() {
        return mBackVisible;
    }


    /*s设置right second 按钮*/
    public void setRightSecondText(CharSequence text) {
        mRightSecondText = text;
        if (mRightSecondTextView != null) {
            mRightSecondTextView.setText(text);
        }
    }

    public CharSequence getRightSecondText() {
        return mRightSecondText;
    }

    public void setRightSecondTextColor(int color) {
        mRightSecondTextView.setTextColor(color);
    }

    public void setRightSecondVisible(boolean visible) {
        mRightSecondVisible = visible;
        mRightSecondTextView.setVisibility(mRightSecondVisible ? VISIBLE : GONE);
    }

    public boolean isRightSecondVisible() {
        return mRightSecondVisible;
    }


    /**
     * 设置 Right Third 按钮
     * @param text
     */
    public void setRightThirdText(CharSequence text) {
        mRightThirdText = text;
        if (mRightThirdTextView != null) {
            mRightThirdTextView.setText(text);
        }
    }

    public CharSequence getRightThirdText() {
        return mRightThirdText;
    }

    public void setRightThirdTextColor(int color) {
        mRightThirdTextView.setTextColor(color);
    }

    public void setRightThirdVisible(boolean visible) {
        mRightThirdVisible = visible;
        mRightThirdTextView.setVisibility(mRightThirdVisible ? VISIBLE : GONE);
    }

    public boolean isRightThirdVisible() {
        return mRightThirdVisible;
    }

    /*s设置确认按钮*/
    public void setOkText(CharSequence okText) {
        mOkText = okText;
        if (mOkTextView != null) {
            mOkTextView.setText(okText);
        }
    }

    public CharSequence getOkText() {
        return mOkText;
    }

    public void setOkTextColor(int color) {
        mOkTextView.setTextColor(color);
    }

    public void setOkVisible(boolean visible) {
        mOkVisible = visible;
        mOkTextView.setVisibility(mOkVisible ? VISIBLE : GONE);
    }

    public boolean isOkVisible() {
        return mOkVisible;
    }

    public TextView getTitleTextView() {
        return mTitleTextView;
    }

    public TextView getSubtitleTextView() {
        return mSubtitleTextView;
    }

    public TextView getCloseTextView() {
        return mCloseTextView;
    }

    public TextView getBackTextView() {
        return mBackTextView;
    }

    public TextView getRightThirdTextView() {
        return mRightThirdTextView;
    }

    public TextView getRightSecondTextView() {
        return mRightSecondTextView;
    }

    public TextView getOkTextView() {
        return mOkTextView;
    }

    public void setOkIconLeft(@DrawableRes int imgRes) {
        if (null == mOkTextView) {
            return;
        }
        Drawable drawable = getResources().getDrawable(imgRes);
        mOkTextView.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);
        mOkTextView.setCompoundDrawablePadding(10);
    }

    @Override
    public void onClick(View v) {
        if (mOnToolbarItemClickListener != null) {
            mOnToolbarItemClickListener.onToolbarItemClick(v);
        }
    }
    private OnToolbarItemClickListener mOnToolbarItemClickListener;

    public void setToolbarClickListener(OnToolbarItemClickListener listener){
        this.mOnToolbarItemClickListener = listener;
    }
    public interface OnToolbarItemClickListener{
        /**
         * 点击监听
         * @param view
         */
        void onToolbarItemClick(View view);
    }
}
