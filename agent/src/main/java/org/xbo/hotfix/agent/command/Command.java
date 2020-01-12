package org.xbo.hotfix.agent.command;

import org.xbo.hotfix.agent.Installer;

public interface Command {
    void execute(Installer.Context context) throws Exception;
}
