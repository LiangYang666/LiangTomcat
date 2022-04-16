package com.liang;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.List;

/**
 * @Description: TODO
 * @Author: LiangYang
 * @Date: 2022/4/16 下午9:06
 **/
public class XmlHandler {
    private final HashMap<String, HttpServlet> servletMapping = new HashMap<>();
    private final HashMap<String, String> urlMapping = new HashMap<>();

    public XmlHandler(String filePath) {
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
                    HttpServlet HttpServlet = (HttpServlet) Class.forName(className).newInstance();
                    servletMapping.put(servletName, HttpServlet);
                }
            }
        }catch (FileNotFoundException | IllegalAccessException
                | InstantiationException | ClassNotFoundException
                | DocumentException e) {
            e.printStackTrace();
        }
    }

    public HashMap<String, HttpServlet> getServletMapping() {
        return servletMapping;
    }

    public HashMap<String, String> getUrlMapping() {
        return urlMapping;
    }
}
