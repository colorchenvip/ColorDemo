package com.colorchen.demo;

import android.Manifest;
import android.content.Intent;
import android.widget.TextView;

import com.blankj.utilcode.util.ToastUtils;
import com.colorchen.demo.base.BaseActivity;
import com.colorchen.demo.map.baidu.MapScrollingActivity;
import com.colorchen.demo.map.baidu.MarkerClusterDemo;
import com.colorchen.qbase.utils.RxUtils;
import com.jakewharton.rxbinding2.view.RxView;
import com.tbruyelle.rxpermissions2.RxPermissions;

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
        RxPermissions rxPermissions = new RxPermissions(this);
        rxPermissions.request(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.CAMERA)
                .compose(RxUtils.filterAndroidVersion())
                .subscribe(aBoolean -> {
                    if (aBoolean) {

                    } else {
                        ToastUtils.showShort("需要权限");
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


}
