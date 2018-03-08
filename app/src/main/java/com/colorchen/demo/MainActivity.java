package com.colorchen.demo;

import android.content.Intent;
import android.widget.TextView;

import com.blankj.utilcode.util.ToastUtils;
import com.colorchen.demo.base.BaseActivity;
import com.jakewharton.rxbinding2.view.RxView;

import butterknife.BindView;

/**
 * name：MainActivity
 * @author: ChenQ
 * @date: 2018-1-12
 * @email: wxchenq@yutong.com
 */
public class MainActivity extends BaseActivity {
    /*@BindView(R.id.text1)
    TextView textView1;*/
    @Override
    public int layoutId() {
        return R.layout.activity_main;
    }

    @Override
    public void initView() {
        TextView textView1 = findViewById(R.id.text1);
        RxView.clicks(textView1).subscribe(v->{
            ToastUtils.showShort("onClick");
            startActivity(new Intent(getApplicationContext(),VideoPlayerActivity.class));
        });
    }

    @Override
    public void initData() {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
