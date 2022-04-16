package com.liang;

import java.io.IOException;
import java.io.InputStream;

/**
 * @Description: TODO
 * @Author: LiangYang
 * @Date: 2022/4/15 下午10:56
 **/
public class Request {
    private String method;
    private String url;
    public Request(InputStream inputStream) throws IOException {
        byte[] bytes = new byte[1024];
        int length = inputStream.read(bytes);
        String requestStr = new String(bytes, 0, length);
        String requestHead = requestStr.split("\n")[0].trim();
        String[] heads = requestHead.split("\\s+");
        method = heads[0];
        url = heads[1];
    }

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
