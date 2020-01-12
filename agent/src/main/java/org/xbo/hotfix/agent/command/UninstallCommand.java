package org.xbo.hotfix.agent.command;

import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.agent.builder.ResettableClassFileTransformer;
import org.xbo.hotfix.agent.Installer;
import org.xbo.hotfix.agent.LineArgs;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * @description: 卸载补丁
 * @author: xbo
 * @date: 2020/01/11
 */
@Slf4j
public class UninstallCommand implements Command {
    @Override
    public void execute(Installer.Context context) throws Exception {
        LineArgs lineArgs=context.getLineArgs();
        Set<ResettableClassFileTransformer> transformers=context.getFileTransformerCached().get(lineArgs.getPatch().toString());
        if(transformers!=null&&!transformers.isEmpty()) {
            for (Iterator<ResettableClassFileTransformer> iterator = transformers.iterator(); iterator.hasNext(); ) {
                ResettableClassFileTransformer transformer=iterator.next();
                boolean result=transformer.reset(context.getInstrumentation(), AgentBuilder.RedefinitionStrategy.RETRANSFORMATION);
                log.debug("transformer reset.result:{},path:{}",result,lineArgs.getPatch());
                result = context.getInstrumentation().removeTransformer(transformer);
                log.debug("transformer removed.result:{},path:{}", result, lineArgs.getPatch());
                iterator.remove();
            }
        }
        context.getPatchManager().unload(lineArgs.getPatch());
        context.getFileTransformerCached().remove(lineArgs.getPatch().toString());
    }
}
