package com.colorchen.demo;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.colorchen.demo.base.BaseActivity;
import com.colorchen.qbase.widget.item.SwipeItemLayout;
import com.colorchen.qbase.widget.mutilstatelayout.MultiStateLayout;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @author ChenQ
 * @name： ColorDemo
 * @date 2018-3-8
 * @email： wxchenq@yutong.com
 * des：
 */
public class TestVideoListActivity extends BaseActivity {

    @BindView(R.id.teacherMainList)
    RecyclerView recyclerView;
    @BindView(R.id.teacherRefreshLayout)
    SmartRefreshLayout refreshLayout;
    @BindView(R.id.teacherLoadingLayout)
    MultiStateLayout emptyLayout;

    private long refreshTime = 0;
    @Override
    protected int layoutId() {
        return R.layout.activity_test_video;
    }

    @Override
    protected void initView() {
        refreshLayout.setEnableRefresh(false);
        refreshLayout.setEnableLoadmore(false);

        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        recyclerView.addOnItemTouchListener(new SwipeItemLayout.OnSwipeItemTouchListener(getApplicationContext()));

    }

    private RouterAdapter adapter;
    @Override
    protected void initData() {
        adapter = new RouterAdapter(getApplicationContext());
        recyclerView.setAdapter(adapter);
    }

    public class RouterAdapter extends RecyclerView.Adapter<RouterAdapter.Holder> {
        private Context mContext;
        private List<VideoBean> dataList;

        public RouterAdapter(Context context) {
            mContext = context;
            dataList = new ArrayList<>();
            initTest();
        }

        private void initTest() {
            dataList.add(new VideoBean("你好","金币数：100"));
            dataList.add(new VideoBean("你好","金币数：100"));
            dataList.add(new VideoBean("你好","金币数：100"));
            dataList.add(new VideoBean("你好","金币数：100"));
            dataList.add(new VideoBean("你好","金币数：100"));
            dataList.add(new VideoBean("你好","金币数：100"));
        }

        public void setData(List<VideoBean> list) {
            this.dataList.clear();
            this.dataList.addAll(list);
            notifyDataSetChanged();
        }

        @Override
        public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
            View root = LayoutInflater.from(mContext).inflate(R.layout.item_test_video, parent, false);
            return new Holder(root);
        }

        @Override
        public void onBindViewHolder(Holder holder, int position) {
            VideoBean routerEntity = dataList.get(position);
            holder.itemVideoTv.setText(routerEntity.startStationId);
        }

        @Override
        public int getItemCount() {
            return dataList.size();
        }

        class Holder extends RecyclerView.ViewHolder {
            @BindView(R.id.itemVideoTv)
            TextView itemVideoTv;

            Holder(View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
            }
        }
    }
}
