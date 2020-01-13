package org.xbo.hotfix.agent.command;


public interface Command {
    void execute(Context context) throws Exception;
}
