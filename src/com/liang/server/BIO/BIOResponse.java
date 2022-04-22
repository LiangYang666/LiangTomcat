package com.liang.server.BIO;

import com.liang.core.Response;

import java.io.*;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;

/**
 * @Description: TODO
 * @Author: LiangYang
 * @Date: 2022/4/16 上午10:13
 **/
public class BIOResponse extends Response {
    private OutputStream outputStream;


    public BIOResponse(OutputStream outputStream) {
        this.outputStream = outputStream;
    }

    public void write(String content) {
        StringBuffer httpResponse = new StringBuffer();
        httpResponse.append("HTTP/1.1 200 OK\n")
                .append("Content-Type: text/html; charset=utf-8\n")
                .append("Content-Length: ").append(content.getBytes().length).append("\n")
                .append("\r\n")
                .append(content).append("\n");
        try {
            outputStream.write(httpResponse.toString().getBytes());
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void write(File file) {
        StringBuffer httpResponse = new StringBuffer();
        long fileLength = file.length();
        httpResponse.append("HTTP/1.1 200 OK\n")
                .append("Content-Length: ").append(fileLength).append("\n")
                .append("\r\n");
        byte[] bytes = new byte[1024];
        if (file.exists()) {
            try {
                outputStream.write(httpResponse.toString().getBytes());
                FileInputStream fileInputStream = new FileInputStream(file);
                int length;
                while ((length=fileInputStream.read(bytes)) != -1) {
                    outputStream.write(bytes, 0, length);
                }
                fileInputStream.close();
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
