package org.xbo.hotfix.agent.command;

import lombok.Data;
import net.bytebuddy.agent.builder.ResettableClassFileTransformer;
import org.xbo.hotfix.agent.LineArgs;
import org.xbo.hotfix.agent.PatchManager;

import java.lang.instrument.Instrumentation;
import java.util.Map;
import java.util.Set;

@Data
public class Context {
    private PatchManager patchManager;
    private Map<String, Set<ResettableClassFileTransformer>> fileTransformerCached;
    private LineArgs lineArgs;
    private Instrumentation instrumentation;
}
