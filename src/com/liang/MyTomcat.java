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
    public static void main(String[] args) {
        int port = 8099;
        try {
            XmlHandler xmlHandler = new XmlHandler("web.xml");
            HashMap<String, HttpServlet> servletMapping = xmlHandler.getServletMapping();
            HashMap<String, String> urlMapping = xmlHandler.getUrlMapping();
            ServerSocket serverSocket = new ServerSocket(port);   // 绑定端口，创建监听socket
            System.out.printf("端口%d 等待连接\n", port);
            Socket clientSocket = serverSocket.accept();     // 等待连接，连接到则创建新socket
            System.out.println(clientSocket);
            InputStream inputStream = clientSocket.getInputStream();
            Request request = new Request(inputStream);
            System.out.println(request);
            Response response = new Response(clientSocket.getOutputStream());

            String servletName = urlMapping.get(request.getUrl());
            HttpServlet httpServlet = servletMapping.get(servletName);
            httpServlet.service(request, response);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
