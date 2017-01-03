package com.example.linson.glidedemo;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;

import java.util.ArrayList;
import java.util.List;

import static android.content.ContentValues.TAG;

public class MainActivity extends Activity {

    private ViewPager vp;
    private List<String> mPageUrlList = new ArrayList<>();
    private List<String> mImageUrlList = new ArrayList<>();
    private int mPageNumber = 0;
    private boolean isLoading = false;
    public Handler mHandler = new Handler(){

        private List<String> list;
        private String content;

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 100:
                    content = (String) msg.obj;
                    List<String> list = RegularUtils.getUrlfromHtmlContent(content);
                    mImageUrlList = RegularUtils.getSingleList(list);
                    mAdapter.notifyDataSetChanged();
                    break;
                case 101:
                    String content = (String) msg.obj;
                    list = RegularUtils.getUrlfromHtmlContent(content);
                    List<String> newList = RegularUtils.getSingleList(list);
                    mImageUrlList.addAll(newList);
                    mAdapter.notifyDataSetChanged();
                    isLoading = false;
                    break;
            }
        }
    };
    private MyPagerAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        initUI();
        initData();
    }

    private void initData() {
        //添加网页地址
        for (int i = 1; i <= 82; i++) {
            if (i == 1) {
                mPageUrlList.add("http://www.qiumeimei.com/tag/gif");
            } else {
                mPageUrlList.add("http://www.qiumeimei.com/tag/gif/page/" + i);
            }
        }
        //取得一页的ImageUrl
        getHtmlContent(mPageUrlList.get(mPageNumber++),100);
    }

    private void initUI() {
        vp = (ViewPager) findViewById(R.id.vp);
        //设置viewpager数据适配器
        mAdapter = new MyPagerAdapter();
        vp.setAdapter(mAdapter);
        vp.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                //Log.i(TAG, "onPageScrolled: 当前页码索引 " + position);
                //取得更新数据
                if (position == mImageUrlList.size() - 1 && !isLoading) {
                    Log.i(TAG, "onPageScrolled: 取得更新数据 " + position);
                    getHtmlContent(mPageUrlList.get(mPageNumber++),101);
                }
            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    private void getHtmlContent(String url, final int flag) {
        HttpUtils httpUtils = new HttpUtils();
        //Log.i(TAG, "getHtmlContent: 网络地址 " + url);
        if (flag == 101) {
            isLoading = true;
        }
        httpUtils.send(HttpRequest.HttpMethod.GET, url, new RequestCallBack<String>() {
            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
//                Log.i(TAG, "onSuccess: 网页内容" + responseInfo.result);
                if (flag == 100) {
                    Message msg = Message.obtain();
                    msg.what = 100;
                    msg.obj = responseInfo.result;
                    mHandler.sendMessage(msg);
                } else if (flag == 101) {
                    Message msg = Message.obtain();
                    msg.what = 101;
                    msg.obj = responseInfo.result;
                    mHandler.sendMessage(msg);
                }
            }

            @Override
            public void onFailure(HttpException e, String s) {
                Log.i(TAG, "onFailure: 网络异常 " + e.toString());
            }
        });
    }

    class MyPagerAdapter extends PagerAdapter {

        @Override
        public int getCount() {
            return mImageUrlList.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            ImageView imageView = new ImageView(getApplicationContext());
            String url = mImageUrlList.get(position);
            Glide.with(getApplicationContext())
                    .load(url)
                    .placeholder(R.drawable.default_d_large)
                    .fitCenter()
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .into(imageView);
            Log.i(TAG, "instantiateItem: url " + url);
            container.addView(imageView);
            return imageView;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }
    }
}
