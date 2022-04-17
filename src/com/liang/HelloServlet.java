package com.liang;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @Description: TODO
 * @Author: LiangYang
 * @Date: 2022/4/16 下午9:45
 **/
public class HelloServlet extends HttpServlet {
    @Override
    public void doGet(Request request, Response response) {
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateStr = sdf.format(date);
        try {
            response.write(dateStr+" - "+request.getUrl()+"--- Hello Servlet！");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void doPost(Request request, Response response) {

    }
}
