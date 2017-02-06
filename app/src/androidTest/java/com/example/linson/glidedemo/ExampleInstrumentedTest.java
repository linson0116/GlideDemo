package com.example.linson.glidedemo;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

import static android.content.ContentValues.TAG;
import static org.junit.Assert.assertEquals;

/**
 * Instrumentation test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    private List<String> mPageUrlList = new ArrayList<>();
    private List<String> mImageUrlList = new ArrayList<>();

    @Test
    public void useAppContext() throws Exception {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();
        getList();
        assertEquals("com.example.linson.glidedemo", appContext.getPackageName());
    }

    public void getList() {
        for (int i = 1; i <= 10; i++) {
            if (i == 1) {
                mPageUrlList.add("http://www.qiumeimei.com/tag/gif");
            } else {
                mPageUrlList.add("http://www.qiumeimei.com/tag/gif/page/" + i);
            }
        }
        HttpUtils httpUtils = new HttpUtils();
        for (final String url : mPageUrlList) {
            httpUtils.send(HttpRequest.HttpMethod.GET, url, new RequestCallBack<String>() {
                @Override
                public void onSuccess(ResponseInfo<String> responseInfo) {
                    List<String> list = RegularUtils.getUrlfromHtmlContent(responseInfo.result);
                    Log.i(TAG, "onSuccess: " + url + " list " + list.size());
                }

                @Override
                public void onFailure(HttpException e, String s) {
                    Log.i(TAG, "onFailure: ");
                }
            });
        }
    }
}
