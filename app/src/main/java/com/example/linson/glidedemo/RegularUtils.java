package com.example.linson.glidedemo;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by linson on 2017/1/3.
 */

public class RegularUtils {
    private static final String TAG = "content";

    /**
     * 根据网页取得特定url
     * src="http://wx2.sinaimg.cn/mw690/006D2xVlly1fc6dg5lqmog30ak0747wj.gif"
     * src="http://ww1.sinaimg.cn/mw690/a00dfa2agw1fbucdlmqx5g20bo0754qr.gif"
     */
    public static List<String> getUrlfromHtmlContent(String content) {
        List<String> list = new ArrayList<String>();
        String reg = "\\ssrc=[\"|']?(http://w([\\w\\W]?)\\d([\\w\\W]*?)(\\.gif))[\"|']";
        reg = "\\ssrc=[\"|']?(http://w([\\w\\W]?)\\d([\\w\\W]*?)([\\.gif|\\.jpg]))[\"|']";
        Pattern p = Pattern.compile(reg, Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(content);
        while (m.find()) {
            list.add(m.group(1));
        }
        Log.i(TAG, "正则表达取得: size " + list.size());
        return list;
    }

    /**
     * 根据网页取得页面page
     * href='http://www.qiumeimei.com/tag/gif/page/86'
     */
    public static String getPageNumfromHtmlContent(String content) {
        List<String> list = new ArrayList<String>();
        String reg = "href='http://www\\.qiumeimei\\.com/tag/gif/page/\\d+";
        Pattern p = Pattern.compile(reg, Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(content);
        while (m.find()) {
            list.add(m.group(0));
        }
        if (list.size() == 0) {
            return "0";
        } else {
            String url = list.get(list.size() - 1);
            String numPage = url.substring(url.lastIndexOf('/') + 1);
            return numPage;
        }
    }

    /**
     * 去除list中的重复数据
     */
    public static List<String> getSingleList(List list) {
        HashSet set = new HashSet(list);
        List newList = new ArrayList();
        newList.addAll(set);
        return newList;
    }
}

