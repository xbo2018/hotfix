package org.xbo.hotfix.agent;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.xbo.hotfix.PatchGenerator;

import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * @description: 补丁管理器，负责加载、卸载、维护补丁基本信息
 * @author: xbo
 * @date: 2020/01/11
 */
@Slf4j
public class PatchManager {
    private HashMap<String,PatchInfo> patchMap=new HashMap<>();
    public ClassLoader load(URL jarUrl) {
        ClassLoader loader=null;
        PatchInfo info=patchMap.get(jarUrl.toString());
        if (info == null) {
            loader = new PatchClassLoader(new URL[]{jarUrl});
            info=new PatchInfo(jarUrl,loader);
            patchMap.put(jarUrl.toString(), info);
            Installer.SystemHook.registerClassLoader(jarUrl.toString(), loader);
        }else {
            loader=info.getClassLoader();
        }
        return loader;
    }
    public void unload(URL jarUrl) throws IOException {
        PatchInfo info=patchMap.get(jarUrl.toString());
        if(info!=null){
            ClassLoader loader =info.getClassLoader();
            if (loader != null) {
                Installer.SystemHook.unregisterClassLoader(jarUrl.toString());
                if(loader instanceof Closeable) {
                    ((Closeable)loader).close();
                }
            }
            patchMap.remove(jarUrl.toString());
        }
    }
    public Collection<PatchInfo> list(){
        return Collections.unmodifiableCollection(patchMap.values());
    }
    public Collection<Class<?>> findPatchClasses(URL jarUrl) throws IOException {
        ClassLoader classLoader = load(jarUrl);
        JarURLConnection juc = (JarURLConnection) jarUrl.openConnection();
        Set<Class<?>> classes = new LinkedHashSet<Class<?>>();
        //final String classNameSuffix = "PatchGenerator";
        JarFile jarFile = null;
        try {
            jarFile = juc.getJarFile();
            Enumeration<JarEntry> es = jarFile.entries();
            while (es.hasMoreElements()) {
                JarEntry jarEntry = (JarEntry) es.nextElement();
                String name = jarEntry.getName();
                if (name != null && name.endsWith(".class")) {//只解析.class文件
                    String className = name.replace("/", ".").substring(0, name.length() - 6);
                    //if (!className.endsWith(classNameSuffix)) continue;
                    //默认去系统已经定义的路径查找对象，针对外部jar包不能用
                    Class<?> c = Class.forName(className, true, classLoader);//自己定义的loader路径可以找到
                    if (c.isInterface() || Modifier.isAbstract(c.getModifiers())) continue; //排除接口类
                    if (PatchGenerator.class.isAssignableFrom(c)) {
                        log.info("found patch class.name:{}", c);
                        classes.add(c);
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("read patch class error.", e);
        } finally {
            if (jarFile != null) {
                jarFile.close();
            }
        }
        return classes;
    }
    @Getter
    public static class PatchInfo{
        private Date loadTime;
        private URL jarUrl;
        private ClassLoader classLoader;
        public PatchInfo(URL jarUrl,ClassLoader classLoader){
            this.jarUrl=jarUrl;
            this.classLoader=classLoader;
            this.loadTime=new Date();
        }
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof PatchInfo)) return false;

            PatchInfo patchInfo = (PatchInfo) o;

            return jarUrl.equals(patchInfo.jarUrl);
        }
        @Override
        public int hashCode() {
            return jarUrl.hashCode();
        }
    }
}
