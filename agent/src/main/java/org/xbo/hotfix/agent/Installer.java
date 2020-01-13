package org.xbo.hotfix.agent;

import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.agent.ByteBuddyAgent;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.agent.builder.ResettableClassFileTransformer;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.matcher.ElementMatchers;
import org.xbo.hotfix.agent.command.Command;
import org.xbo.hotfix.agent.command.Context;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @description:
 * @author: xbo
 * @date: 2020/01/11
 */
@Slf4j
public class Installer {
    public static void main(String[] args) throws Exception {
        LineArgs lineArgs = LineArgs.parse(args);
        if(lineArgs.isHelp()) {
            return;
        }
        String argument = lineArgs.toString();
        log.info("begging install agent.args:{}", argument);
        ByteBuddyAgent.attach(new File(Installer.class.getProtectionDomain().getCodeSource().getLocation().getPath()), lineArgs.getPid(), argument);
        log.info("agent install finished.");
    }
    public static void premain(String argument, Instrumentation inst) throws Exception {
        agentmain(argument, inst);
    }
    public static void agentmain(String argument, Instrumentation inst) throws Exception {
        log.info("loading agent,args:{}", argument);
        log.info("agent classloader:{}", Installer.class.getClassLoader());
        LineArgs lineArgs = LineArgs.parse(argument);
        Installer.install(lineArgs, inst);
    }
    static void install(LineArgs lineArgs, Instrumentation instrumentation) throws Exception {
        log.debug("instrumentation old:{},new:{}", Installer.instrumentation, instrumentation);
        if (Installer.instrumentation == null) {
            Installer.instrumentation = instrumentation;
            context.setInstrumentation(Installer.instrumentation);
            installSystemHook();
        }
        log.info("context.instrumentation:{}", context.getInstrumentation());
        context.setLineArgs(lineArgs);
        Command cmd = lineArgs.getCmd().generateCommand();
        log.info("patch begging to {}.", lineArgs.getCmd().name().toLowerCase());
        cmd.execute(context);
        log.info("{} patch finished.", lineArgs.getCmd().name().toLowerCase());
    }
    public static void installSystemHook() {
        SystemHook.install(Installer.instrumentation);
    }
    public static void uninstallSystemHook() {
        SystemHook.uninstall(Installer.instrumentation);
        Installer.instrumentation=null;
    }
    static volatile Context context;
    static volatile Instrumentation instrumentation;
    static {
        context = new Context();
        context.setFileTransformerCached(new HashMap<>());
        context.setPatchManager(new PatchManager());
    }

    /**
     * @description:
     * @author: xbo
     * @date: 2020/01/11 18:39
     */
    @Slf4j
    static class SystemHook {
        static volatile ResettableClassFileTransformer transformer = null;
        public static void install(Instrumentation inst) {
            transformer = new AgentBuilder.Default()
                    // by default, JVM classes are not instrumented
                    .ignore(ElementMatchers.none().or(ElementMatchers.nameStartsWith("net.bytebuddy.")))
                    .disableClassFormatChanges()
                    .with(AgentBuilder.RedefinitionStrategy.RETRANSFORMATION)
                    .with(AgentBuilder.Listener.StreamWriting.toSystemOut().withErrorsOnly())
                    .type(ElementMatchers.is(ClassLoader.getSystemClassLoader().getClass()))
                    .transform((builder, type, loader, module) -> builder
                            .visit(Advice
                                    .to(SystemHook.class)
                                    .on(ElementMatchers.named("loadClass")
                                            .and(ElementMatchers.returns(Class.class))
                                            //.and(ElementMatchers.isPublic())
                                            .and(ElementMatchers.takesArguments(String.class, boolean.class))
                                    )
                            ))
                    .installOn(inst);
            log.info("install system classloader hook done.");
        }
        public static void uninstall(Instrumentation inst) {
            boolean result = transformer.reset(inst, AgentBuilder.RedefinitionStrategy.RETRANSFORMATION);
            log.debug("system transformer reset.result:{}", result);
            result = inst.removeTransformer(transformer);
            log.debug("system transformer removed.result:{}", result);
        }
        @Advice.OnMethodExit(onThrowable = ClassNotFoundException.class)
        static void onLoadClassExit(@Advice.This ClassLoader loader,
                                    @Advice.Argument(0) String name,
                                    @Advice.Return(readOnly = false) Class<?> returned,
                                    @Advice.Thrown(readOnly = false) Throwable thrown) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
            //System.out.println("classLoader:"+loader+" name:"+name+" returned:"+returned+" throwable:"+(thrown==null?"null":thrown));
                /*log.info("invoke loadClass.isError:{},loader:{},name:{},result:{}",ex!=null,loader,name,result);
                if(ex!=null){
                    log.error("load {} class error.",name,ex);
                }*/
            if (returned == null && loader == ClassLoader.getSystemClassLoader()) {
                //System.out.println("classLoader:"+loader+" name:"+name+" returned:"+returned+" throwable:"+(thrown==null?"null":thrown));
                Class<?> clazz = Class.forName("org.xbo.hotfix.agent.Installer$SystemHook",
                        true,
                        ClassLoader.getSystemClassLoader());
                Method method = clazz.getDeclaredMethod("findClass", ClassLoader.class, String.class);
                method.setAccessible(true);
                returned = (Class<?>) method.invoke(null, loader, name);
                if (returned != null) {
                    thrown = null;//找到相应类时忽略ClassNotFoundException
                }
            }
        }

        static Class<?> findClass(ClassLoader loader, String name) {
            Map<String, AtomicInteger> counterMap = Collections.synchronizedMap(classNameCounter);
            AtomicInteger counter = counterMap.get(name);
            if (counter == null) {
                counter = new AtomicInteger(0);
                counterMap.put(name, counter);
            }
            //防止递归死循环
            if (counter.addAndGet(1) > 1) {
                log.debug("class not found. name:" + name);
                return null;
            }
            log.debug("caller ****** {}", name);
            Class<?> returned = null;
            for (ClassLoader cl : classLoaderMap.values()) {
                try {
                    if (loader == cl) continue;
                    returned = cl.loadClass(name);
                } catch (ClassNotFoundException e) {
                    // ClassNotFoundException thrown if class not found
                    // from the non-null other class loader
                    //printException(e);
                }
                log.debug("sub classLoader:" + cl + " name:" + name + " returned:" + returned);
                if (returned != null) {
                    counterMap.remove(name);
                    return returned;
                }
            }
            return null;
        }

        static Map<String, AtomicInteger> classNameCounter = new WeakHashMap<>();
        static Map<String, ClassLoader> classLoaderMap = new HashMap<>();

        public static void registerClassLoader(String name, ClassLoader classLoader) {
            classLoaderMap.put(name, classLoader);
        }

        public static void unregisterClassLoader(String name) {
            classLoaderMap.remove(name);
        }

        static void printException(Throwable e) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);// stack trace as a string
            System.out.println("error-----------" + sw.toString());
        }
        static interface FindClassHandler {
            Class<?> handle(ClassLoader loader, String name, Class<?> returned, Throwable thrown) throws ClassNotFoundException;
        }
    }

}
