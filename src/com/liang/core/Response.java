package com.liang.core;

import java.io.File;
import java.io.IOException;

/**
 * @Description: TODO
 * @Author: LiangYang
 * @Date: 2022/4/18 下午8:11
 **/
public abstract class Response {
    public abstract void write(String content);
    public abstract void write(File file);
}
