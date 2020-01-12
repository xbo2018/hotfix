package org.xbo.hotfix;

import net.bytebuddy.agent.builder.AgentBuilder;

public interface PatchGenerator {
    AgentBuilder createPatchBuilder();
}
