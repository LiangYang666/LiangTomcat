package com.liang.server.BIO;
/**
 * @Description: TODO
 * @Author: LiangYang
 * @Date: 2022/4/14
 **/

import com.liang.core.Server;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.*;

public class BIOServer extends Server {

    public BIOServer(int port, String xmlPath) {
        super(port, xmlPath);
    }
    public void start(){
        ServerSocket serverSocket = null;   // 绑定端口，创建监听socket
        try {
            serverSocket = new ServerSocket(port);
            System.out.printf("端口%d 等待连接\n", port);
            ThreadPoolExecutor executors = new ThreadPoolExecutor(100, 100, 5, TimeUnit.SECONDS, new LinkedBlockingQueue<>());
            while (true){
                Socket clientSocket = serverSocket.accept();     // 等待连接，连接到则创建新socket
                executors.submit(() -> {
                    System.out.println(clientSocket);
                    InputStream inputStream;
                    try {
                        inputStream = clientSocket.getInputStream();
                        BIORequest bioRequest = new BIORequest(inputStream);
//                        System.out.println(bioRequest);
                        BIOResponse bioResponse = new BIOResponse(clientSocket.getOutputStream());
                        dispatcher.dispatch(bioRequest, bioResponse);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
