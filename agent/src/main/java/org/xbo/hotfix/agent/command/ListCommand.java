package org.xbo.hotfix.agent.command;

import lombok.extern.slf4j.Slf4j;
import org.xbo.hotfix.agent.Installer;
import org.xbo.hotfix.agent.PatchManager;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Collections;

/**
 * @description: 日志打印安装的所有补丁
 * @author: xbo
 * @date: 2020/01/11 22:40
 */
@Slf4j
public class ListCommand implements Command {
    @Override
    public void execute(Installer.Context context) throws Exception {
        Collection<PatchManager.PatchInfo> list=context.getPatchManager().list();
        final String repeated=String.join("", Collections.nCopies(20, "**"));
        StringBuilder stringBuilder=new StringBuilder();
        stringBuilder.append(repeated);
        stringBuilder.append("PATCH LIST");
        stringBuilder.append(repeated);
        stringBuilder.append("\n");
        stringBuilder.append("  load time  ");
        stringBuilder.append("\t\t\t\t");
        stringBuilder.append("url");
        Format formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        list.forEach(item->{
            stringBuilder.append("\n");
            stringBuilder.append("  "+ formatter.format(item.getLoadTime())+"  ");
            stringBuilder.append("\t\t");
            stringBuilder.append(item.getJarUrl());
        });
        stringBuilder.append("\n");
        stringBuilder.append(repeated);
        stringBuilder.append("PATCH LIST");
        stringBuilder.append(repeated);
        log.info("\n{}",stringBuilder.toString());
    }
}
