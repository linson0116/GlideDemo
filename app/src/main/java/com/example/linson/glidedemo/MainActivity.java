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
import android.widget.Button;
import android.widget.EditText;
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

    public static final String CURRENT_PAGE_NUMBER = "currentPageNumber";
    private static final String NONE = "none";
    private Button btn_to_page;
    private EditText et_page_number;
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

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 200:
                    pagerNumber = msg.arg1;
                    ArrayList<String> imageUrlList = (ArrayList<String>) msg.obj;
                    mImageArray[pagerNumber] = imageUrlList;
                    mCurrentPageNumber++;
                    mProgressDialog.setProgress(mCurrentPageNumber);
                    if (mCurrentPageNumber == mPageTotal) {
                        //将数据写入数据库
                        //Log.i(TAG, "handleMessage: " + mImageArray.length);
                        int sum = 0;
                        for (int i = 0; i < mImageArray.length; i++) {
                            ArrayList list = mImageArray[i];
                            for (int j = list.size() - 1; j >= 0; j--) {
                                ImageBean bean = new ImageBean();
                                bean.setImageUrl((String) list.get(j));
                                bean.save();
                                sum++;
                                Log.i(TAG, "handleMessage: " + bean.getImageUrl());
                            }
                        }
                        Log.i(TAG, "将数据写入数据库 写入 " + sum + " 条");

                        //将数据装入内存
                        mImageUrlList.clear();
                        List<ImageBean> all = DataSupport.order("id desc").find(ImageBean.class);
                        Log.i(TAG, "数据中共有" + all.size() + "条数据");
                        for (ImageBean bean : all) {
                            mImageUrlList.add(bean.getImageUrl());
                        }
                        Log.i(TAG, "将数据装入内存   新--旧");
                        mAdapter.notifyDataSetChanged();
                        mProgressDialog.dismiss();
                    }
                    break;
                case 201:
                    pagerNumber = msg.arg1;
                    imageUrlList = (ArrayList<String>) msg.obj;
                    mImageArray[pagerNumber] = imageUrlList;
                    mCurrentPageNumber++;
                    mProgressDialog.setProgress(mCurrentPageNumber);
                    if (mCurrentPageNumber == newPages) {
                        Log.i(TAG, "将新数据写入数据库  旧--新");
                        //将新的数据写入数据库
                        Log.i(TAG, "新数据组数：" + mImageArray.length);
                        for (int i = 0; i < mImageArray.length; i++) {
                            ArrayList<String> imageList = mImageArray[mImageArray.length - 1 - i];
                            Log.i(TAG, "新数据个数：" + imageList.size());
                            for (int j = imageList.size() - 1; j >= 0; j--) {
                                ImageBean imageBean = new ImageBean();
                                String imageUrl = imageList.get(j);
                                //Log.i(TAG, "新数据 URL：" + imageUrl);
                                imageBean.setImageUrl(imageUrl);
                                List<ImageBean> result = DataSupport.where("imageUrl like ?", imageUrl).find(ImageBean.class);
                                if (result.size() == 0) {
                                    imageBean.save();
                                    Log.i(TAG, "新数据添加: " + imageUrl);
                                } else {
                                    Log.i(TAG, "数据重复 不添加 " + imageUrl);
                                }
                            }
                        }
                        Log.i(TAG, "将数据装入内存  新--旧");
                        //将数据库的数据装入内存
                        mImageUrlList.clear();
                        List<ImageBean> all = DataSupport.order("id desc").find(ImageBean.class);
                        Log.i(TAG, "数据中共有" + all.size() + "条数据");
                        for (ImageBean bean : all) {
                            mImageUrlList.add(bean.getImageUrl());
                        }
                        //读取标记
                        String stringValue = ToolsUtils.getStringValue(MainActivity.this, CURRENT_PAGE_NUMBER, "0");
                        int intValue = Integer.parseInt(stringValue);
                        if (intValue == 0) {
                            Log.i(TAG, "没有标记过页面");
                        } else {
                            Log.i(TAG, "标记为第" + intValue + "页");
                            //查找标记数据
                            ImageBean imageBean = DataSupport.find(ImageBean.class, Integer.parseInt(stringValue));
                            Log.i(TAG, "标记文件  " + imageBean.getImageUrl());
                            //vp转到此页面
                            mAdapter.notifyDataSetChanged();
                            vp.setCurrentItem(intValue - 1, true);
                            et_page_number.setText(intValue + "");
                        }
                        mAdapter.notifyDataSetChanged();
                        mProgressDialog.dismiss();
                        //存储新初始化页数
                        ToolsUtils.setString(MainActivity.this, "init", String.valueOf(mPageUrlList.size()));
                        Log.i(TAG, "存储新初始化页数: " + mPageTotal);
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
        String url = "http://www.qiumeimei.com/tag/gif";
        HttpUtils httpUtils = new HttpUtils();
        httpUtils.send(HttpRequest.HttpMethod.GET, url, new RequestCallBack<String>() {
            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                String html = responseInfo.result;
//                Log.i(TAG, "onSuccess: " + html);
                List<String> list = RegularUtils.getUrlfromHtmlContent(html);
                Log.i(TAG, "onSuccess: " + list.size());
            }

            @Override
            public void onFailure(HttpException e, String s) {

            }
        });

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
            //设置标识 pageNum
            Log.i(TAG, "当前页面数共" + mPageUrlList.size() + "页");
            for (int i = 0; i < mPageUrlList.size(); i++) {
                new RunThreadGetImageUrl(mHandler, i, mPageUrlList.get(i), 200).run();
            }
            //存储初始化页数
            ToolsUtils.setString(this, "init", String.valueOf(mPageUrlList.size()));
            Log.i(TAG, "初始化页数记录：" + mPageUrlList.size());
        } else {
            int dbPageNumber = Integer.parseInt(stringValue);
            newPages = (mPageTotal - dbPageNumber) + 1;
            Log.i(TAG, "当前最新页数：" + mPageTotal + " 数据已经记录页数：" + dbPageNumber);
            Log.i(TAG, "数据库已经初始化，追加更新 追加页数 + 1：" + newPages);
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
        btn_to_page = (Button) findViewById(R.id.btn_to_page);
        btn_to_page.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String currentPageNumber = et_page_number.getText().toString();
                int i_currentPageNumber = Integer.parseInt(currentPageNumber) - 1;
                vp.setCurrentItem(i_currentPageNumber, true);
                Log.i(TAG, "转到" + currentPageNumber + "页");
            }
        });
        et_page_number = (EditText) findViewById(R.id.et_page_number);
        vp = (ViewPager) findViewById(R.id.vp);
        //设置viewpager数据适配器
        mAdapter = new MyPagerAdapter();
        vp.setAdapter(mAdapter);
        vp.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                if (position == mImageUrlList.size() - 1 && !isLoading) {
                    Toast.makeText(MainActivity.this, "没有更多数据", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onPageSelected(int position) {
                String currentPageNumber = (position + 1) + "";
                et_page_number.setText(currentPageNumber);
                ToolsUtils.setString(MainActivity.this, CURRENT_PAGE_NUMBER, currentPageNumber);
                Log.i(TAG, "标记为第" + currentPageNumber + "页");
            }

            @Override
            public void onPageScrollStateChanged(int state) {

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
                mProgressDialog = new ProgressDialog(MainActivity.this);
                mProgressDialog.setMessage("数据初始化中");
                mProgressDialog.setMax(mPageTotal);
                mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                mProgressDialog.setCancelable(false);
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
                Log.i(TAG, "onFailure: " + s);
            }
        });
    }
}
