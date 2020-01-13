package org.xbo.hotfix.agent.command;

import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.agent.builder.ResettableClassFileTransformer;
import org.xbo.hotfix.PatchGenerator;
import org.xbo.hotfix.agent.LineArgs;

import java.util.*;

/**
 * @description: 打补丁
 * @author: xbo
 * @date: 2020/01/11
 */
@Slf4j
public class ApplyCommand extends UninstallCommand {
    @Override
    public void execute(Context context) throws Exception {
        super.execute(context);
        LineArgs lineArgs=context.getLineArgs();
        Set<ResettableClassFileTransformer> transformers=context.getFileTransformerCached().get(lineArgs.getPatch().toString());
        if(transformers==null){
            transformers=new LinkedHashSet<>();
            context.getFileTransformerCached().put(lineArgs.getPatch().toString(),transformers);
        }
        Collection<Class<?>> classes= context.getPatchManager().findPatchClasses(lineArgs.getPatch());
        for (Class<?> item:classes) {
            PatchGenerator generator= (PatchGenerator) item.newInstance();
            AgentBuilder builder= generator.createPatchBuilder();
            ResettableClassFileTransformer transformer=builder.installOn(context.getInstrumentation());
            transformers.add(transformer);
            log.info("patch class instance : {}",generator.toString());
        }
    }
}
