package org.xbo.hotfix.demo.web;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.CommandLineRunner;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;

@RestController
@Slf4j
public class HelloControll {
    static final Logger logger = LoggerFactory.getLogger("abc");
    @RequestMapping(value = "/api/hello")
    public Map hello(String name){
        HashMap<Object,Object> map = new HashMap<Object,Object>();
        try {
            log.debug("数据查询成功！");
            map.put("success",true);
            map.put("result",name);
            logger.debug("数据查询完成，默认不显示这条日志。补丁修改log日志级别，显示该条日志。");
            logger.info("name:{}",name);
            return map;
        }catch (Exception e){
            e.printStackTrace();
            map.put("success",false);
            map.put("message","数据查询失败！");
            log.debug("数据查询失败！");
            return map;
        }
    }
}
