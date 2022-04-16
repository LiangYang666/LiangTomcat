package com.liang;

import java.io.IOException;
import java.io.OutputStream;

/**
 * @Description: TODO
 * @Author: LiangYang
 * @Date: 2022/4/16 上午10:13
 **/
public class Response {
    private  OutputStream outputStream;

    public Response(OutputStream outputStream) {
        this.outputStream = outputStream;
    }

    public void write(String content) throws IOException {
        StringBuffer httpResponse = new StringBuffer();
        httpResponse.append("HTTP/1.1 200 OK\n")
                .append("Content-Type: text/html; charset=utf-8\n")
                .append("\r\n")
                .append("<html><body>\n")
                .append(content).append("\n")
                .append("</body></html>\n");
        outputStream.write(httpResponse.toString().getBytes());
        outputStream.close();
    }
}
