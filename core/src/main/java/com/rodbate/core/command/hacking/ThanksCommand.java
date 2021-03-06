package com.rodbate.core.command.hacking;

import com.rodbate.core.command.Command;
import com.rodbate.core.command.annotation.Cmd;
import com.rodbate.core.server.Session;
import org.apache.commons.io.IOUtils;

import java.lang.instrument.Instrumentation;

/**
 * 工具介绍<br/>
 * 感谢
 * Created by oldmanpushcart@gmail.com on 15/9/1.
 */
@Cmd(isHacking = true, name = "thanks", summary = "Thanks",
        eg = {
                "thanks"
        }
)
public class ThanksCommand implements Command {

    @Override
    public Action getAction() {
        return new SilentAction() {

            @Override
            public void action(Session session, Instrumentation inst, Printer printer) throws Throwable {

                printer.println(IOUtils.toString(getClass().getResourceAsStream("/com/rodbate/core/res/thanks.txt"))).finish();

            }
        };
    }

}
