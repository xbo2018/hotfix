package org.xbo.hotfix.patch.example;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.matcher.ElementMatchers;
import org.slf4j.LoggerFactory;
import org.xbo.hotfix.PatchGenerator;

import java.lang.reflect.Method;

@Slf4j
public class HelloControllPatchGenerator implements PatchGenerator {
    public AgentBuilder createPatchBuilder() {
        AgentBuilder agentBuilder = new AgentBuilder.Default()
                // by default, JVM classes are not instrumented
                .ignore(ElementMatchers.none())
                .disableClassFormatChanges()
                .with(AgentBuilder.RedefinitionStrategy.RETRANSFORMATION)
                .with(AgentBuilder.Listener.StreamWriting.toSystemOut().withErrorsOnly())
                .type(ElementMatchers.nameStartsWith("org.xbo.hotfix.demo.web.HelloControll"))
                .transform((builder, type, loader, module) -> builder
                       .visit(Advice
                                .to(HelloControllPatchGenerator.class)
                                .on(ElementMatchers.named("hello"))
                        )
                );
        return agentBuilder;
    }

    /*
     * @This 调用对象
     * @Argument 方法参数(只能选择一个)
     * @Arguments 方法所有参数
     * @Origin 原始调用方法
     * @SuperCall 回调方法
     * */
    @Advice.OnMethodEnter//inline默认true,即把方法体内容复制到原方法里面
    private static void onHelloEnter(@Advice.This Object self, @Advice.Origin Method method,
                                  @Advice.Argument(value = 0,readOnly = false) String name){
        System.out.println("begin call hello");
        name="hello,"+name;
        System.out.println("new name x:" + name);
        HelloPatch patch=new HelloPatch();
        patch.changeLogLevel(name);
    }

    /**
     * @description:
     * @author: xbo
     * @date: 2020/01/12 16:17
     */
    @Slf4j
    public static class HelloPatch {
        public void changeLogLevel(String name) {
            log.info("name:{}", name);
            log.info("哼哈二将");
            System.out.println("okkkkkkkkkkkkkkkkkkkkk");
            LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
            loggerContext.getLogger("abc").setLevel(Level.DEBUG);
        }
    }
}
