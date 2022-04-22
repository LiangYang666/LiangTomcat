package com.liang.core;

import java.io.File;

/**
 * @Description: TODO
 * @Author: LiangYang
 * @Date: 2022/4/19 上午11:20
 **/
public class ResourceServlet extends Servlet {
    private static volatile ResourceServlet resourceServlet = null;
    public static ResourceServlet getResourceServlet(){
        if (resourceServlet==null){
            synchronized (ResourceServlet.class){
                if (resourceServlet==null){
                    resourceServlet = new ResourceServlet();
                }
            }
        }
        return resourceServlet;
    }
    @Override
    public void doGet(Request request, Response response) {
        String url = request.getUrl();
        String path = "resources"+url;
        File file = new File(path);
        if (file.isHidden() || !file.exists()) {
            response.write("<h1>404 Not Found</h1>");
            return;
        }
        if (!file.isFile()) {
            response.write("<h1>404 Not Found</h1>");
            return;
        }
        response.write(file);
    }

    @Override
    public void doPost(Request request, Response response) {

    }
}
