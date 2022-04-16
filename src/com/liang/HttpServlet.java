package com.liang;

/**
 * @Description: TODO
 * @Author: LiangYang
 * @Date: 2022/4/16 下午9:41
 **/
public abstract class HttpServlet {
    public abstract void doGet(Request request, Response response);
    public abstract void doPost(Request request, Response response);
    public void service(Request request, Response response){
        if(request.getMethod().equalsIgnoreCase("GET")){
            doGet(request, response);
        } else if(request.getMethod().equalsIgnoreCase("POST")){
            doPost(request, response);
        }
    }
}
