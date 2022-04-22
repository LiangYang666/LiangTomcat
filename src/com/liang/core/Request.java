package com.liang.core;

import java.io.IOException;
import java.io.InputStream;

/**
 * @Description: TODO
 * @Author: LiangYang
 * @Date: 2022/4/18 下午8:10
 **/
public abstract class Request {
    public String method;
    public String url;

    public String getMethod() {
        return method;
    }

    public String getUrl() {
        return url;
    }

    @Override
    public String toString() {
        return "Request{" +
                "method='" + method + '\'' +
                ", url='" + url + '\'' +
                '}';
    }
}
