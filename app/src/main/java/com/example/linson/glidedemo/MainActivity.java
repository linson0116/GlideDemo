package com.example.linson.glidedemo;

import android.app.Activity;
import android.app.ProgressDialog;
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
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.List;

import static android.content.ContentValues.TAG;

public class MainActivity extends Activity {

    private final String NONE = "none";
    private ViewPager vp;
    private List<String> mPageUrlList = new ArrayList<>();
    private List<String> mImageUrlList = new ArrayList<>();
    private int mCurrentPageNumber = 0;
    private int mPageTotal;
    private ArrayList[] mImageArray;
    private boolean isLoading = false;
    private MyPagerAdapter mAdapter;
    private ProgressDialog mProgressDialog;
    private int newPages;
    public Handler mHandler = new Handler() {

        int pagerNumber;
        ArrayList<String> imageUrlList;
        private List<String> list;
        private String content;

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 100:
                    content = (String) msg.obj;
                    //List<String> list = RegularUtils.getUrlfromHtmlContent(content);
                    //mImageUrlList = list;
//                    Log.i(TAG, "100: mImageUrlList = " + mImageUrlList);
                    mAdapter.notifyDataSetChanged();
                    break;
                case 101:
                    content = (String) msg.obj;
                    list = RegularUtils.getUrlfromHtmlContent(content);
                    mImageUrlList.addAll(list);
                    mAdapter.notifyDataSetChanged();
                    isLoading = false;
                    break;
                case 200:
                    pagerNumber = msg.arg1;
                    ArrayList<String> imageUrlList = (ArrayList<String>) msg.obj;
                    mImageArray[pagerNumber] = imageUrlList;
                    mCurrentPageNumber++;
                    mProgressDialog.setProgress(mCurrentPageNumber);
                    if (mCurrentPageNumber == mPageTotal) {
                        mProgressDialog.dismiss();
                        //将数据装入内存
                        for (int i = 0; i < mImageArray.length; i++) {
                            mImageUrlList.addAll(mImageArray[mImageArray.length - 1 - i]);
                        }
                        Log.i(TAG, "将数据装入内存  新--旧");
                        //将数据写入数据库
                        for (int i = 0; i < mImageArray.length; i++) {
                            ImageBean imageBean = new ImageBean();
                            imageBean.setImageUrl(mImageUrlList.get(i));
                            imageBean.save();
                        }
                        Log.i(TAG, "将数据写入数据库  旧--新");
                        mAdapter.notifyDataSetChanged();
                    }
                    break;
                case 201:
                    pagerNumber = msg.arg1;
                    imageUrlList = (ArrayList<String>) msg.obj;
                    mImageArray[pagerNumber] = imageUrlList;
                    mCurrentPageNumber++;
                    mProgressDialog.setProgress(mCurrentPageNumber);
                    if (mCurrentPageNumber == newPages) {
                        mProgressDialog.dismiss();
                        //将新的数据装入内存
                        mImageUrlList.clear();
                        for (int i = 0; i < mImageArray.length; i++) {
                            mImageUrlList.addAll(mImageArray[i]);
                        }
                        //将旧的数据装入内存
                        List<ImageBean> allImageBean = DataSupport.findAll(ImageBean.class);
                        for (ImageBean bean : allImageBean) {
                            mImageUrlList.add(bean.getImageUrl());
                        }
                        Log.i(TAG, "将数据装入内存  新--旧");
                        //将新的数据写入数据库
                        for (int i = 0; i < mImageArray.length; i++) {
                            ArrayList<String> imageList = mImageArray[mImageArray.length - 1 - i];
                            for (String url : imageList) {
                                ImageBean imageBean = new ImageBean();
                                imageBean.setImageUrl(url);
                                List<ImageBean> result = DataSupport.where("imageUrl like ?", url).find(ImageBean.class);
                                if (result.size() == 0) {
                                    imageBean.save();
                                    Log.i(TAG, "新数据: " + url);
                                } else {
                                    Log.i(TAG, "数据重复 跳出");
                                    break;
                                }
                            }
                        }
                        Log.i(TAG, "将新数据写入数据库  旧--新");
                        //读取标记

                        //查找标记数据

                        //vp转到此页面


                        mAdapter.notifyDataSetChanged();
                    }
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        initUI();
        initData();
//        test();
    }

    private void test() {
        ArrayList<String> list = new ArrayList<>();
        list.add(0, "0");
        list.add(1, null);
        Log.i(TAG, "test: " + list.size());
        list.remove(null);
        Log.i(TAG, "test: " + list.size());
    }

    private void initData() {
        Log.i(TAG, "数据初始化");
        //取得pageNum
        getPageNum();
    }

