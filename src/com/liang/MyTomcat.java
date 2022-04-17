package com.liang;
/**
 * @Description: TODO
 * @Author: LiangYang
 * @Date: 2022/4/14
 **/

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Scanner;

public class MyTomcat {
    private final int port;
    private final HashMap<String, HttpServlet> servletMapping;
    private final HashMap<String, String> urlMapping;

    public static void main(String[] args) throws IOException {
        MyTomcat myTomcat = new MyTomcat(8099);
        myTomcat.start();
    }

    public MyTomcat(int port) {
        this.port = port;
        XmlHandler xmlHandler = new XmlHandler("web.xml");
        servletMapping = xmlHandler.getServletMapping();
        urlMapping = xmlHandler.getUrlMapping();
    }
    public void start(){
        ServerSocket serverSocket = null;   // 绑定端口，创建监听socket
        try {
            serverSocket = new ServerSocket(port);
            while (true){
                System.out.printf("端口%d 等待连接\n", port);
                Socket clientSocket = serverSocket.accept();     // 等待连接，连接到则创建新socket
                new Thread(() -> {  //  创建线程连接
                    System.out.println(clientSocket);
                    InputStream inputStream = null;
                    try {
                        inputStream = clientSocket.getInputStream();
                        Request request = new Request(inputStream);
                        System.out.println(request);
                        Response response = new Response(clientSocket.getOutputStream());
                        dispatch(request, response);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void dispatch(Request request, Response response){
        String servletName = urlMapping.get(request.getUrl());
        HttpServlet httpServlet = servletMapping.get(servletName);
        if(httpServlet != null)
            httpServlet.service(request, response);
    }
}
