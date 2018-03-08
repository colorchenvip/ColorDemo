package com.colorchen.demo;

import android.support.v7.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.colorchen.demo.base.BaseActivity;

import butterknife.BindView;
import cn.jzvd.JZVideoPlayer;
import cn.jzvd.JZVideoPlayerStandard;

/**
 * name：VideoPlayerActivity
 * @author: ChenQ
 * @date: 2018-3-8
 */
public class VideoPlayerActivity extends BaseActivity{

    @BindView(R.id.videoPlayer)
    JZVideoPlayerStandard jzVideoPlayerStandard ;
    @Override
    protected int layoutId() {
        return R.layout.activity_video_player;
    }

    @Override
    protected void initView() {

    }

    @Override
    protected void initData() {
        jzVideoPlayerStandard.setUp("http://jzvd.nathen.cn/c6e3dc12a1154626b3476d9bf3bd7266/6b56c5f0dc31428083757a45764763b0-5287d2089db37e62345123a1be272f8b.mp4"
                , JZVideoPlayerStandard.SCREEN_WINDOW_NORMAL, "饺子闭眼睛");
        Glide.with(this).load("http://p.qpic.cn/videoyun/0/2449_43b6f696980311e59ed467f22794e792_1/640").into(jzVideoPlayerStandard.thumbImageView);
    }

    @Override
    protected void onPause() {
        super.onPause();
        JZVideoPlayer.releaseAllVideos();
    }

}
