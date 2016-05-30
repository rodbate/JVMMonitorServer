package com.dataeye.core.command;


import com.dataeye.core.command.annotation.Cmd;
import com.dataeye.core.server.Session;

import java.lang.instrument.Instrumentation;

/**
 * 退出命令
 * Created by oldmanpushcart@gmail.com on 15/5/18.
 */
@Cmd(name = "quit", sort = 8, summary = "Quit Greys console",
        eg = {
                "quit"
        })
public class QuitCommand implements Command {

    @Override
    public Action getAction() {
        return new SilentAction() {

            @Override
            public void action(Session session, Instrumentation inst, Printer printer) throws Throwable {
                printer.println("Bye!").finish();
            }

        };
    }
}
