package org.xbo.hotfix.agent.command;

public enum CommandLineEnum {
    APPLY{
        public Command generateCommand(){
            return new ApplyCommand();
        }
    }
    ,UNINSTALL{
        public Command generateCommand(){
            return new UninstallCommand();
        }
    },CLEAN{
        @Override
        public Command generateCommand() {
            return new CleanCommand();
        }
    },LIST{
        @Override
        public Command generateCommand() {
            return new ListCommand();
        }
    };
    public abstract Command generateCommand();
}
