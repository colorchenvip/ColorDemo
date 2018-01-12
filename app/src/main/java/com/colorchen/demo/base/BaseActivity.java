package com.colorchen.demo.base;

import android.app.Application;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.colorchen.demo.R;

import butterknife.ButterKnife;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import me.yokeyword.fragmentation.SupportActivity;

/**
 * @author ChenQ
 * @name： ColorDemo
 * @date 2018-1-12
 * @email： wxchenq@yutong.com
 * des：
 */
public abstract class BaseActivity extends AppCompatActivity {
    protected CompositeDisposable compositeDisposable;
    @LayoutRes
    protected abstract int layoutId();

    protected abstract void initView();

    protected abstract void initData();

    /**
     * 优化重构onCreate 方法加入定义方法
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        layoutId();
        super.onCreate(savedInstanceState);
        setContentView(layoutId());
        ButterKnife.bind(this);
        compositeDisposable = new CompositeDisposable();
        initView();
        initData();
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (compositeDisposable != null) {
            compositeDisposable.dispose();
        }
    }

    /**
     * 添加disposable到复合disposable
     *
     * @param disposable
     */
    protected void addDisposable(Disposable disposable) {
        if (compositeDisposable == null) {
            compositeDisposable = new CompositeDisposable();
        }
        compositeDisposable.add(disposable);
    }

    @Override
    public void startActivity(Intent intent) {
        super.startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.stay);
    }

    @Override
    public void startActivityForResult(Intent intent, int requestCode) {
        super.startActivityForResult(intent, requestCode);
        overridePendingTransition(R.anim.slide_in_right, R.anim.stay);
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.stay, R.anim.slide_out_right);
    }
}
