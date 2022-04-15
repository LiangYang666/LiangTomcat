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
import java.util.Scanner;

public class MyTomcat {
    public static void main(String[] args) {
        int port = 8099;
        try {
            ServerSocket serverSocket = new ServerSocket(port);   // 绑定端口，创建监听socket
            System.out.printf("端口%d 等待连接\n", port);
            Socket clientSocket = serverSocket.accept();     // 等待连接，连接到则创建新socket
            System.out.println(clientSocket);
            InputStream inputStream = clientSocket.getInputStream();
            byte[] data = new byte[1024];
            int length = inputStream.read(data);
            String s = new String(data, 0, length);
            System.out.println("+++++++++++++++++HTTP Request+++++++++++++++++++++++++");
            System.out.println(s);
            System.out.println("++++++++++++++++++++++++++++++++++++++++++");
            System.out.println("回车以关闭客户端连接socket");
            Scanner sc = new Scanner(System.in);
            sc.nextLine();
            System.out.println("关闭客户端连接socket");
            clientSocket.close();
            System.out.println("回车以关闭监听socket");
            sc.nextLine();
            serverSocket.close();
            System.out.println("关闭监听socket");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
