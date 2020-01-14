# 简介
java线上系统出现故障，怎么办怎么办怎么办，当然是修改bug重新上线，对于没有高可用系统架构来说，重新上线意味着系统暂停服务，有时候我们不希望系统停止服务，那怕是一秒。有没有办法不暂停系统服务给系统打个补丁就能修复故障呢，hotfix 就是解决类似场景的工具。hotfix目标是**不重启或暂停系统服务**的情况，动态给系统打补丁，对目标程序也**没有依赖**。

# 用法

1. 编写补丁

   - 导入hotfix补丁接口maven依赖。（当前hotfix没有放到公共仓库，需要使用maven安装到本地）

     ```
     <dependency>
       <groupId>org.xbo.hotfix</groupId>
       <artifactId>hotfix-patch</artifactId>
       <version>${hotfix.version}</version>
     </dependency>
     ```

   - 实现接口PatchGenerator，返回AgentBuilder。样例：

     ```
     public class HelloControllPatchGenerator implements PatchGenerator {
         public AgentBuilder createPatchBuilder() {
             AgentBuilder agentBuilder = new AgentBuilder.Default()
                     // by default, JVM classes are not instrumented
                     .ignore(ElementMatchers.none())
                     .disableClassFormatChanges()
                     .with(AgentBuilder.RedefinitionStrategy.RETRANSFORMATION)
                      //安装补丁的错误信息打印到控制台
                     .with(AgentBuilder.Listener.StreamWriting.toSystemOut().withErrorsOnly())
                      //给org.xbo.hotfix.demo.web.HelloControll打补丁
                     .type(ElementMatchers.nameStartsWith("org.xbo.hotfix.demo.web.HelloControll"))
                     .transform((builder, type, loader, module) -> builder
                            .visit(Advice
                                     //补丁实现类
                                     .to(HelloControllPatchGenerator.class)
                                     //哪个方法要打补丁
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
             //修改参数
             name="hello,"+name;
             System.out.println("new name :" + name);
             LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
             //原abc日志级别重置DEBUG级别
             loggerContext.getLogger("abc").setLevel(Level.DEBUG);
         }
     }
     ```

   - 编译补丁并打包jar

2. 应用补丁

   - 编译hotfix-agent并随同补丁部署到服务器中

   - 获取待带补丁的java目标进程PID

   - 执行命令：

     ```
     java -jar hotfix-agent-0.3-SNAPSHOT.jar -patch "patch-example-1.0-SNAPSHOT.jar" -pid 10196
     ```

3. 查看应用的补丁

   - 执行命令：

     ```
     java -jar hotfix-agent-0.3-SNAPSHOT.jar -patch "patch-example-1.0-SNAPSHOT.jar" -pid 10196 -cmd list
     ```

   - 观察目标进程运行日志，可以看到如下内容：

     ```
     ****************************************PATCH LIST****************************************
       load time  				url
       2020-01-13 22:23:21  		jar:file:/hotfix/example/patch-example-1.0-SNAPSHOT.jar!/
     ****************************************PATCH LIST****************************************
     ```

4. 卸载补丁，执行命令：

   ```
   java -jar hotfix-agent-0.3-SNAPSHOT.jar -patch "patch-example-1.0-SNAPSHOT.jar" -pid 10196 -cmd uninstall
   ```

5. 卸载所有补丁，执行命令：

   ```
   java -jar hotfix-agent-0.3-SNAPSHOT.jar -patch "patch-example-1.0-SNAPSHOT.jar" -pid 10196 -cmd clean
   ```

# 依赖

工具与补丁依赖bytebuddy

```
<properties>
    <byte.buddy.version>1.10.6</byte.buddy.version>
  </properties>
<dependency>
  <groupId>net.bytebuddy</groupId>
  <artifactId>byte-buddy</artifactId>
  <version>${byte.buddy.version}</version>
</dependency>
<dependency>
  <groupId>net.bytebuddy</groupId>
  <artifactId>byte-buddy-agent</artifactId>
  <version>${byte.buddy.version}</version>
</dependency>
```

# 架构



# 原理

