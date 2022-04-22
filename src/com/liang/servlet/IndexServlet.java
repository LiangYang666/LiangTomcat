package com.liang.servlet;

import com.liang.core.Servlet;
import com.liang.core.Request;
import com.liang.core.Response;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @Description: TODO
 * @Author: LiangYang
 * @Date: 2022/4/16 下午9:45
 **/
public class IndexServlet extends Servlet {
    @Override
    public void doGet(Request request, Response response) {
        File file = new File("resources/index.html");
        response.write(file);
    }

    @Override
    public void doPost(Request request, Response response) {

    }
}
