package org.xbo.hotfix.agent;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;

@Slf4j
public class PatchManagerTest {

    @org.junit.Test
    public void loadPatch() throws Exception {
        String filePath="patch/target/patch.example-1.0-SNAPSHOT.jar";
        File jarFile=new File(filePath);
        log.info("URI:{}",URI.create(filePath));
        URL file=new URL(jarFile.toURI().toString());
        log.info("url:{},uri:{}",file.toString(),file.toURI().toString());
        URLConnection uc = file.openConnection();
        String pluginurl = "jar:"+jarFile.toURI()+"!/";
        URL jarUrl=new URL(pluginurl);
        log.info("url:{},uri:{}",jarUrl.toString(),jarUrl.toURI().toString());
        uc = file.openConnection();
        log.info(uc.toString());
        PatchManager manager=new PatchManager();
        manager.load(jarUrl);
        log.info(manager.toString());
    }
}