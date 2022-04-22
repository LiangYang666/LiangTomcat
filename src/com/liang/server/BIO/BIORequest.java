package com.liang.server.BIO;

import com.liang.core.Request;

import java.io.IOException;
import java.io.InputStream;

/**
 * @Description: TODO
 * @Author: LiangYang
 * @Date: 2022/4/15 下午10:56
 **/
public class BIORequest extends Request {

    public BIORequest(InputStream inputStream) throws IOException {
        byte[] bytes = new byte[1024];
        int length = inputStream.read(bytes);
        String requestStr = new String(bytes, 0, length);
        String requestHead = requestStr.split("\n")[0].trim();
        String[] heads = requestHead.split("\\s+");
        method = heads[0];
        url = heads[1];
    }
}
