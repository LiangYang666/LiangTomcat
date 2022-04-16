package com.liang;

import java.io.IOException;

/**
 * @Description: TODO
 * @Author: LiangYang
 * @Date: 2022/4/16 下午9:45
 **/
public class HelloServlet extends HttpServlet {
    @Override
    public void doGet(Request request, Response response) {
        try {
            response.write(request.getUrl()+"--- Hello Servlet！");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void doPost(Request request, Response response) {

    }
}
