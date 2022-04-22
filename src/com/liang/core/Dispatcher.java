package com.liang.core;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.List;

/**
 * @Description: TODO
 * @Author: LiangYang
 * @Date: 2022/4/16 下午9:06
 **/
public class Dispatcher {
    private final HashMap<String, Servlet> servletMapping = new HashMap<>();
    private final HashMap<String, String> urlMapping = new HashMap<>();

    public Dispatcher(String filePath) {
        try {
            FileInputStream in = new FileInputStream(filePath);
            SAXReader sr = new SAXReader();
            Document doc = sr.read(in);
            List<Element> elements = doc.getRootElement().elements();
            for (Element element : elements) {
                if (element.getName().equals("servlet-mapping")) {
                    String servletName = element.elementTextTrim("servlet-name");
                    String url = element.elementTextTrim("url-pattern");
                    urlMapping.put(url, servletName);
                } else if (element.getName().equals("servlet")) {
                    String servletName = element.elementTextTrim("servlet-name");
                    String className = element.elementTextTrim("servlet-class");
                    Servlet HttpServlet = (Servlet) Class.forName(className).newInstance();
                    servletMapping.put(servletName, HttpServlet);
                }
            }
        }catch (FileNotFoundException | IllegalAccessException
                | InstantiationException | ClassNotFoundException
                | DocumentException e) {
            e.printStackTrace();
        }
    }
    public void dispatch(Request request, Response response){
        String servletName = urlMapping.get(request.getUrl());
        Servlet httpServlet = servletMapping.get(servletName);
        if(httpServlet != null)
            httpServlet.service(request, response);
        else
            ResourceServlet.getResourceServlet().service(request, response);
    }
}
