package com.colorchen.demo;

import android.content.Intent;
import android.widget.TextView;

import com.colorchen.demo.base.BaseActivity;
import com.colorchen.demo.map.baidu.MapScrollingActivity;
import com.colorchen.demo.map.baidu.MarkerClusterDemo;
import com.jakewharton.rxbinding2.view.RxView;

import butterknife.BindView;

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
    @BindView(R.id.text2)
    TextView textView2;
    @BindView(R.id.text3)
    TextView textView3;
    @Override
    public int layoutId() {
        return R.layout.activity_main;
    }

    @Override
    public void initView() {
        RxView.clicks(textView1).subscribe(o -> startActivity(new Intent(getApplicationContext(),MarkerClusterDemo.class)));
        RxView.clicks(textView2).subscribe(o -> startActivity(new Intent(getApplicationContext(),MapScrollingActivity.class)));
    }

    @Override
    public void initData() {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


}
