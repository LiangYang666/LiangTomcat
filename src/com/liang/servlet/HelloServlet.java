package com.liang.servlet;

import com.liang.core.Request;
import com.liang.core.Response;
import com.liang.core.Servlet;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @Description: TODO
 * @Author: LiangYang
 * @Date: 2022/4/16 下午9:45
 **/
public class HelloServlet extends Servlet {
    @Override
    public void doGet(Request request, Response response) {
        Date date = new Date();
        StringBuffer htmlContent = new StringBuffer();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateStr = sdf.format(date);
        String content = dateStr+"测试 - "+request.getUrl()+"--- Hello Servlet！";
        htmlContent.append("<html><body>\n")
                .append(content).append("\n")
                .append("</body></html>\n");
        response.write(htmlContent.toString());
    }

    @Override
    public void doPost(Request request, Response response) {

    }
}
