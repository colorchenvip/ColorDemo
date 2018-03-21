package com.colorchen.demo;

import android.widget.TextView;

import com.blankj.utilcode.util.ToastUtils;
import com.colorchen.demo.base.BaseActivity;
import com.jakewharton.rxbinding2.view.RxView;

import butterknife.BindView;
import io.reactivex.functions.Consumer;

/**
 * name：MainActivity
 *
 * @author: ChenQ
 * @date: 2018-1-12
 * @email: wxchenq@yutong.com
 */
public class MainActivity extends BaseActivity {

    @BindView(R.id.text1)
    TextView textView1;
    @Override
    public int layoutId() {
        return R.layout.activity_main;
    }

    @Override
    public void initView() {
        RxView.clicks(textView1).subscribe(o -> ToastUtils.showShort("点击事件"));
    }

    @Override
    public void initData() {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
