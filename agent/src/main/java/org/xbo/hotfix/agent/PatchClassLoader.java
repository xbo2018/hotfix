package org.xbo.hotfix.agent;

import sun.misc.ClassLoaderUtil;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * @description: 补丁加载器
 * @author: xbo
 * @date: 2020/01/11 18:41
 */
public class PatchClassLoader extends URLClassLoader {
    public PatchClassLoader(URL[] urls, ClassLoader parent) {
        super(urls, parent);
    }
    public PatchClassLoader(URL[] urls) {
        super(urls);
    }
    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        if(isClosed){return null;}
        //防止重复加载
        Class<?> c = findLoadedClass(name);
        if (c == null) {
            return super.findClass(name);
        }else {
            return c;
        }
    }
    volatile boolean isClosed=false;
    @Override
    public void close() throws IOException {
        isClosed=true;
        super.close();
        ClassLoaderUtil.releaseLoader(this);
    }
}
