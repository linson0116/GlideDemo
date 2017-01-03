package com.example.linson.glidedemo;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by linson on 2017/1/3.
 */

public class RegularUtils {
    /**
     * 根据字符串取得url
     */
    public static List<String> getUrlfromHtmlContent(String content) {
        List<String> list = new ArrayList<String>();
        String reg = "src=[\"|']?(http://ww\\d([\\w\\W]*?)(\\.gif))[\"|']";
        Pattern p = Pattern.compile(reg, Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(content);
        while (m.find()) {
            list.add(m.group(1));
        }
        return list;
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