    private void getImageList() {
        final String stringValue = ToolsUtils.getStringValue(this, "init", NONE);
        if (stringValue.equals(NONE)) {
            Log.i(TAG, "数据库未初始化");
            //添加网页地址
            for (int i = mPageTotal; i > 0; i--) {
                if (i == 1) {
                    mPageUrlList.add("http://www.qiumeimei.com/tag/gif");
                } else {
                    mPageUrlList.add("http://www.qiumeimei.com/tag/gif/page/" + i);
                }
            }
            mImageArray = new ArrayList[mPageTotal];
            //数据库初始化
//            for (final String url : mPageUrlList) {
//                Log.i(TAG, "getImageList: url " + url);
//                HttpUtils httpUtils = new HttpUtils();
//                httpUtils.send(HttpRequest.HttpMethod.GET, url, new RequestCallBack<String>() {
//                    @Override
//                    public void onSuccess(ResponseInfo<String> responseInfo) {
//                        List<String> list = RegularUtils.getUrlfromHtmlContent(responseInfo.result);
//                        mImageUrlList.addAll(list);
//                        Log.i(TAG, "解析图片  " + url +" mImageUrlList " + mImageUrlList.size());
//                        for (String url : list) {
//                            ImageBean imageBean = new ImageBean();
//                            imageBean.setImageUrl(url);
//                            imageBean.save();
//                        }
//                        overIndex++;
//                        mProgressDialog.setProgress(overIndex);
//                        if (overIndex == mPageTotal) {
//                            mProgressDialog.dismiss();
//                            Log.i(TAG, "图片数量 " + mImageUrlList.size());
////                            String imageUrl = mImageUrlList.get(0);
////                            getHtmlContent(imageUrl,100);
//                            mAdapter.notifyDataSetChanged();
//
//
//                        }
//                    }
//                    @Override
//                    public void onFailure(HttpException e, String s) {
//                        Log.i(TAG, "onFailure: ");
//                    }
//                });
//            }

            //设置标识 pageNum
            Log.i(TAG, "当前页面数共" + mPageUrlList.size() + "页");
            for (int i = 0; i < mPageUrlList.size(); i++) {
                new RunThreadGetImageUrl(mHandler, i, mPageUrlList.get(i), 200).run();
            }
            ToolsUtils.setString(this, "init", String.valueOf(mPageUrlList.size()));
        } else {
            int dbPageNumber = Integer.parseInt(stringValue);
            newPages = (mPageTotal - dbPageNumber) + 1;
            Log.i(TAG, "数据库已经初始化，追加更新 " + newPages);
            for (int i = 1; i <= newPages; i++) {
                if (i == 1) {
                    mPageUrlList.add("http://www.qiumeimei.com/tag/gif");
                } else {
                    mPageUrlList.add("http://www.qiumeimei.com/tag/gif/page/" + i);
                }
            }
            mImageArray = new ArrayList[newPages];
            for (int i = 0; i < mPageUrlList.size(); i++) {
                new RunThreadGetImageUrl(mHandler, i, mPageUrlList.get(i), 201).run();
            }
        }

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
                    //Log.i(TAG, "onPageScrolled: 取得更多数据 " + position);
                    Toast.makeText(MainActivity.this, "没有更多数据", Toast.LENGTH_SHORT).show();
                    //getHtmlContent(mPageUrlList.get(mCurrentPageNumber--), 101);
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

    public void getPageNum() {
        HttpUtils httpUtils = new HttpUtils();
        String url = "http://www.qiumeimei.com/tag/gif";
        httpUtils.send(HttpRequest.HttpMethod.GET, url, new RequestCallBack<String>() {
            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                String content = responseInfo.result;
                mPageTotal = Integer.parseInt(RegularUtils.getPageNumfromHtmlContent(content));
                //mPageTotal = 5;
                mProgressDialog = new ProgressDialog(MainActivity.this);
                mProgressDialog.setMessage("数据初始化中");
                mProgressDialog.setMax(mPageTotal);
                mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                mProgressDialog.show();

                //初始化数据库
                getImageList();
            }

            @Override
            public void onFailure(HttpException e, String s) {
                Log.i(TAG, "取得页面总数失败: " + s);
                Toast.makeText(MainActivity.this, "取得页面总数失败", Toast.LENGTH_SHORT).show();
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
//            imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
//            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            String url = mImageUrlList.get(position);
            Glide.with(getApplicationContext())
                    .load(url)
                    .placeholder(R.drawable.default_d_large)
                    .fitCenter()
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .into(imageView);
            Log.i(TAG, "glide装载: url " + url);
            container.addView(imageView);
            return imageView;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }
    }
}

class RunThreadGetImageUrl extends Thread {
    Handler handler;
    int pageNumber;
    String url;
    int type;

    RunThreadGetImageUrl(Handler handler, int pageNumber, String url, int type) {
        this.handler = handler;
        this.pageNumber = pageNumber;
        this.url = url;
        this.type = type;
    }

    @Override
    public void run() {
        HttpUtils httpUtils = new HttpUtils();
        httpUtils.send(HttpRequest.HttpMethod.GET, url, new RequestCallBack<String>() {
            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                String content = responseInfo.result;
                List<String> imageUrlList = RegularUtils.getUrlfromHtmlContent(content);
                Message message = handler.obtainMessage();
                message.what = type;
                message.arg1 = pageNumber;
                message.obj = imageUrlList;
                handler.sendMessage(message);
            }

            @Override
            public void onFailure(HttpException e, String s) {
            }
        });
    }
}
