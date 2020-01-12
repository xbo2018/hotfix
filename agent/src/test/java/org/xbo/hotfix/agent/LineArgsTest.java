package org.xbo.hotfix.agent;

import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;
import org.xbo.hotfix.agent.LineArgs;

@Slf4j
public class LineArgsTest {

    @Test
    public void parse() {
        String[] argv = { "-pid", "2", "-patch", "../example/target/patch-example-1.0-SNAPSHOT.jar",
                "-cmd", "apply" };
        LineArgs args= LineArgs.parse(argv);
        String argument=args.toString();
        log.info(argument);
        args= LineArgs.parse(argument);
        Assert.assertNotNull(args);
    }
}