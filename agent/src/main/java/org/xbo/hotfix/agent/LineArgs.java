package org.xbo.hotfix.agent;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.converters.EnumConverter;
import com.beust.jcommander.converters.URLConverter;
import lombok.Data;
import lombok.SneakyThrows;
import org.xbo.hotfix.agent.command.CommandLineEnum;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringJoiner;

/**
 * @description: 命令行参数
 * @author: xbo
 * @date: 2020/01/11 19:20
 */
@Data
public class LineArgs {
    @Parameter(names = {"-pid", "pid"}, description = "id of java process", required = true,order = 1,help = true)
    private String pid;
    @Parameter(names = {"-patch", "patch"}, description = "patch file path of jar.",order = 2,converter = JarUrlConverter.class)
    private URL patch;
    @Parameter(names = {"-cmd", "cmd"}, description = "patch action.when it's apply or uninstall,patch parameter is required.",order = 3,converter = CommandLineEnumConverter.class)
    private CommandLineEnum cmd = CommandLineEnum.APPLY;
    @Parameter(names = {"--help","-h"}, help = true,description = "show usage.for example,apply patch: java -jar hotfix-agent.jar -patch \"patch.jar\" -pid [pid]")
    private boolean help;
    public static LineArgs parse(String[] argv) {
        LineArgs lineArgs = new LineArgs();
        JCommander jCommander=JCommander.newBuilder()
                .addObject(lineArgs)
                .build();
        jCommander.parse(argv);
        validate(jCommander,lineArgs);
        return lineArgs;
    }

    public static LineArgs parse(String argument) {
        LineArgs lineArgs = new LineArgs();
        List<String> list = new ArrayList<>();
        for (String option : argument.split(",")) {
            list.addAll(Arrays.asList(option.split("=")));
        }
        JCommander jCommander=JCommander.newBuilder()
                .addObject(lineArgs)
                .build();
        jCommander.parse(list.toArray(new String[0]));
        validate(jCommander,lineArgs);
        return lineArgs;
    }
    static void validate(JCommander jCommander,LineArgs args){
        if(args.isHelp()){
            jCommander.setColumnSize(100);
            jCommander.usage();
            return;
        }
        CommandLineEnum cmdEnum=args.getCmd();
        if(cmdEnum==CommandLineEnum.APPLY||cmdEnum==CommandLineEnum.UNINSTALL){
            if(args.getPatch()==null){
                throw new RuntimeException("must be set patch parameter.");
            }
        }
    }
    @SneakyThrows
    @Override
    public String toString() {
        String path = null;
        if (patch != null) {
            path = patch.getFile();
            path = path.substring(0, path.length() - 2);
            path = new URL(path).getFile();
        }
        return new StringJoiner(",")
                .add("pid=" + pid)
                .add("patch=" + path)
                .add("cmd=" + cmd)
                .toString();
    }

    static class CommandLineEnumConverter extends EnumConverter<CommandLineEnum> {
        public CommandLineEnumConverter(String optionName, Class<CommandLineEnum> clazz) {
            super(optionName, clazz);
        }
    }

    static class JarUrlConverter extends URLConverter {
        public JarUrlConverter(String optionName) {
            super(optionName);
        }

        @Override
        public URL convert(String value) {
            File jarFile = new File(value);
            if (!jarFile.exists()) {
                throw new ParameterException(getErrorString(value, "JarUrl.jar file not exists."));
            }
            String jarUrl = "jar:" + jarFile.toURI() + "!/";
            return super.convert(jarUrl);
        }
    }
}
