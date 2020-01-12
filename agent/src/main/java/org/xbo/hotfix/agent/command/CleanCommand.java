package org.xbo.hotfix.agent.command;

import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.agent.builder.ResettableClassFileTransformer;
import org.xbo.hotfix.agent.Installer;

import java.net.URL;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * @description: 卸载所有补丁
 * @author: xbo
 * @date: 2020/01/11 21:48
 */
@Slf4j
public class CleanCommand implements Command {
    @Override
    public void execute(Installer.Context context) throws Exception {
        Map<String, Set<ResettableClassFileTransformer>> map=context.getFileTransformerCached();
        for (Map.Entry<String, Set<ResettableClassFileTransformer>> entry : map.entrySet()) {
            String key = entry.getKey();
            Set<ResettableClassFileTransformer> transformers = entry.getValue();
            for (Iterator<ResettableClassFileTransformer> iterator = transformers.iterator(); iterator.hasNext(); ) {
                ResettableClassFileTransformer transformer = iterator.next();
                boolean result = transformer.reset(context.getInstrumentation(), AgentBuilder.RedefinitionStrategy.RETRANSFORMATION);
                log.debug("transformer reset.result:{}", result);
                result = context.getInstrumentation().removeTransformer(transformer);
                log.debug("transformer removed.result:{}", result);
                iterator.remove();
            }
            context.getPatchManager().unload(new URL(key));
            context.getFileTransformerCached().remove(key);
            log.info("patch cleaned.({})",key);
        }
        map.clear();
        Installer.uninstallSystemHook();
    }
}
