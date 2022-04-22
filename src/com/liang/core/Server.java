package com.liang.core;

/**
 * @Description: TODO
 * @Author: LiangYang
 * @Date: 2022/4/18 下午10:33
 **/
public abstract class Server {
    public Dispatcher dispatcher;
    public int port;

    public Server(int port, String xmlPath) {
        this.dispatcher = new Dispatcher(xmlPath);
        this.port = port;
    }
    public abstract void start();
}
