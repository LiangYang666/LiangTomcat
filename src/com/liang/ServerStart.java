package com.liang;

import com.liang.core.Server;
import com.liang.server.BIO.BIOServer;
import com.liang.server.Netty.NettyServer;
import com.liang.server.NewIO.NIOServer;

/**
 * @Description: TODO
 * @Author: LiangYang
 * @Date: 2022/4/18 下午10:40
 **/
public class ServerStart {
    public static void main(String[] args) {
        Server server = new BIOServer(8099, "web.xml");
//        Server server = new NIOServer(8099, "web.xml");
//        Server server = new NettyServer(8099, "web.xml");
        server.start();
    }
}
